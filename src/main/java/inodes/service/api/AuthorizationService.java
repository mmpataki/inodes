package inodes.service.api;

import inodes.models.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public abstract class AuthorizationService {

    @Autowired
    UserGroupService AS;

    @Autowired
    UserGroupService GS;

    boolean hasCommentPermission(String userId, Document doc) throws Exception {
        return AS.getUser(userId).getRoles().contains("COMMENT");
    }

    public void checkCreatePermission(String userId, Document doc) throws Exception {
        if(!AS.getUser(userId).getRoles().contains("CREATE")) {
            throw new UnAuthorizedException(userId + " has no permission to create a post");
        }
    }

    public void checkApprovePermission(String userId, Document doc) throws Exception {
        if(!AS.getGroupsOf(userId).contains(UserGroupService.SECURITY)) {
            throw new UnAuthorizedException("Not authorized to approve");
        }
    }

    public void checkFlagPermission(String userId, Document doc) throws Exception {
        // logged in is enough
    }

    public void checkDeletePermission(String userId, Document doc) throws Exception {
        if(doc.getOwner() == null) return;
        if(!userId.equals(doc.getOwner()) && AS.isAdmin(userId)) {
            throw new UnAuthorizedException(userId + " has no permission to delete a " + doc.getId());
        }
    }

    public boolean checkUpVotePermission(String userId, Document doc) throws Exception {
        return AS.getUser(userId).getRoles().contains("UPVOTE") && doc.upVotable();
    }

    public boolean checkDownVotePermission(String userId, Document doc) throws Exception {
        return AS.getUser(userId).getRoles().contains("DOWNVOTE") && doc.downVotable();
    }

    public boolean checkCommentPermission(String userId, Document doc) throws Exception {
        return AS.getUser(userId).getRoles().contains("COMMENT") && doc.commentable();
    }

    public void checkCommentDeletePermission(String userId, String id, String owner, long time, Document doc) throws Exception {
        if(!userId.equals(owner) || !AS.getUser(userId).getRoles().contains("COMMENT") || !doc.commentable()) {
            throw new UnAuthorizedException(userId + " has no permission to delete comment from " + owner);
        }
    }

    public boolean checkTagCreatePermission(String userId) throws Exception {
        return AS.getUser(userId).getRoles().contains("TAGCREATE");
    }

    public void checkKlassCreatePermission(String userId) throws Exception {
        if(!AS.getUser(userId).getRoles().contains("KLASSCREATE") && !AS.isAdmin(userId)) {
            throw new UnAuthorizedException(userId + " has no permission to create a klass");
        }
    }

    public void checkUpdatePermission(String userId, Document oldDoc, Document newDoc) throws Exception {
        if((!userId.equals(oldDoc.getOwner())) && !AS.getUser(userId).getRoles().contains("EDIT") && !AS.isAdmin(userId)) {
            throw new UnAuthorizedException(userId + " has no permission to create a klass");
        }
    }

    public void checkEditPermission(String userId, Document doc) throws Exception {
        if(!AS.getUser(userId).getRoles().contains("EDIT") && !doc.getOwner().equals(userId) && !AS.isAdmin(userId)) {
            throw new UnAuthorizedException(userId + " has no permission to edit " + doc.getId());
        }
    }

    public void checkGroupCreationPermissions(String user) throws Exception {
        // logged in? then fine
    }

    public void checkAddUserToGroupPermission(String user, String group) throws Exception {
        if(!AS.isAdmin(user) && !GS.getGroup(group).getUsers().contains(user)) {
            throw new UnAuthorizedException("You are not allowed to add users to these groups");
        }
    }

    public void checkDeleteUserFromGroupPermission(String user, String group) throws Exception {
        if(!AS.isAdmin(user) && !GS.getGroup(group).getUsers().contains(user)) {
            throw new UnAuthorizedException("You are not allowed to delete users from this group");
        }
    }
}
