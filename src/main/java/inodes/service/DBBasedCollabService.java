package inodes.service;

import inodes.Configuration;
import inodes.models.Comment;
import inodes.service.api.CollabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class DBBasedCollabService extends CollabService {

    @Autowired
    Configuration conf;

    Connection CONN;

    @PostConstruct
    void init() throws Exception {
        try {
            CONN = DriverManager.getConnection(conf.getProperty("collabservice.db.url"), conf.getProperty("collabservice.db.user"), conf.getProperty("collabservice.db.password"));
            CONN.setAutoCommit(true);
            try {
                CONN.createStatement().execute("CREATE TABLE votes (postid VARCHAR(64) PRIMARY KEY, nvotes NUMBER)");
            } catch (Exception e) {
            }
            try {
                CONN.createStatement().execute("CREATE TABLE pcomments (postid VARCHAR(64), userid VARCHAR(32), itime NUMBER, txt VARCHAR(256), CONSTRAINT pcomments_pk PRIMARY KEY(postid, userid, itime))");
            } catch (Exception e) {
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    protected Map<String, Long> _getVotes(List<String> ids) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size() - 1; i++) sb.append("?, ");
        sb.append('?');

        String sql = String.format("SELECT postid, nvotes FROM votes WHERE postid in (%s)", sb.toString());
        System.out.println(sql);

        PreparedStatement ps = CONN.prepareStatement(sql);
        int i = 1;
        for (String id : ids) {
            ps.setString(i++, id);
        }
        Map<String, Long> ret = new HashMap<>();
        boolean vals = ps.execute();
        if (vals) {
            ResultSet rs = ps.getResultSet();
            while(rs.next()) {
                ret.put(rs.getString(1), rs.getLong(2));
            }
        }
        return ret;
    }

    @Override
    protected List<Comment> _getComments(String id) throws Exception {
        PreparedStatement ps = CONN.prepareStatement("SELECT postid, userid, itime, txt FROM pcomments WHERE postid=?");
        ps.setString(1, id);
        boolean vals = ps.execute();
        List<Comment> ret = new LinkedList<>();
        if (vals) {
            ResultSet rs = ps.getResultSet();
            while (rs.next()) {
                ret.add(new Comment(rs.getString(1), rs.getString(2), rs.getLong(3), rs.getString(4)));
            }
        }
        return ret;
    }

    @Override
    protected Comment _comment(String user, String id, String comment) throws Exception {
        Comment c = new Comment(id, user, System.currentTimeMillis(), comment);
        PreparedStatement ps = CONN.prepareStatement("INSERT INTO pcomments (postid, userid, itime, txt) values (?, ?, ?, ?)");
        ps.setString(1, id);
        ps.setString(2, user);
        ps.setLong(3, c.time);
        ps.setString(4, comment);
        ps.executeUpdate();
        return c;
    }

    @Override
    protected void _deleteComment(String id, String owner, long time) throws Exception {
        PreparedStatement ps = CONN.prepareStatement("DELETE FROM pcomments WHERE postid=? AND userid=? AND itime=?");
        ps.setString(1, id);
        ps.setString(2, owner);
        ps.setLong(3, time);
        ps.executeUpdate();
    }

    @Override
    protected void _downvote(String user, String id) throws Exception {
        vote(id, -1);
    }

    @Override
    protected void _upvote(String user, String id) throws Exception {
        vote(id, +1);
    }

    private void vote(String id, int delta) throws Exception {
        PreparedStatement ps = CONN.prepareStatement("begin insert into votes (postid, nvotes) values (?, 0); exception when dup_val_on_index then update votes set nvotes = nvotes+? where  postid = ?; end;");
        ps.setString(1, id);
        ps.setInt(2, delta);
        ps.setString(3, id);
        ps.executeUpdate();
    }
}
