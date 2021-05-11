package inodes.service.api;

import inodes.models.Document;
import inodes.models.Subscription;
import inodes.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.print.Doc;

@Service
public class AuthorizationService {

    @Autowired
    UserGroupService AS;

    @Autowired
    UserGroupService GS;

    @Autowired
    DataService DS;

    public void checkCreatePermission(String userId, Document doc) throws Exception {
        if(!AS.getUser(userId).getRoles().contains("CREATE")) {
            reject("create a post");
        }
    }

    public void checkCreatePermission(Document doc) throws Exception {
        checkCreatePermission(SecurityUtil.getCurrentUser(), doc);
    }

    public void checkApprovePermission(String userId, String docId) throws Exception {
        Document doc = DS.get(docId);
        if(!AS.getGroupsOf(userId).contains(UserGroupService.SECURITY)) {
            reject("approve");
        }
    }

    public void checkApprovePermission(String docId) throws Exception {
        checkApprovePermission(SecurityUtil.getCurrentUser(), docId);
    }

    public void checkFlagPermission(Document doc) throws Exception {
        // logged in is enough
    }

    public void checkDeletePermission(String userId, String id) throws Exception {
        Document doc = DS.get(id);
        if(doc.getOwner() == null) return;
        if(!userId.equals(doc.getOwner()) && !AS.isAdmin(userId)) {
            reject("delete " + doc.getId());
        }
    }

    public void checkDeletePermission(String id) throws Exception {
        checkDeletePermission(SecurityUtil.getCurrentUser(), id);
    }

    public void checkUpVotePermission(String userId, String id) throws Exception {
        Document doc = DS.get(id);
        if(!AS.getUser(userId).getRoles().contains("UPVOTE") || !doc.upVotable()) {
            reject("upvote " + id);
        }
    }

    public void checkUpVotePermission(String id) throws Exception {
        checkUpVotePermission(SecurityUtil.getCurrentUser(), id);
    }

    public void checkDownVotePermission(String userId, String id) throws Exception {
        Document doc = DS.get(id);
        if(!AS.getUser(userId).getRoles().contains("DOWNVOTE") || !doc.downVotable())
            reject("downvote " + id);
    }

    public void checkDownVotePermission(String id) throws Exception {
        checkDownVotePermission(SecurityUtil.getCurrentUser(), id);
    }

    public void checkCommentPermission(String userId, String id) throws Exception {
        Document doc = DS.get(id);
        if(!AS.getUser(userId).getRoles().contains("COMMENT") || !doc.commentable())
            reject("comment on " + id);
    }

    public void checkCommentPermission(String id) throws Exception {
        checkCommentPermission(SecurityUtil.getCurrentUser(), id);
    }

    public void checkCommentDeletePermission(String userId, String id, String owner) throws Exception {
        Document doc = DS.get(id);
        if(!userId.equals(owner) || !AS.getUser(userId).getRoles().contains("COMMENT") || !doc.commentable()) {
            reject(" delete comment from " + owner);
        }
    }

    public void checkCommentDeletePermission(String id, String owner) throws Exception {
        checkCommentDeletePermission(SecurityUtil.getCurrentUser(), id, owner);
    }

    public void checkTagCreatePermission() throws Exception {
        if(!AS.getUser(SecurityUtil.getCurrentUser()).getRoles().contains("TAGCREATE"))
            reject("create a tag");
    }

    public void checkKlassCreatePermission() throws Exception {
        String userId = SecurityUtil.getCurrentUser();
        if(!AS.getUser(userId).getRoles().contains("KLASSCREATE") && !AS.isAdmin(userId)) {
            reject("create a klass");
        }
    }

    public void checkEditPermission(String userId, Document doc) throws Exception {
        if(!AS.getUser(userId).getRoles().contains("EDIT") && !doc.getOwner().equals(userId) && !AS.isAdmin(userId)) {
            reject("edit " + doc.getId());
        }
    }

    public void checkEditPermission(Document doc) throws Exception {
        checkEditPermission(SecurityUtil.getCurrentUser(), doc);
    }

    public void checkGroupCreationPermissions() throws Exception {
        // logged in? then fine
    }

    public void checkAddUserToGroupPermission(String group) throws Exception {
        String user = SecurityUtil.getCurrentUser();
        if(!AS.isAdmin(user) && !GS.getGroup(group).getUsers().contains(user)) {
            reject("add users to " + group);
        }
    }

    public void checkDeleteUserFromGroupPermission(String group) throws Exception {
        String user = SecurityUtil.getCurrentUser();
        if(!AS.isAdmin(user) && !GS.getGroup(group).getUsers().contains(user)) {
            reject("delete users from " + group);
        }
    }

    public void checkSubscribePermission(String user, Subscription.SubscriberType subscriberType, String subscriberId) throws Exception {
        if((subscriberType == Subscription.SubscriberType.USER && user.equals(subscriberId)) ||
            (subscriberType == Subscription.SubscriberType.GROUP && AS.getGroupsOf(user).contains(subscriberId)))
                return;
        reject("subscribe on other user's behalf or subscribe in behalf of a foreign group");
    }

    public void checkSubscribePermission(Subscription.SubscriberType subscriberType, String subscriberId) throws Exception {
        checkSubscribePermission(SecurityUtil.getCurrentUser(), subscriberType, subscriberId);
    }

    private void reject(String action) throws UnAuthorizedException {
        throw new UnAuthorizedException(SecurityUtil.getCurrentUser() + " has no permission to " + action);
    }

}
