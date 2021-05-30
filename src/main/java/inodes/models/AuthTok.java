package inodes.models;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;

@Entity
@IdClass(AuthTok.AuthTokId.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AuthTok {

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class AuthTokId implements Serializable {
        String uId, tok;
    }

    @Id
    String uId;

    @Id
    @Column(unique = true)
    String tok;

}
