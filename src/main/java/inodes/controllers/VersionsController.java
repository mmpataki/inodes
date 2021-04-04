package inodes.controllers;

import inodes.models.Document;
import inodes.service.api.VersionControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VersionsController {

    @Autowired
    VersionControlService VCS;

    @GetMapping("/versionsof/{id}")
    public VersionControlService.DocHistory getVersionsOf(@PathVariable String id) throws Exception {
        return VCS.getHistoryOf(id);
    }

    @GetMapping("/docwithversion")
    public Document getDocWithVersion(String id, Long time, String user) throws Exception {
        return VCS.getDocWithVersion(id, time, user);
    }

}
