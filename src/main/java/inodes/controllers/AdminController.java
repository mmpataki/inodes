package inodes.controllers;

import inodes.service.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class AdminController extends AuthenticatedController {

    @Autowired
    AdminService AS;

    @Autowired
    UserGroupService UGS;

    @DeleteMapping("/deleteall")
    public void deleteAll(@ModelAttribute("loggedinuser") String user) throws Exception {
        if(!UGS.isAdmin(user)) {
            throw new UnAuthorizedException("You are not authorized to do this action");
        }
        AS.deleteAll();
    }

    @PostMapping("/loadtrusted")
    public void loadTrusted(@ModelAttribute("loggedinuser") String user) throws Exception {
        if(!UGS.isAdmin(user)) {
            throw new UnAuthorizedException("You are not authorized to do this action");
        }
        AS.loadTrusted();
    }

    @PostMapping("/trust/{id}")
    public void trust(@ModelAttribute("loggedinuser") String user, @PathVariable String id) throws Exception {
        if(!UGS.isAdmin(user)) {
            throw new UnAuthorizedException("You are not authorized to do this action");
        }
        AS.trust(id);
    }

    @PostMapping("/backup")
    public void backup(@ModelAttribute("loggedinuser") String user) throws Exception {
        if(!UGS.isAdmin(user)) {
            throw new UnAuthorizedException("You are not authorized to do this action");
        }
        AS.backup();
    }

    @PostMapping("/restore")
    public void restore(@ModelAttribute("loggedinuser") String user) throws Exception {
        if(!UGS.isAdmin(user)) {
            throw new UnAuthorizedException("You are not authorized to do this action");
        }
        AS.restore();
    }

}
