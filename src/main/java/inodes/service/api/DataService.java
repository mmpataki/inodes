package inodes.service.api;

import inodes.models.*;
import inodes.util.SecurityUtil;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.print.Doc;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static inodes.util.TryCatchUtil.tc;

@Service
public abstract class DataService extends Observable {

    public enum ObservableEvents {
        SEARCH,
        NEW,
        APPROVAL_NEEDED,
        UPDATE
    }

    public enum BulkUpdateField {
        VISIBILITY {
            @Override
            public void update(Document d, List<Update> updates) throws Exception {
                for (Update update : updates) {
                    switch (update.getType()) {
                        case ADD:
                            String ug = (String) update.getUpdate();
                            validateUGExists(ug);
                            d.getVisibility().add(ug);
                            break;
                        case DELETE:
                            d.getVisibility().remove((String) update.getUpdate());
                    }
                }
            }
        },
        OWNER {
            @Override
            public void update(Document d, List<Update> updates) throws Exception {
                if(updates.size() == 0)
                    return;
                Update upd = updates.get(0);
                if(updates.size() > 1 || !upd.getType().equals(UpdateType.SET))
                    throw new IllegalArgumentException("single update SET is supported for owner");
                String ug = (String) upd.getUpdate();
                if(getUFromUtag(ug) == null)
                    throw new IllegalArgumentException("Owner can be a user not something else");

                validateUGExists(ug);
                d.setOwner(getUFromUtag(ug));
            }
        },
        TAGS {
            @Override
            public void update(Document d, List<Update> updates) throws Exception {
                for (Update update : updates) {
                    switch (update.getType()) {
                        case ADD:
                            d.getTags().add((String) update.getUpdate());
                            break;
                        case DELETE:
                            d.getTags().remove(update.getUpdate());
                    }
                }
            }
        };

        private static void validateUGExists(String ug) throws Exception {
            String u = getUFromUtag(ug);
            String g = getGFromGtag(ug);
            if(u != null) {
                User usr = getUGS().getUser(u);
                if (usr == null) {
                    throw new Exception("User " + u + " not found");
                }
            } else if(g != null) {
                Group grp = getUGS().getGroup(g);
                if (grp == null) {
                    throw new Exception("Group " + g + " not found");
                }
            } else {
                throw new Exception(ug + " is not in u-uid / g-gid format");
            }
        }

        public void update(Document d, List<Update> updates) throws Exception {
            throw new UnsupportedOperationException("default function called");
        }
    }

    @Data
    public static class SearchResponse {
        List<Document> results;
        Map<String, Map<String, Long>> facetResults;
        long totalResults;
        Object debugInfo;
    }

    @Autowired
    AuthorizationService AS;

    @Autowired
    UserGroupService US;

    @Autowired
    KlassService KS;

    List<String> klassesNeedingViewPermission = new ArrayList<>();

    public List<String> getKlassesNeedingViewPermission() {
        return klassesNeedingViewPermission;
    }


    static DataService PDS;

    static UserGroupService getUGS() {
        return PDS.US;
    }

    @PostConstruct
    public void _init() throws Exception {

        US.registerPostEvent(UserGroupService.Events.USER_SEARCH, ed -> {
            UserInfo userInfo = (UserInfo) ed.get("userInfo");
            userInfo.addExtraInfo("postCount", getUserPostsFacets(userInfo.getBasic().getUserName()));
        });

        for (String klassName : KS.getRegisteredKlasses()) {
            if (KS.getKlass(klassName).isPermissionNeeded())
                klassesNeedingViewPermission.add(klassName);
        }

        /* enums + helper functions need this */
        PDS = this;
    }

    public static String getUFromUtag(String uTag) {
        if (uTag.startsWith("u-")) return uTag.substring(2);
        return null;
    }

    public static String getGFromGtag(String gTag) {
        if (gTag.startsWith("g-")) return gTag.substring(2);
        return null;
    }

    public static String getUserTag(String userName) {
        return "u-" + userName;
    }

    public static String getGroupTag(String groupName) {
        return "g-" + groupName;
    }

    public SearchResponse search(SearchQuery q) throws Exception {
        String user = SecurityUtil.getCurrentUser();
        q.setVisibility(new HashSet<>());
        if (user != null && !user.isEmpty()) {
            q.getVisibility().add(getUserTag(user));
        }
        q.getVisibility().addAll(US.getGroupsOf(user).stream().map(DataService::getGroupTag).collect(Collectors.toList()));
        if (US.isAdmin(user)) {
            q.getVisibility().clear();
            q.getVisibility().add("*");
        }
        notifyPreEvent(ObservableEvents.SEARCH, EventData.of("query", q));
        SearchResponse resp = _search(user, q);
        notifyPostEvent(ObservableEvents.SEARCH, EventData.of("query", q, "results", resp.getResults()));
        return resp;
    }

    public void deleteObj(String id) throws Exception {
        AS.checkDeletePermission(id);
        _deleteObj(id);
    }

    public Document get(String id) throws Exception {
        try {
            return search(SearchQuery.builder().id(id).pageSize(1).build()).getResults().get(0);
        } catch (IndexOutOfBoundsException i) {
            throw new NoSuchDocumentException(id);
        }
    }

