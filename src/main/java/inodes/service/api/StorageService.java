package inodes.service.api;

import inodes.models.FileDetail;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public interface StorageService {

    String store(String user, MultipartFile file) throws Exception;

    String store(String user, String fileName, InputStream inputStream) throws Exception;

    List<FileDetail> loadAll(String user) throws IOException, Exception;

    Path load(String filename);

    Resource loadAsResource(String filename) throws Exception;

    void delete(String user, String fileName);

    void deleteAll();

}