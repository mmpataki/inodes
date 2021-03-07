package inodes.service.api;

import inodes.models.Document;
import inodes.models.UserInfo;
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
        APPROVAL_NEEDED
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


    @PostConstruct
    public void _init() {
        US.registerPostEvent(UserGroupService.Events.USER_SEARCH, o -> {
            UserInfo userInfo = (UserInfo) o;
            userInfo.addExtraInfo("postCount", getUserPostsFacets(userInfo.getBasic().getUserName()));
        });
    }

    public static String getUserTag(String userName) {
        return "u-" + userName;
    }

    public static String getGroupTag(String groupName) {
        return "g-" + groupName;
    }

    public SearchResponse search(String user, SearchQuery q) throws Exception {
        q.setVisibility(new HashSet<>());
        if (user != null && !user.isEmpty()) {
            q.getVisibility().add(getUserTag(user));
        }
        q.getVisibility().addAll(US.getGroupsOf(user).stream().map(DataService::getGroupTag).collect(Collectors.toList()));
        SearchResponse resp = _search(user, q);
        notifyPostEvent(ObservableEvents.SEARCH, resp.getResults());
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

        if (doc.getTags().contains("inodesapp") && !US.isAdmin(user))
            doc.getTags().remove("inodesapp");

        if (doc.getId() != null && !doc.getId().isEmpty()) {
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
        notifyPreEvent(ObservableEvents.NEW, doc);
        _putData(doc);
        notifyPostEvent(ObservableEvents.NEW, doc);
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
