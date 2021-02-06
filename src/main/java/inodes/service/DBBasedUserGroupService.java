package inodes.service;

import inodes.Configuration;
import inodes.service.api.UserExistsException;
import inodes.service.api.UserGroupService;
import inodes.service.api.NoSuchUserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.*;
import java.util.*;

import static inodes.util.TryCatchUtil.tc;

@Service
public class DBBasedUserGroupService extends UserGroupService {

    Logger LOG = LoggerFactory.getLogger(DBBasedUserGroupService.class);

    @Autowired
    Configuration conf;

    Map<String, User> users = new HashMap<>();
    Map<String, Group> groups = new HashMap<>();

    Connection CONN;
    Random R = new Random();

    @PostConstruct
    void init() throws Exception {
        try {
            CONN = DriverManager.getConnection(conf.getProperty("authservice.db.url"), conf.getProperty("authservice.db.user"), conf.getProperty("authservice.db.password"));

            tc(() -> CONN.createStatement().execute("CREATE TABLE users (username VARCHAR(32) PRIMARY KEY, fullname VARCHAR(128), password VARCHAR(64), roles VARCAR(128), verified INT, teamsurl VARCHAR(256), email VARCHAR(64), regtok VARCHAR(64))"));
            tc(() -> CONN.createStatement().execute("CREATE TABLE groups (groupname VARCHAR(32) PRIMARY KEY, desc VARCHAR(1024), teamsurl VARCHAR(256), email VARCHAR(64))"));
            tc(() -> CONN.createStatement().execute("CREATE TABLE groupmap (groupname VARCHAR(32), username VARCHAR(32), PRIMARY KEY(groupname, username))"));

            tc(() -> _register(new User("mmp", "Madhusoodan Pataki", "m@123", true, "CREATE,DELETE,EDIT,UPVOTE,DOWNVOTE,COMMENT", "", "")));
            tc(() -> _register(new User("admin", "Admin", "a@123", true, "CREATE,DELETE,EDIT,UPVOTE,DOWNVOTE,COMMENT,ADMIN", "", "")));

            tc(() -> _createGroup(new Group(ADMIN, "admin group", "", "")));
            tc(() -> _createGroup(new Group(SECURITY, "security group to review content", "", "")));

            tc(() -> _addUserToGroup(ADMIN, "admin"));
            tc(() -> _addUserToGroup(SECURITY, "admin"));

        } catch (Exception e) {
            LOG.error("Error while initializing user group service", e);
        }
    }

    @Override
    public boolean authenticate(User cred) throws Exception {
        User c = getUser(cred.getUserName());
        if (c != null) {
            return c.isVerified() && c.getPassword().equals(cred.getPassword());
        }
        return false;
    }

