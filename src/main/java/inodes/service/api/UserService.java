package inodes.service.api;

import inodes.Inodes;
import inodes.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public abstract class UserService extends Observable{

    @Autowired
    EmailService ES;

    @PostConstruct
    public void _init() {
        register("register", o -> {
            User u = (User) o;
            String url = String.format("http://%s/auth/validate/%s?tok=%s", Inodes.getLocalAddr(), u.getUser(), u.__getRegTok());

            ES.sendEmail(
                u.getEmail(),
                "Verify your account",
                String.format(
                    "Thanks for registering on inodes. <br/><br/><a href='%s'>Click here</a> to verify your inodes account, or open this url manually<br/>%s<br/><br/>Intial credentials<br/>%s / %s",
                    url, url, u.getUser(), u.getPassword()
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


    public abstract boolean isAdmin(String userId);

    public abstract List<User> getUsers() throws Exception;


    public void updateUser(String userId, User u) throws Exception {
        if(!userId.equals(u.getUser()) && !isAdmin(userId)) {
            throw new UnAuthorizedException("Unauthorized");
        }
        _updateUser(userId, u);
    }

    public abstract void _updateUser(String userId, User u) throws Exception;

    public static class User {
        String user;
        String fullName;
        String password;
        boolean verified;
        String roles;
        String teamsUrl;
        String email;
        String regTok;

        public User(String user, String fullName, String password, boolean verified, String roles, String teamsUrl, String email, String regTok) {
            this.user = user;
            this.fullName = fullName;
            this.password = password;
            this.verified = verified;
            this.roles = roles;
            this.teamsUrl = teamsUrl;
            this.email = email;
            this.regTok = regTok;
        }

        public String __getRegTok() {
            return regTok;
        }

        public void setRegTok(String regTok) {
            this.regTok = regTok;
        }

        public User(String user, String fullName, String password, boolean verified, String roles, String teamsUrl, String email) {
            this(user, fullName, password, verified, roles, teamsUrl, email, null);
        }

        public User(String user, String password) {
            this.user = user;
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

        User(){}

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
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
                    "user='" + user + '\'' +
                    ", password='" + password + '\'' +
                    '}';
        }
    }

}
