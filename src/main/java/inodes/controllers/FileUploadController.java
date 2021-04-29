package inodes.controllers;

import inodes.service.api.StorageService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class FileUploadController extends AuthenticatedController {

    @Autowired
    StorageService storageService;

    @Data
    @AllArgsConstructor
    public class FileDetail {
        String name;
        Long mtime;
        Long size;
    }

    @GetMapping("/allmyfiles")
    public List<FileDetail> listUploadedFiles(@ModelAttribute("loggedinuser") String user) throws Exception {
        if (user == null || user.isEmpty())
            return Collections.emptyList();
        return storageService
                .loadAll(user)
                .map(path -> path.toFile())
                .map(f -> new FileDetail(f.getName(), f.lastModified(), f.length()))
                .collect(Collectors.toList());
    }

    @GetMapping("/files")
    public ResponseEntity<Resource> serveFile(@RequestParam("file") String filename) throws Exception {
        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().body(file);
    }

    @PostMapping("/files")
    public String handleFileUpload(
            @ModelAttribute("loggedinuser") String user,
            @RequestParam("file") MultipartFile file) throws Exception {
        return storageService.store(user, file);
    }

}
