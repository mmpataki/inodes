package inodes.controllers;

import static inodes.service.api.AuthenticationService.*;

import inodes.service.api.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Random;

@RestController
@CrossOrigin
public class UserController {

    @Autowired
    AuthenticationService AS;

    Random R = new Random();

    @RequestMapping(value = "/auth/login", method = RequestMethod.POST)
    public void login(@RequestBody Credentials cred, HttpServletResponse response) throws Exception {
        if(AS.authenticate(cred)) {
            response.setHeader("User", cred.getUser());
        }
    }

    @RequestMapping(value = "/auth/logout", method = RequestMethod.POST)
    public void logout() {

    }

    @RequestMapping(value = "/auth/register", method = RequestMethod.POST)
    public void register(@RequestBody Credentials cred) {
        cred.setRoles("NORMAL");
        cred.setVerified(false);
        AS.register(cred);
    }
}
