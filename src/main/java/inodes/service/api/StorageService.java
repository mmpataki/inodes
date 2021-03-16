package inodes.service.api;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

    String store(String user, MultipartFile file) throws Exception;

    Stream<Path> loadAll(String user) throws IOException, Exception;

    Path load(String filename);

    Resource loadAsResource(String filename) throws Exception;

    void deleteAll();

}