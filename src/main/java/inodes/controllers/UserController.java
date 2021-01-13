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

    @RequestMapping(value = "/auth/register", method = RequestMethod.POST)
    public void register(@RequestBody Credentials cred) throws Exception {
        AS.register(cred);
    }
}
