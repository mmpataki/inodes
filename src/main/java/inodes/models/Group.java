package inodes.models;

import lombok.Data;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Entity(name = "GROUPS")
public class Group implements Cloneable {
    @Id
    String groupName;
    String description;
    String teamsUrl;
    String email;

    // transient (not stored with group record)
    @ManyToMany(fetch = FetchType.EAGER)
    Set<User> users = new HashSet<>();

    public Group() {
    }

    public Group(String groupName, String description, String teamsUrl, String email) {
        this.groupName = groupName;
        this.description = description;
        this.teamsUrl = teamsUrl;
        this.email = email;
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void deleteUser(String user) {
        users.removeIf(u -> u.getUserName().equals(user));
    }

    public List<String> getUsers() {
        return users.stream().map(x -> x.getUserName()).collect(Collectors.toList());
    }
}
