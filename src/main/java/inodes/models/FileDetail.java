package inodes.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileDetail {
    String name, path;
    Long mtime;
    Long size;
}