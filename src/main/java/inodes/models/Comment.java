package inodes.models;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;

@Entity
@IdClass(Comment.CID.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Comment {

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class CID implements Serializable {
        String postid;
        String user;
        long time;
    }

    @Id
    String postid;
    @Id
    @Column(name = "user")
    String user;
    @Id
    @Column(name = "itime")
    long time;

    public String comment;

}
