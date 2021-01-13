package inodes.service.api;

import inodes.models.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public abstract class AuthorizationService {

    @Autowired
    AuthenticationService AS;

    @Autowired
    DataService DS;

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
        if(!userId.equals(doc.getOwner()) || AS.isAdmin(userId)) {
            throw new UnAuthorizedException(userId + " has no permission to delete a " + doc.getId());
        }
    }

    boolean hasEditPermission(String userId, Document doc) throws Exception {
        return AS.getUser(userId).getRoles().contains("EDIT") && doc.getOwner().equals(userId);
    }

    boolean hasUpVotePermission(String userId, Document doc) throws Exception {
        return AS.getUser(userId).getRoles().contains("UPVOTE") && doc.upVotable();
    }

    boolean hasDownVotePermission(String userId, Document doc) throws Exception {
        return AS.getUser(userId).getRoles().contains("DOWNVOTE") && doc.downVotable();
    }
}
