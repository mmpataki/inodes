package inodes.controllers;

import inodes.service.api.*;
import inodes.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class AdminController {

    @Autowired
    AdminService AS;

    @Autowired
    UserGroupService UGS;

    @DeleteMapping("/deleteall")
    public void deleteAll() throws Exception {
        if(!UGS.amIAdmin()) {
            throw new UnAuthorizedException("You are not authorized to do this action");
        }
        AS.deleteAll();
    }

    @PostMapping("/loadtrusted")
    public void loadTrusted() throws Exception {
        if(!UGS.amIAdmin()) {
            throw new UnAuthorizedException("You are not authorized to do this action");
        }
        AS.loadTrusted();
    }

    @PostMapping("/trust/{id}")
    public void trust(@PathVariable String id) throws Exception {
        if(!UGS.amIAdmin()) {
            throw new UnAuthorizedException("You are not authorized to do this action");
        }
        AS.trust(id);
    }

    @PostMapping("/backup")
    public void backup() throws Exception {
        if(!UGS.amIAdmin()) {
            throw new UnAuthorizedException("You are not authorized to do this action");
        }
        AS.backup();
    }

    @PostMapping("/restore")
    public void restore() throws Exception {
        if(!UGS.amIAdmin()) {
            throw new UnAuthorizedException("You are not authorized to do this action");
        }
        AS.restore();
    }

}
