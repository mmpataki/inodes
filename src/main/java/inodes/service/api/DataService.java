package inodes.service.api;

import inodes.models.Document;
import inodes.models.PermissionRequest;
import inodes.models.UserInfo;
import inodes.util.SecurityUtil;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
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

    @Data
    public static class SearchResponse {
        List<Document> results;
        Map<String, Map<String, Long>> facetResults;
        long totalResults;
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

    @PostConstruct
    public void _init() throws Exception {

        US.registerPostEvent(UserGroupService.Events.USER_SEARCH, ed -> {
            UserInfo userInfo = (UserInfo) ed.get("userInfo");
            userInfo.addExtraInfo("postCount", getUserPostsFacets());
        });

        for (String klassName : KS.getRegisteredKlasses()) {
            if (KS.getKlass(klassName).isPermissionNeeded())
                klassesNeedingViewPermission.add(klassName);
        }
    }

    public static String getUFromUtag(String uTag) {
        if(uTag.startsWith("u-")) return uTag.substring(2);
        return null;
    }

    public static String getGFromGtag(String gTag) {
        if(gTag.startsWith("g-")) return gTag.substring(2);
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

    public Map<String, Long> getUserPostsFacets() throws Exception {
        return search(SearchQuery.builder().q("*").fq(Collections.singletonList("type")).fqLimit(Integer.MAX_VALUE).build()).getFacetResults().get("type");
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
