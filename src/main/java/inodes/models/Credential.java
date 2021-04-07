package inodes.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Credential {
    String userName;
    String password;
}
