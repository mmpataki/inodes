package inodes.service.api;

import com.google.gson.Gson;
import inodes.models.Comment;
import inodes.models.Document;
import inodes.service.EmailService;
import inodes.service.DBBasedUserGroupService;
import inodes.util.UrlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public abstract class CollabService extends Observable {

    @Autowired
    DBBasedUserGroupService US;

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
                    String comment = (new Gson()).fromJson(chunks[2], String.class);

                    Document d = DS.get("admin", chunks[1]);
                    UserGroupService.User owner = US.getUser(d.getOwner());

                    Set<String> rcpnts = new HashSet<String>();
                    rcpnts.add(owner.getEmail());
                    Matcher m = Pattern.compile("\\@([0-9A-Za-z]+)").matcher(comment);
                    while(m.find()) {
                        try {
                            String uName = m.group(1);
                            System.out.println(uName);
                            String email = US.getUser(uName).getEmail();
                            if(email != null && !email.isEmpty()) {
                                rcpnts.add(email);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if(owner.getEmail() != null) {
                        ES.sendEmail(rcpnts, getCommentEmailSubject(chunks[0]), getCommentEmailBody(chunks[0], chunks[1], comment));
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
                "<a href='%s'>%s</a> commented on your <a href='%s'>post</a></br>" +
                "<div style='padding: 10px; background-color: #f2f2f2; border: solid 1px gray; display: block'>%s</div>" +
                "</div>",
                UrlUtil.getUserUrl(user), user, UrlUtil.getDocUrl(id), comment
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

    public Comment comment(String user, String id, String comment) throws Exception {
        AS.checkCommentPermission(user, DS.get(user, id));
        Comment c = _comment(user, id, comment);
        notifyObservers("comment", new String[] {user, id, comment});
        return c;
    }

    // id's made up of three parts
    public void deleteComment(String user, String id, String owner, long time) throws Exception {
        AS.checkCommentDeletePermission(user, id, owner, time, DS.get(user, id));
        _deleteComment(id, owner, time);
    }

    protected abstract Comment _comment(String user, String id, String comment) throws Exception;

    protected abstract void _deleteComment(String id, String owner, long time) throws Exception;

    protected abstract void _downvote(String user, String id) throws Exception;

    protected abstract void _upvote(String user, String id) throws Exception;

    protected abstract Map<String, Long> _getVotes(List<String> id) throws Exception;

}
