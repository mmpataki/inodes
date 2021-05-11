package inodes.service.api;

import inodes.models.Comment;
import inodes.models.Document;
import inodes.util.SecurityUtil;
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
        DS.registerPostEvent(DataService.ObservableEvents.SEARCH, ed -> {
            List<Document> docs = (List<Document>) ed.get("results");
            Map<String, Long> votes = this.getVotes(docs.stream().map(d -> d.getId()).collect(Collectors.toList()));
            for (Document doc : docs) {
                Long l = votes.get(doc.getId());
                doc.setVotes(l == null ? 0 : l);
            }
        });
    }

    public Map<String, Long> getVotes(List<String> id) throws Exception {
        return _getVotes(id);
    }

    public void upVote(String id) throws Exception {
        AS.checkUpVotePermission(id);
        _upvote(id);
    }

    public void downVote(String id) throws Exception {
        AS.checkDownVotePermission(id);
        _downvote(id);
    }

    public List<Comment> getComments(String id) throws Exception {
        return _getComments(id);
    }

    public Comment comment(String id, String comment) throws Exception {
        AS.checkCommentPermission(id);
        notifyPreEvent(EventType.NEW_COMMENT, EventData.of("docid", id, "comment", comment));
        Comment c = _comment(id, comment);
        notifyPostEvent(EventType.NEW_COMMENT, EventData.of("docid", id, "comment", comment));
        return c;
    }

    // id's made up of three parts
    public void deleteComment(String id, String owner, long time) throws Exception {
        AS.checkCommentDeletePermission(id, owner);
        _deleteComment(id, owner, time);
    }


    protected abstract List<Comment> _getComments(String id) throws Exception;
    protected abstract Comment _comment(String id, String comment) throws Exception;
    protected abstract void _deleteComment(String id, String owner, long time) throws Exception;

    protected abstract void _downvote(String id) throws Exception;
    protected abstract void _upvote(String id) throws Exception;
    protected abstract Map<String, Long> _getVotes(List<String> id) throws Exception;

}
