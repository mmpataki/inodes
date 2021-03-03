package inodes.service.api;

import inodes.models.Comment;
import inodes.models.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
public abstract class CollabService extends Observable {

    @Autowired
    AuthorizationService AS;

    @Autowired
    DataService DS;

    enum EventType {
        NEW_COMMENT
    }

    @PostConstruct
    public void _init() {

        /* append votes to the search results */
        registerPostEvent(DataService.ObservableEvents.SEARCH, o -> {
            Map<String, Long> votes = this.getVotes(((List<Document>) o).stream().map(d -> d.getId()).collect(Collectors.toList()));
            for (Document doc : (List<Document>)o) {
                Long l = votes.get(doc.getId());
                doc.setVotes(l == null ? 0 : l);
            }
        });
    }

    public Map<String, Long> getVotes(List<String> id) throws Exception {
        return _getVotes(id);
    }

    public void upVote(String user, String id) throws Exception {
        AS.checkUpVotePermission(user, DS.get(user, id));
        _upvote(user, id);
    }

    public void downVote(String user, String id) throws Exception {
        AS.checkDownVotePermission(user, DS.get(user, id));
        _downvote(user, id);
    }

    public List<Comment> getComments(String id) throws Exception {
        return _getComments(id);
    }

    public Comment comment(String user, String id, String comment) throws Exception {
        AS.checkCommentPermission(user, DS.get(user, id));
        Comment c = _comment(user, id, comment);
        notifyPostEvent(EventType.NEW_COMMENT, Arrays.asList(user, id, comment));
        return c;
    }

    // id's made up of three parts
    public void deleteComment(String user, String id, String owner, long time) throws Exception {
        AS.checkCommentDeletePermission(user, id, owner, time, DS.get(user, id));
        _deleteComment(id, owner, time);
    }


    protected abstract List<Comment> _getComments(String id) throws Exception;
    protected abstract Comment _comment(String user, String id, String comment) throws Exception;
    protected abstract void _deleteComment(String id, String owner, long time) throws Exception;

    protected abstract void _downvote(String user, String id) throws Exception;
    protected abstract void _upvote(String user, String id) throws Exception;
    protected abstract Map<String, Long> _getVotes(List<String> id) throws Exception;

}
