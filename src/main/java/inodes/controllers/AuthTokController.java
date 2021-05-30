package inodes.controllers;

import inodes.models.AuthTok;
import inodes.service.AuthTokService;
import inodes.service.api.UnAuthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/authtok")
public class AuthTokController {

    @Autowired
    AuthTokService ATS;

    @PostMapping("")
    public AuthTok getNewTok() throws UnAuthorizedException {
        return ATS.getNewAuthTok();
    }

    @GetMapping("/verify")
    public Boolean verify(String usr, String tok) {
        return ATS.verify(usr, tok);
    }

}
