package inodes.controllers;

import static inodes.service.api.AuthenticationService.*;

import inodes.models.UserInfo;
import inodes.service.api.AuthenticationService;
import inodes.service.api.DataService;
import inodes.service.api.UnAuthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
public class UserController extends AuthenticatedController {

    @Autowired
    AuthenticationService AS;

    @Autowired
    DataService DS;

    @RequestMapping(value = "/auth/register", method = RequestMethod.POST)
    public void register(@RequestBody User cred) throws Exception {
        AS.register(cred);
    }

    @RequestMapping(value = "/auth/users", method = RequestMethod.GET)
    public List<UserInfo> getUsers(@ModelAttribute("loggedinuser") String user) throws Exception {
        if(!user.equals("admin")) {
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

    UserInfo getMoreInfo(User u) {
        UserInfo fui = new UserInfo(u);
        try {
            fui.setPostsCount(DS.getUserPostsFacets(u.getUser()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fui;
    }
}
