package inodes.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import inodes.service.api.NotificationPayLoad;
import lombok.*;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity(name = "APP_NOTIFICATIONS")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AppNotification {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ndid")
    NotificationData data;

    private String nFor;

    private Boolean seen;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Entity(name = "APP_NOTIFICATION_DATA")
    public static class NotificationData implements NotificationPayLoad {

        @Id
        @GeneratedValue
        @JsonIgnore
        Long id;

        String nFrom;

        Long ptime = 0l;

        @JsonIgnore
        @OneToMany
        @JoinColumn(name = "ndid")
        Set<AppNotification> notif;

        @Column(length = 1024)
        String ntext;

    }

}
