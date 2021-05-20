package inodes.models;

import lombok.*;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;
import java.util.List;

@Entity(name = "PERM_REQUESTS")
@IdClass(PermissionRequest.PRID.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PermissionRequest {

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class PRID implements Serializable {

        String reqBy;

        String docId;

    }

    @Id
    String reqBy;

    @Id
    String docId;

    Long reqTime;

    @ElementCollection
    List<String> reqTo;
}
