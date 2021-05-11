package inodes.controllers;

import inodes.models.Group;
import inodes.models.User;
import inodes.models.UserInfo;
import inodes.service.api.UserGroupService;
import inodes.service.api.DataService;
import inodes.util.SecurityUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
public class UserController {

    @Autowired
    UserGroupService US;

    @Autowired
    DataService DS;

    @PostMapping("/auth/register")
    public void register(@RequestBody User cred) throws Exception {
        US.register(cred);
    }

    @GetMapping("/auth/validate/{uid}")
    public ResponseEntity<String> validate(@PathVariable("uid") String uid, @RequestParam("tok") String tok) throws Exception {
        US.validate(uid, tok);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/?q=hello");
        return new ResponseEntity<String>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/auth/users")
    public List<String> getUsers() throws Exception {
        return US.getUsers().stream().map(u -> u.getUserName()).collect(Collectors.toList());
    }

    @GetMapping("/auth/user/{uid}")
    public UserInfo getUser(@PathVariable String uid) throws Exception {
        UserInfo u = US.getUserInfo(uid);
        u.getBasic().setPassword(null);
        return u;
    }

    @PostMapping("/auth/user")
    public void updateUser(@RequestBody User user) throws Exception {
        US.updateUser(user);
    }

    @PostMapping("/auth/groups")
    public void addGroup(@RequestBody Group grp) throws Exception {
        US.createGroup(grp);
    }

    @PostMapping("/auth/groups/{gname}/add")
    public void addUserToGroup(@PathVariable("gname") String group, @RequestParam("user") List<String> users) throws Exception {
        for (String user : users) {
            US.addUserToGroup(group, user);
        }
    }

    @PostMapping("/auth/groups/{gname}/delete")
    public void deleteUserFromGroup(@PathVariable("gname") String group, @RequestParam("user") List<String> users) throws Exception {
        for (String user : users) {
            US.deleteUserFromGroup(group, user);
        }
    }

    @GetMapping("/auth/groups/{gname}")
    public Group getGroup(@PathVariable("gname") String group) throws Exception {
        return US.getGroup(group);
    }

    @GetMapping("/auth/groups")
    public List<String> getGroupNames() throws Exception {
        return US.getAllGroups();
    }

    @Data
    static class AutoCompleteResult {
        String name;
        String id;

        public AutoCompleteResult(String name, String id) {
            this.name = name;
            this.id = id;
        }
    }

    @GetMapping("/auth/find-ug-like")
    public List<AutoCompleteResult> getUserGroupSuggestions(@RequestParam("term") String sQuery) {
        List<AutoCompleteResult> ret = new LinkedList<>();
        US.findUsersLike(stripQ(sQuery)).stream().map(u -> new AutoCompleteResult(u.getFullName(), DataService.getUserTag(u.getUserName()))).forEach(ret::add);
        US.findGroupsLike(stripQ(sQuery)).stream().map(g -> new AutoCompleteResult(g, DataService.getGroupTag(g))).forEach(ret::add);
        return ret;
    }

    private String stripQ(String s) {
        if(s.length() > 2 && s.charAt(1) == ':')
            s = s.substring(2);
        return s;
    }

}
