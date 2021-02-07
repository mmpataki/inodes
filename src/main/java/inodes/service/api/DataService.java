package inodes.service.api;

import inodes.Inodes;
import inodes.models.Document;
import inodes.models.Klass;
import inodes.service.EmailService;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public abstract class DataService extends Observable {

    public enum ObservableEvents {
        SEARCH,
        NEW,
        APPROVAL_NEEDED
    }

    public static class SearchResponse {
        List<Document> results;
        Map<String, Map<String, Long>> facetResults;
        long totalResults;

        public Map<String, Map<String, Long>> getFacetResults() {
            return facetResults;
        }

        public void setFacetResults(Map<String, Map<String, Long>> facetResults) {
            this.facetResults = facetResults;
        }

        public List<Document> getResults() {
            return results;
        }

        public void setResults(List<Document> results) {
            this.results = results;
        }

        public long getTotalResults() {
            return totalResults;
        }

        public void setTotalResults(long totalResults) {
            this.totalResults = totalResults;
        }
    }

    @Autowired
    AuthorizationService AS;

    @Autowired
    CollabService CS;

    @Autowired
    UserGroupService UGS;

    @Autowired
    KlassService KS;

    @Autowired
    EmailService ES;


    public DataService() {
        register(ObservableEvents.SEARCH, o -> {
            Map<String, Long> votes = CS.getVotes(((List<Document>) o).stream().map(d -> d.getId()).collect(Collectors.toList()));
            for (Document doc : (List<Document>)o) {
                Long l = votes.get(doc.getId());
                doc.setVotes(l == null ? 0 : l);
            }
        });
        register(ObservableEvents.APPROVAL_NEEDED, o -> {
            Document d = (Document) o;
            ES.sendEmail(
                UGS
                .getGroup(UserGroupService.SECURITY)
                .getUsers()
                .stream()
                .map(u -> { try { return UGS.getUser(u).getEmail(); } catch (Exception e) { return null; }})
                .collect(Collectors.toSet()),
                "New applet alert",
                String.format("New <a href=\"%s/?q=@%s\">applet alert</a>", Inodes.getLocalAddr(), d.getId())
            );
        });
    }

    public SearchResponse search(String user, SearchQuery q) throws Exception {
        q.setVisibility(new HashSet<>());
        if(user != null && !user.isEmpty()) {
            q.getVisibility().add(user);
        }
        q.getVisibility().add("public");
        q.getVisibility().addAll(UGS.getGroupsOf(user));

        SearchResponse resp = _search(user, q);
        notifyObservers(ObservableEvents.SEARCH, resp.getResults());
        return resp;
    }

    public void deleteObj(String user, String id) throws Exception {
        AS.checkDeletePermission(user, get(user, id));
        _deleteObj(id);
    }

    public Document get(String user, String id) throws Exception {
        try {
            return search(user, SearchQuery.builder().id(id).pageSize(1).build()).getResults().get(0);
        } catch (IndexOutOfBoundsException i) {
            throw new NoSuchDocumentException(id);
        }
    }

    public void putData(String user, Document doc) throws Exception {
        assert
            Objects.nonNull(doc) &&
            Objects.nonNull(doc.getContent()) &&
            Objects.nonNull(doc.getTags()) &&
            Objects.nonNull(doc.getVisibility()) &&
            Objects.nonNull(doc.getType());

        if(doc.getTags().contains("inodesapp") && !UGS.isAdmin(user))
            doc.getTags().remove("inodesapp");

        if(doc.getId() != null && !doc.getId().isEmpty()) {
            Document oldDoc = get(user, doc.getId());
            AS.checkEditPermission(user, oldDoc);
            doc.setOwner(oldDoc.getOwner());
            doc.setVotes(oldDoc.getVotes());
            doc.setComments(oldDoc.getComments());
            doc.setPostTime(oldDoc.getPostTime());
            doc.setType(oldDoc.getType());
        } else {
            AS.checkCreatePermission(user, doc);
            doc.setId(UUID.randomUUID().toString());
            doc.setPostTime(System.currentTimeMillis());
            doc.setOwner(user);
        }
        notifyObservers(ObservableEvents.NEW, doc);
        Klass klass = KS.getKlass(doc.getType());
        if(klass.isEditApprovalNeeded()) {
            doc.setNeedsApproval(true);
            doc.setSavedVisibility(doc.getVisibility());
            doc.setVisibility(Arrays.asList(doc.getOwner(), UserGroupService.SECURITY));
            notifyObservers(ObservableEvents.APPROVAL_NEEDED, doc);
        }
        _putData(doc);
    }

    public void approve(String userId, String docId) throws Exception {
        Document doc = get(userId, docId);
        AS.checkApprovePermission(userId, doc);
        doc.setVisibility(doc.getSavedVisibility());
        doc.setNeedsApproval(false);
        _putData(doc);
    }

    public void flag(String userId, String docId) throws Exception {
        Document doc = get(userId, docId);
        AS.checkFlagPermission(userId, doc);
        doc.setSavedVisibility(doc.getVisibility());
        doc.setVisibility(Arrays.asList(doc.getOwner(), UserGroupService.SECURITY));
        doc.setNeedsApproval(true);
        _putData(doc);
    }

    protected abstract SearchResponse _search(String user, SearchQuery q) throws Exception;

    protected abstract void _deleteObj(String id) throws Exception;

    protected abstract void _putData(Document doc) throws IOException;

    public Map<String, Long> getUserPostsFacets(String userName) throws Exception {
        return search(userName, SearchQuery.builder().q("*").fq(Arrays.asList("type")).fqLimit(Integer.MAX_VALUE).build()).getFacetResults().get("type");
    }

    @Builder @Data
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
