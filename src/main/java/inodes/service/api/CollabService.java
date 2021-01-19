package inodes.service.api;

import com.google.gson.Gson;
import inodes.models.Comment;
import inodes.models.Document;
import inodes.service.EmailService;
import inodes.service.DBBasedUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Service
public abstract class CollabService extends Observable {

    @Autowired
    DBBasedUserService US;

    @Autowired
    AuthorizationService AS;

    @Autowired
    DataService DS;

    @Autowired
    EmailService ES;

    @PostConstruct
    public void _init() {
        register("comment", o -> {
            new Thread(() -> {
                try {
                    String chunks[] = (String[]) o;
                    Document d = DS.get("admin", chunks[1]);
                    UserService.User owner = US.getUser(d.getOwner());
                    if(owner.getEmail() != null) {
                        ES.sendEmail(owner.getEmail(), getCommentEmailSubject(chunks[0]), getCommentEmailBody(chunks[0], chunks[1], (new Gson()).fromJson(chunks[2], String.class)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private String getCommentEmailBody(String user, String id, String comment) {
        return String.format(
                "<div style='padding: 10px; border: solid 1px skyblue; display: block'>" +
                "<a href='%s/?q=%%23user !%s'>%s</a> commented on your <a href='%s/?q=@%s'>post</a></br>" +
                "<div style='padding: 10px; background-color: #f2f2f2; border: solid 1px gray; display: block'>%s</div>" +
                "</div>",
                "http://localhost:8080", user, user,
                "http://localhost:8080", id,
                comment
        );
    }

    private String getCommentEmailSubject(String who) {
        return who + " commented on your post";
    }

    public Map<String, Long> getVotes(List<String> id) throws Exception {
        return _getVotes(id);
    }

    public List<Comment> getComments(String id) throws Exception {
        return _getComments(id);
    }

    protected abstract List<Comment> _getComments(String id) throws Exception;

    public void upVote(String user, String id) throws Exception {
        AS.checkUpVotePermission(user, DS.get(user, id));
        _upvote(user, id);
    }

    public void downVote(String user, String id) throws Exception {
        AS.checkDownVotePermission(user, DS.get(user, id));
        _downvote(user, id);
    }

    public void comment(String user, String id, String comment) throws Exception {
        AS.checkCreatePermission(user, DS.get(user, id));
        _comment(user, id, comment);
        notifyObservers("comment", new String[] {user, id, comment});
    }

    protected abstract void _comment(String user, String id, String comment) throws Exception;

    protected abstract void _downvote(String user, String id) throws Exception;

    protected abstract void _upvote(String user, String id) throws Exception;

    protected abstract Map<String, Long> _getVotes(List<String> id) throws Exception;

}
