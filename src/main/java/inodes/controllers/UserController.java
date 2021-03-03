package inodes.controllers;

import static inodes.util.TryCatchUtil.tc;

import inodes.models.Group;
import inodes.models.User;
import inodes.models.UserInfo;
import inodes.service.api.UserGroupService;
import inodes.service.api.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
public class UserController extends AuthenticatedController {

    @Autowired
    UserGroupService US;

    @Autowired
    DataService DS;

    @RequestMapping(value = "/auth/register", method = RequestMethod.POST)
    public void register(@RequestBody User cred) throws Exception {
        US.register(cred);
    }

    @RequestMapping(value = "/auth/validate/{uid}", method = RequestMethod.GET)
    public ResponseEntity<String> validate(@PathVariable("uid") String uid, @RequestParam("tok") String tok) throws Exception {
        US.validate(uid, tok);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/?q=hello");
        return new ResponseEntity<String>(headers, HttpStatus.FOUND);
    }

    @RequestMapping(value = "/auth/users", method = RequestMethod.GET)
    public List<String> getUsers(@ModelAttribute("loggedinuser") String user) throws Exception {
        return US.getUsers().stream().map(u -> u.getUserName()).collect(Collectors.toList());
    }

    @RequestMapping(value = "/auth/user/{uid}", method = RequestMethod.GET)
    public UserInfo getUser(@PathVariable String uid) throws Exception {
        UserInfo u = US.getUserInfo(uid);
        u.getBasic().setPassword(null);
        return u;
    }

    @RequestMapping(value = "/auth/user", method = RequestMethod.POST)
    public void updateUser(@RequestBody User user, @ModelAttribute("loggedinuser") String curUser) throws Exception {
        Objects.requireNonNull(curUser);
        US.updateUser(curUser, user);
    }

    @RequestMapping(value = "/auth/groups", method = RequestMethod.POST)
    public void addGroup(@RequestBody Group grp, @ModelAttribute("loggedinuser") String user) throws Exception {
        US.createGroup(user, grp);
        US.addUserToGroup(user, grp.getGroupName(), user);
    }

    @RequestMapping(value = "/auth/groups/{gname}/add", method = RequestMethod.POST)
    public void addUserToGroup(@PathVariable("gname") String group, @RequestParam("user") List<String> users, @ModelAttribute("loggedinuser") String curUser) throws Exception {
        for (String user : users) {
            US.addUserToGroup(curUser, group, user);
        }
    }

    @RequestMapping(value = "/auth/groups/{gname}/delete", method = RequestMethod.POST)
    public void deleteUserFromGroup(@PathVariable("gname") String group, @RequestParam("user") List<String> users, @ModelAttribute("loggedinuser") String curUser) throws Exception {
        for (String user : users) {
            US.deleteUserFromGroup(curUser, group, user);
        }
    }

    @RequestMapping(value = "/auth/groups/{gname}", method = RequestMethod.GET)
    public Group getGroup(@PathVariable("gname") String group) throws Exception {
        return US.getGroup(group);
    }

    @RequestMapping(value = "/auth/groups", method = RequestMethod.GET)
    public List<String> getGroupNames() throws Exception {
        return US.getAllGroups();
    }

}
