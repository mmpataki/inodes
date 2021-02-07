package inodes.service.api;

import inodes.Inodes;
import inodes.service.EmailService;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.beans.Transient;
import java.sql.SQLException;
import java.util.*;

@Service
public abstract class UserGroupService extends Observable {

    Logger LOG = LoggerFactory.getLogger(UserGroupService.class);

    @Autowired
    EmailService ES;

    @PostConstruct
    public void _init() {
        register("register", o -> {
            User u = (User) o;
            String url = String.format("http://%s/auth/validate/%s?tok=%s", Inodes.getLocalAddr(), u.getUserName(), u.__getRegTok());

            ES.sendEmail(
                    Collections.singleton(u.getEmail()),
                    "Verify your account",
                    String.format(
                            "Thanks for registering on inodes. <br/><br/><a href='%s'>Click here</a> to verify your inodes account, or open this url manually<br/>%s<br/><br/>Intial credentials<br/>%s / %s",
                            url, url, u.getUserName(), u.getPassword()
                    )
            );
        });
    }

    public abstract boolean authenticate(User cred) throws Exception;

    public abstract void validate(String uid, String tok) throws Exception;

    public void register(User cred) throws Exception {
        cred.setRoles("UPVOTE,DOWNVOTE,COMMENT");
        cred.setVerified(false);
        _register(cred);
        notifyObservers("register", cred);
    }

    public abstract void _register(User cred) throws Exception;

    public abstract User getUser(String userName) throws Exception;

    public abstract boolean isAdmin(String userId) throws Exception;

    public abstract List<User> getUsers() throws Exception;

    public void updateUser(String modifier, User u) throws Exception {
        if (!modifier.equals(u.getUserName()) && !isAdmin(modifier)) {
            throw new UnAuthorizedException("Unauthorized");
        }
        User modifierUser = getUser(modifier);
        if (u.getRoles() != null) {
            for (String role : u.getRoles().split(",")) {
                if (!modifierUser.getRoles().contains(role)) {
                    throw new UnAuthorizedException("You are not authorized to modify roles of this user");
                }
            }
        }

        User origUser = getUser(u.getUserName());
        for (String role : origUser.getRoles().split(",")) {
            if (!modifierUser.getRoles().contains(role)) {
                throw new UnAuthorizedException("You are not authorized to modify roles of this user");
            }
        }

        // this origUser is cached, so don't update it right now, let implementation do it.
        User copyUser = origUser.clone();

        if (u.getRoles() != null)
            copyUser.setRoles(u.getRoles());

        if (isAdmin(modifier) || modifier.equals(u.getUserName())) {
            copyUser.setFullName(u.getFullName());
            copyUser.setEmail(u.getEmail());
            copyUser.setTeamsUrl(u.getTeamsUrl());
            if (u.getPassword() != null && !u.getPassword().isEmpty())
                copyUser.setPassword(u.getPassword());
        }

        _updateUser(modifier, copyUser);
    }

    public abstract void _updateUser(String modifier, User u) throws Exception;


    public static final String SECURITY = "security";
    public static final String ADMIN = "admin";

    @Autowired
    AuthorizationService AS;

    public abstract List<String> getAllGroups() throws Exception;

    public abstract List<String> getGroupsOf(String user) throws SQLException, Exception;

    public Group getGroup(String groupName) throws Exception {
        return _getGroup(groupName);
    }

    public void createGroup(String user, Group grp) throws Exception {
        AS.checkGroupCreationPermissions(user);
        _createGroup(grp);
    }

    public void addUserToGroup(String curUser, String group, String user) throws Exception {
        AS.checkAddUserToGroupPermission(curUser, group);
        _addUserToGroup(group, user);
    }

    public void deleteUserFromGroup(String curUser, String group, String user) throws Exception {
        AS.checkDeleteUserFromGroupPermission(curUser, group);
        _deleteUserFromGroup(group, user);
    }

    protected abstract void _addUserToGroup(String group, String user) throws Exception;

    protected abstract void _createGroup(Group grp) throws Exception;

    protected abstract Group _getGroup(String groupName) throws Exception;

    public abstract void _deleteUserFromGroup(String group, String user) throws SQLException, Exception;

    @Data
    public static class User implements Cloneable {
        String userName;
        String fullName;
        String password;
        boolean verified;
        String roles;
        String teamsUrl;
        String email;
        String regTok;

        public User(String userName, String fullName, String password, boolean verified, String roles, String teamsUrl, String email, String regTok) {
            this.userName = userName;
            this.fullName = fullName;
            this.password = password;
            this.verified = verified;
            this.roles = roles;
            this.teamsUrl = teamsUrl;
            this.email = email;
            this.regTok = regTok;
        }

        @Override
        public User clone() throws CloneNotSupportedException {
            return (User) super.clone();
        }

        public String __getRegTok() {
            return regTok;
        }

        @Transient
        public String getRegTok() { return regTok; }

        public void setRegTok(String regTok) {
            this.regTok = regTok;
        }

        public User(String userName, String fullName, String password, boolean verified, String roles, String teamsUrl, String email) {
            this(userName, fullName, password, verified, roles, teamsUrl, email, null);
        }

        public User(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }
    }

    @Data
    public static class Group implements Cloneable {
        String groupName;
        String desc;
        String teamsUrl;
        String email;

        // transient (not stored with group record)
        Set<String> users = new HashSet<>();

        public Group() {
        }

        public Group(String groupName, String desc, String teamsUrl, String email) {
            this.groupName = groupName;
            this.desc = desc;
            this.teamsUrl = teamsUrl;
            this.email = email;
        }

        public void addUser(String username) {
            users.add(username);
        }
    }
}