    @Override
    public void validate(String uid, String tok) throws Exception {
        User u = getUser(uid);
        if (!u.__getRegTok().equals(tok)) {
            throw new Exception("token don't match, re-register");
        }
        PreparedStatement ps = CONN.prepareStatement("UPDATE users SET verified=1 WHERE username=?");
        ps.setString(1, uid);
        try {
            ps.executeUpdate();
            u.setVerified(true);
            users.get(uid).setVerified(true);
        } catch (Exception e) {
            LOG.error("Validation of token failed: " + uid + " [" + tok + "]", e);
            throw new Exception("Registration failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void _register(User cred) throws Exception {
        if (getUser(cred.getUserName()) != null) {
            throw new UserExistsException(cred.getUserName() + " already exists");
        }
        PreparedStatement ps = CONN.prepareStatement("INSERT INTO users (username, fullname, password, roles, verified, teamsurl, email, regtok) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        String tok = R.nextDouble() + "-" + R.nextInt();
        ps.setString(1, cred.getUserName());
        ps.setString(2, cred.getFullName());
        ps.setString(3, cred.getPassword());
        ps.setString(4, cred.getRoles());
        ps.setInt(5, cred.isVerified() ? 1 : 0);
        ps.setString(6, cred.getTeamsUrl());
        ps.setString(7, cred.getEmail());
        ps.setString(8, tok);
        try {
            ps.executeUpdate();
            users.put(cred.getUserName(), cred);
            cred.setRegTok(tok);
        } catch (Exception e) {
            LOG.error("Registration of " + cred.getUserName() + " failed", e);
            throw new Exception("Registration failed" + e.getMessage(), e); // mask the exception
        }
    }

    @Override
    public User getUser(String userName) throws Exception {
        User cred = users.get(userName);
        if (cred != null) {
            return cred.clone();
        }
        List<User> users = _getUsers(userName);
        return users.isEmpty() ? null : users.get(0);
    }

    @Override
    public boolean isAdmin(String userId) throws Exception {
        return getUser(userId).getRoles().contains("ADMIN");
    }

    @Override
    public List<User> getUsers() throws Exception {
        return _getUsers(null);
    }

    @Override
    public void _updateUser(String modifier, User u) throws Exception {
        PreparedStatement ps = CONN.prepareStatement("UPDATE users SET fullname = ?, password = ?, roles = ?, teamsurl = ?, email = ? WHERE username=?");
        ps.setString(1, u.getFullName());
        ps.setString(2, u.getPassword());
        ps.setString(3, u.getRoles());
        ps.setString(4, u.getTeamsUrl());
        ps.setString(5, u.getEmail());
        ps.setString(6, u.getUserName());
        ps.executeUpdate();
        users.put(u.getUserName(), u);
    }

    public List<User> _getUsers(String userName) throws Exception {
        PreparedStatement ps = CONN.prepareStatement(
                String.format(
                        "SELECT * FROM users %s",
                        userName == null ? "" : "WHERE username=?"
                )
        );
        if (userName != null) {
            ps.setString(1, userName);
        }
        boolean ok = ps.execute();
        if (!ok) {
            throw new NoSuchUserException(userName == null ? "" : userName);
        }
        List<User> users = new LinkedList<>();
        try {
            ResultSet rs = ps.getResultSet();
            while (rs.next()) {
                users.add(new User(
                        rs.getString("username"),
                        rs.getString("fullname"),
                        rs.getString("password"),
                        rs.getInt("verified") == 1,
                        rs.getString("roles"),
                        rs.getString("teamsurl"),
                        rs.getString("email"),
                        rs.getString("regtok")
                ));
            }
            return users;
        } catch (Exception e) {
            e.printStackTrace();
            throw new NoSuchUserException(userName);
        }
    }

    @Override
    protected void _addUserToGroup(String group, String user) throws Exception {
        PreparedStatement ps = CONN.prepareStatement("INSERT INTO groupmap (groupname, username) VALUES (?, ?)");
        ps.setString(1, group);
        ps.setString(2, user);
        try {
            ps.executeUpdate();
            groups.remove(group);
        } catch (Exception e) {
            LOG.error("Adding user to group failed: u:" + user + " g:" + group, e);
            throw new Exception("Group creation failed" + e.getMessage(), e); // mask the exception
        }
    }

    @Override
    protected void _createGroup(Group grp) throws Exception {
        PreparedStatement ps = CONN.prepareStatement("INSERT INTO groups (groupname, desc, teamsUrl, email) VALUES (?, ?, ?, ?)");
        ps.setString(1, grp.getGroupName());
        ps.setString(2, grp.getDesc());
        ps.setString(3, grp.getTeamsUrl());
        ps.setString(4, grp.getEmail());
        ps.executeUpdate();
        groups.put(grp.getGroupName(), grp);
    }

    @Override
    protected Group _getGroup(String groupName) throws Exception {
        if (groups.containsKey(groupName)) {
            return groups.get(groupName);
        }
        Group g = new Group();

        PreparedStatement ps1 = CONN.prepareStatement("SELECT groupname, desc, teamsurl, email FROM groups WHERE groupname = ?");
        ps1.setString(1, groupName);
        if (ps1.execute()) {
            ResultSet rs = ps1.getResultSet();
            if (!rs.next()) {
                throw new Exception("No such group");
            }
            g.setGroupName(rs.getString("groupname"));
            g.setDesc(rs.getString("desc"));
            g.setEmail(rs.getString("email"));
            g.setTeamsUrl(rs.getString("teamsurl"));
        }

        PreparedStatement ps = CONN.prepareStatement("SELECT groupname, username FROM groupmap WHERE groupname = ?");
        ps.setString(1, groupName);
        if (ps.execute()) {
            ResultSet rs = ps.getResultSet();
            while (rs.next()) {
                g.addUser(rs.getString("username"));
            }
            if (g.getUsers().isEmpty()) {
                throw new Exception("Group doesn't exist");
            }
            groups.put(groupName, g);
            return g;
        }
        return null;
    }

    @Override
    public List<String> getAllGroups() throws Exception {
        PreparedStatement ps = CONN.prepareStatement("SELECT groupname FROM groups");
        List<String> groups = new LinkedList<>();
        if (ps.execute()) {
            ResultSet rs = ps.getResultSet();
            while (rs.next()) {
                groups.add(rs.getString("groupname"));
            }
        }
        return groups;
    }

    @Override
    public List<String> getGroupsOf(String user) throws Exception {
        PreparedStatement ps = CONN.prepareStatement("SELECT groupname FROM groupmap WHERE username=?");
        ps.setString(1, user);
        List<String> groups = new LinkedList<>();
        if (ps.execute()) {
            ResultSet rs = ps.getResultSet();
            while (rs.next()) {
                groups.add(rs.getString("groupname"));
            }
        }
        return groups;
    }

    @Override
    public void _deleteUserFromGroup(String group, String user) throws Exception {
        PreparedStatement ps = CONN.prepareStatement("DELETE FROM groupmap WHERE groupname = ? AND username = ?");
        ps.setString(1, group);
        ps.setString(2, user);
        try {
            ps.executeUpdate();
            groups.remove(group);
        } catch (Exception e) {
            LOG.error("Deleting user from group failed: u:" + user + " g:" + group, e);
            throw new Exception("Deleting user from group failed" + e.getMessage(), e); // mask the exception
        }
    }

}