    public void putData(Document doc, String changeNote) throws Exception {
        assert
                Objects.nonNull(doc) &&
                        Objects.nonNull(doc.getContent()) &&
                        Objects.nonNull(doc.getTags()) &&
                        Objects.nonNull(doc.getVisibility()) &&
                        Objects.nonNull(doc.getType());

        if (doc.getTags().contains("inodesapp") && !US.amIAdmin())
            doc.getTags().remove("inodesapp");

        if (doc.getId() != null && !doc.getId().isEmpty()) {
            if (changeNote == null || changeNote.isEmpty()) {
                throw new Exception("change note is required to edit this item");
            }
            Document oldDoc = get(doc.getId());
            AS.checkEditPermission(oldDoc);
            doc.setOwner(oldDoc.getOwner());
            doc.setVotes(oldDoc.getVotes());
            doc.setComments(oldDoc.getComments());
            doc.setPostTime(oldDoc.getPostTime());
            doc.setType(oldDoc.getType());
            updateContent(doc, changeNote);
        } else {
            AS.checkCreatePermission(doc);
            doc.setId(UUID.randomUUID().toString());
            doc.setPostTime(System.currentTimeMillis());
            doc.setOwner(SecurityUtil.getCurrentUser());
            createContent(doc, changeNote);
        }
    }

    public void updateContent(Document doc, String changeNote) throws IOException {
        notifyPreEvent(ObservableEvents.UPDATE, EventData.of("doc", doc, "changeNote", changeNote));
        _putData(doc);
        notifyPostEvent(ObservableEvents.UPDATE, EventData.of("doc", doc, "changeNote", changeNote));
    }

    public void createContent(Document doc, String changeNote) throws IOException {
        notifyPreEvent(ObservableEvents.NEW, EventData.of("doc", doc, "changeNote", changeNote));
        _putData(doc);
        notifyPostEvent(ObservableEvents.NEW, EventData.of("doc", doc, "changeNote", changeNote));
    }

    public Map<String, Object> bulkUpdateContent(BulkUpdateRequest req) throws Exception {
        System.out.println(req);
        Map<String, String> failedUpdates = new HashMap<>();
        Set<String> updatedDocs = new HashSet<>();
        iterateAllDocs(req.getSearchQuery(), (d) -> {
            try {
                AS.checkEditPermission(d);
            } catch (Exception e) {
                failedUpdates.put(d.getId(), e.getMessage());
                return;
            }
            boolean failed = false;
            for (Map.Entry<BulkUpdateField, List<Update>> entry : req.getUpdates().entrySet()) {
                BulkUpdateField updateField = entry.getKey();
                List<Update> updates = entry.getValue();
                try {
                    updateField.update(d, updates);
                } catch (Exception e) {
                    failedUpdates.put(d.getId(), e.getMessage());
                    failed = true;
                }
            }
            if (!failed) {
                try {
                    _putData(d);
                    updatedDocs.add(d.getId());
                } catch (IOException e) {
                    failedUpdates.put(d.getId(), e.getMessage());
                }
            }
        });
        Map<String, Object> ret = new HashMap<>();
        ret.put("successful", updatedDocs);
        ret.put("failed", failedUpdates);
        return ret;
    }

    public void approve(String docId) throws Exception {
        Document doc = get(docId);
        AS.checkApprovePermission(docId);
        doc.setVisibility(doc.getSavedVisibility());
        doc.setNeedsApproval(false);
        _putData(doc);
    }

    public void flag(String docId) throws Exception {
        Document doc = get(docId);
        AS.checkFlagPermission(doc);
        doc.setSavedVisibility(doc.getVisibility());
        doc.setVisibility(Arrays.asList(doc.getOwner(), getGroupTag(UserGroupService.SECURITY)));
        doc.setNeedsApproval(true);
        _putData(doc);
    }

    protected abstract SearchResponse _search(String user, SearchQuery q) throws Exception;

    protected abstract void _deleteObj(String id) throws Exception;

    protected abstract void _putData(Document doc) throws IOException;

    protected abstract Map<String, Long> _getUserPostsFacets(String user) throws Exception;

    public Map<String, Long> getUserPostsFacets(String user) throws Exception {
        return _getUserPostsFacets(user);
    }

    public void iterateAllDocs(String q, Consumer<Document> consumer) throws Exception {
        int ps = 100;
        while (true) {
            SearchQuery sq = SearchQuery.builder()
                    .q(q)
                    .visibility(Collections.singleton("*"))
                    .offset(0).pageSize(ps)
                    .build();
            SearchResponse resp = _search("x", sq);
            for (Document document : resp.getResults()) {
                consumer.accept(document);
            }
            if (resp.getResults().size() < ps)
                break;
        }
    }

    public enum UpdateType {
        ADD, DELETE, SET
    }

    @Data
    @NoArgsConstructor
    public static class Update {
        UpdateType type;
        Object update;
    }

    @Data
    @NoArgsConstructor
    public static class BulkUpdateRequest {
        String searchQuery;
        Map<BulkUpdateField, List<Update>> updates;
    }

    @Builder
    @Data
    public static class SearchQuery {
        String q;
        String id;
        long offset;
        int pageSize;
        List<String> sortOn;
        List<String> fq;
        int fqLimit;
        Set<String> visibility;
    }

}
