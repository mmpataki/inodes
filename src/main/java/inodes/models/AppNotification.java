package inodes.models;

import inodes.service.api.NotificationPayLoad;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;

@Entity(name = "APP_NOTIFICATIONS")
@IdClass(AppNotification.NID.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AppNotification implements NotificationPayLoad {

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class NID implements Serializable {

        String nFor;

        String nFrom;

        Long ptime = 0l;

    }

    @Id
    String nFor;

    @Id
    String nFrom;

    @Id
    Long ptime = 0l;

    Boolean seen = false;

    @Column(length = 1024)
    String ntext;

}
