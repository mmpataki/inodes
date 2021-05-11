package inodes.controllers;

import inodes.models.FileDetail;
import inodes.service.api.StorageService;
import inodes.util.SecurityUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
public class FileUploadController {

    @Autowired
    StorageService storageService;

    @GetMapping("/allmyfiles")
    public List<FileDetail> listUploadedFiles() throws Exception {
        String user = SecurityUtil.getCurrentUser();
        if (user == null || user.isEmpty())
            return Collections.emptyList();
        return storageService.loadAll(user);
    }

    @DeleteMapping("/files")
    public void deleteFile(@RequestParam String file) throws Exception {
        storageService.delete(file);
    }

    @PostMapping("/files")
    public String handleFileUpload(
            @RequestParam("file") MultipartFile file) throws Exception {
        return storageService.store(SecurityUtil.getCurrentUser(), file);
    }

    @PostMapping("/files/download")
    public String handleFileDownload(
            @RequestBody FileSaveRequest req) throws Exception {
        URL url = new URL(req.getUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(req.getMethod());
        if(req.getHeaders() != null) {
            req.getHeaders().entrySet().forEach(e -> connection.setRequestProperty(e.getKey(), e.getValue()));
        }
        if(req.getData() != null) {
            connection.setDoOutput(true);
            connection.getOutputStream().write(req.getData().getBytes());
        }

        InputStream in = connection.getInputStream();
        return storageService.store(SecurityUtil.getCurrentUser(), req.fileName, in);
    }

    @Data
    public static class FileSaveRequest {
        String method, url, data, fileName;
        Map<String, String> headers;
    }

}
