package inodes.models;

import inodes.service.api.UserGroupService;
import lombok.Builder;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.beans.Transient;

@Data
@Entity
@Builder
public class User implements Cloneable {
    @Id
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
    public String getRegTok() {
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

    User() {
    }
}
