package inodes.service;

import inodes.Configuration;
import inodes.models.FileDetail;
import inodes.service.api.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService extends WebMvcConfigurerAdapter implements StorageService {

    private Path rootLocation;

    @Autowired
    Configuration conf;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/u/**").addResourceLocations("file:" + conf.getProperty("storageservice.root.dir"));
    }

    @PostConstruct
    public void xinit() throws Exception {
        this.rootLocation = Paths.get(conf.getProperty("storageservice.root.dir"));
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new Exception("Could not initialize storage", e);
        }
    }

    @Override
    public String store(String user, MultipartFile file) throws Exception {
        try {
            if (file.isEmpty()) {
                throw new Exception("Failed to store empty file.");
            }

            Path userDir = rootLocation.resolve(user);
            if(!Files.exists(userDir))
                Files.createDirectories(userDir);

            Path destinationFile = userDir.resolve(Paths.get(file.getOriginalFilename()))
                    .normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(userDir.toAbsolutePath())) {
                throw new Exception("Cannot store file outside current directory.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return String.format("%s/%s", user, file.getOriginalFilename());
        } catch (Exception e) {
            throw new Exception("Failed to store file.", e);
        }
    }

    @Override
    public List<FileDetail> loadAll(String user) throws Exception {
        return Arrays.stream(new File(this.rootLocation + File.separator + user).listFiles())
                .map(f -> FileDetail.builder()
                            .name(f.getName())
                            .mtime(f.lastModified())
                            .size(f.length())
                            .path(String.format("/u/files/%s/%s", user, f.getName()))
                            .build()
                )
                .collect(Collectors.toList());
    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) throws Exception {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("Could not read file: " + filename);
            }
        } catch (MalformedURLException | FileNotFoundException e) {
            throw new Exception("Could not read file: " + filename, e);
        }
    }

    @Override
    public void delete(String user, String fileName) {
        System.out.println("fileName = " + fileName);
        System.out.println(rootLocation.resolve(user).resolve(fileName).toFile().getAbsolutePath());
        rootLocation.resolve(user).resolve(fileName).toFile().delete();
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

}
