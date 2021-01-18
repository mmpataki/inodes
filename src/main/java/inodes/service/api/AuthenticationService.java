package inodes.service.api;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AuthenticationService {

    boolean authenticate(User cred) throws Exception;

    void register(User cred) throws Exception;

    User getUser(String userName) throws Exception;



    boolean isAdmin(String userId);

    List<User> getUsers() throws Exception;

    class User {
        String user;
        String fullName;
        String password;
        boolean verified;
        String roles;

        public User(String user, String fullName, String password, boolean verified, String roles) {
            this.user = user;
            this.fullName = fullName;
            this.password = password;
            this.verified = verified;
            this.roles = roles;
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
