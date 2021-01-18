package inodes.service.api;

import inodes.models.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public abstract class AuthorizationService {

    @Autowired
    AuthenticationService AS;

    boolean hasCommentPermission(String userId, Document doc) throws Exception {
        return AS.getUser(userId).getRoles().contains("COMMENT");
    }

    public void checkCreatePermission(String userId, Document doc) throws Exception {
        if(!AS.getUser(userId).getRoles().contains("CREATE")) {
            throw new UnAuthorizedException(userId + " has no permission to create a post");
        }
    }

    public void checkDeletePermission(String userId, Document doc) throws UnAuthorizedException {
        if(doc.getOwner() == null) return;
        if(!userId.equals(doc.getOwner()) && AS.isAdmin(userId)) {
            throw new UnAuthorizedException(userId + " has no permission to delete a " + doc.getId());
        }
    }

    public boolean hasEditPermission(String userId, Document doc) throws Exception {
        return AS.getUser(userId).getRoles().contains("EDIT") && doc.getOwner().equals(userId);
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

    public boolean checkTagCreatePermission(String userId) throws Exception {
        return AS.getUser(userId).getRoles().contains("TAGCREATE");
    }

    public void checkKlassCreatePermission(String userId) throws Exception {
        if(!AS.getUser(userId).getRoles().contains("KLASSCREATE") && !AS.isAdmin(userId)) {
            throw new UnAuthorizedException(userId + " has no permission to create a klass");
        }
    }
}
