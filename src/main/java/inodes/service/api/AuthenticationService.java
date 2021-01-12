package inodes.service.api;

import org.springframework.stereotype.Service;

@Service
public interface AuthenticationService {

    boolean authenticate(Credentials cred);

    String register(Credentials cred);

    Credentials getUser(String userName);

    boolean isAdmin(String userId);

    public static class Credentials {
        String user;
        String password;
        boolean verified;
        String roles;

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

        Credentials(){}

        public Credentials(String user, String password, String roles, boolean verified) {
            this.user = user;
            this.password = password;
            this.roles = roles;
            this.verified = verified;
        }

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
