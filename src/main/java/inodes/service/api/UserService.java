package inodes.service.api;

import inodes.Inodes;
import inodes.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Service
public abstract class UserService extends Observable {

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

        public String getTeamsUrl() {
            return teamsUrl;
        }

        public void setTeamsUrl(String teamsUrl) {
            this.teamsUrl = teamsUrl;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public boolean isVerified() {
            return verified;
        }

        public void setVerified(boolean verified) {
            this.verified = verified;
        }

        public String getRoles() {
            return roles;
        }

        public void setRoles(String roles) {
            this.roles = roles;
        }

        User() {
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String user) {
            this.userName = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        public String toString() {
            return "Credentials{" +
                    "user='" + userName + '\'' +
                    ", password='" + password + '\'' +
                    '}';
        }
    }

}
