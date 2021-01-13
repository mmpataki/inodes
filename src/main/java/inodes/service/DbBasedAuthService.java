package inodes.service;

import inodes.Configuration;
import inodes.service.api.AuthenticationService;
import inodes.service.api.NoSuchUserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class DbBasedAuthService implements AuthenticationService {

    @Autowired
    Configuration conf;

    Map<String, Credentials> users = new HashMap<>();
    Connection CONN;

    @PostConstruct
    void init() throws Exception {
        try {
            CONN = DriverManager.getConnection(conf.getProperty("authservice.db.url"), conf.getProperty("authservice.db.user"), conf.getProperty("authservice.db.password"));
            CONN.createStatement().execute("CREATE TABLE users (username VARCHAR(32) PRIMARY KEY, fullname VARCHAR(128), password VARCHAR(64), roles VARCHAR(128), verified INT)");
            register(new Credentials("mmp", "Madhusoodan Pataki", "m@123",true, "CREATE,DELETE,EDIT,UPVOTE,DOWNVOTE,COMMENT"));
            register(new Credentials("admin", "Admin", "a@123",true, "CREATE,DELETE,EDIT,UPVOTE,DOWNVOTE,COMMENT"));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public boolean authenticate(Credentials cred) throws Exception {
        Credentials c = getUser(cred.getUser());
        if (c != null) {
            return c.isVerified() && c.getPassword().equals(cred.getPassword());
        }
        return false;
    }

    @Override
    public void register(Credentials cred) throws Exception {
        cred.setRoles("");
        cred.setVerified(false);
        PreparedStatement ps = CONN.prepareStatement("INSERT INTO users (username, fullname, password, roles, verified) VALUES (?, ?, ?, ?, ?)");
        ps.setString(1, cred.getUser());
        ps.setString(2, cred.getFullName());
        ps.setString(3, cred.getPassword());
        ps.setString(4, cred.getRoles());
        ps.setInt(5, cred.isVerified() ? 1 : 0);
        try {
            ps.execute();
            users.put(cred.getUser(), cred);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Registration failed" + e.getMessage()); // mask the exception
        }
    }

    @Override
    public Credentials getUser(String userName) throws Exception {
        Credentials cred = users.get(userName);
        if (cred != null) {
            return cred;
        }
        PreparedStatement ps = CONN.prepareStatement("SELECT username, fullname, password, roles, verified FROM users WHERE username=?");
        ps.setString(0, userName);
        boolean ok = ps.execute();
        if (!ok) {
            throw new NoSuchUserException(userName);
        }
        try {
            ResultSet rs = ps.getResultSet();
            rs.next();
            return new Credentials(
                    rs.getString("username"),
                    rs.getString("fullname"),
                    rs.getString("password"),
                    rs.getInt("verified") == 1,
                    rs.getString("roles")
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new NoSuchUserException(userName);
        }
    }

    @Override
    public boolean isAdmin(String userId) {
        return false;
    }

    private String makeCredentialLine(Credentials cred) {
        return String.format("%s:%s:%s:%s", cred.getUser(), cred.getPassword(), cred.getRoles(), cred.isVerified());
    }
}
