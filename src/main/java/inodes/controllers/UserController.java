package inodes.controllers;

import static inodes.service.api.UserService.*;

import inodes.models.UserInfo;
import inodes.service.api.UserService;
import inodes.service.api.DataService;
import inodes.service.api.UnAuthorizedException;
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
    UserService AS;

    @Autowired
    DataService DS;

    @RequestMapping(value = "/auth/register", method = RequestMethod.POST)
    public void register(@RequestBody User cred) throws Exception {
        AS.register(cred);
    }

    @RequestMapping(value = "/auth/validate/{uid}", method = RequestMethod.GET)
    public ResponseEntity<String> validate(@PathVariable("uid") String uid, @RequestParam("tok") String tok) throws Exception {
        AS.validate(uid, tok);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/?q=hello");
        return new ResponseEntity<String>(headers, HttpStatus.FOUND);
    }

    @RequestMapping(value = "/auth/users", method = RequestMethod.GET)
    public List<UserInfo> getUsers(@ModelAttribute("loggedinuser") String user) throws Exception {
        if (!user.equals("admin")) {
            throw new UnAuthorizedException("Unauthorized");
        }
        return AS.getUsers().stream().map(u -> getMoreInfo(u)).collect(Collectors.toList());
    }

    @RequestMapping(value = "/auth/user/{uid}", method = RequestMethod.GET)
    public UserInfo getUser(@PathVariable String uid) throws Exception {
        User u = AS.getUser(uid);
        u.setPassword(null);
        return getMoreInfo(u);
    }

    @RequestMapping(value = "/auth/user", method = RequestMethod.POST)
    public void updateUser(@RequestBody User user, @ModelAttribute("loggedinuser") String curUser) throws Exception {
        Objects.requireNonNull(curUser);
        AS.updateUser(curUser, user);
    }

    UserInfo getMoreInfo(User u) {
        UserInfo fui = new UserInfo(u);
        try {
            fui.setPostsCount(DS.getUserPostsFacets(u.getUserName()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fui;
    }
}
