package inodes.service.api;

import inodes.models.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public abstract class CollabService {

    @Autowired
    AuthorizationService AS;

    @Autowired
    DataService DS;

    public Map<String, Long> getVotes(List<String> id) throws Exception {
        return _getVotes(id);
    }

    public List<Comment> getComments(String id) throws Exception {
        return _getComments(id);
    }

    protected abstract List<Comment> _getComments(String id) throws Exception;

    public void upVote(String user, String id) throws Exception {
        AS.checkUpVotePermission(user, DS.get(id));
        _upvote(user, id);
    }

    public void downVote(String user, String id) throws Exception {
        AS.checkDownVotePermission(user, DS.get(id));
        _downvote(user, id);
    }

    public void comment(String user, String id, String comment) throws Exception {
        AS.checkCreatePermission(user, DS.get(id));
        _comment(user, id, comment);
    }

    protected abstract void _comment(String user, String id, String comment) throws Exception;

    protected abstract void _downvote(String user, String id) throws Exception;

    protected abstract void _upvote(String user, String id) throws Exception;

    protected abstract Map<String, Long> _getVotes(List<String> id) throws Exception;

}
