package inodes.models;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;

@Entity(name = "SUBSCRIPTIONS")
@IdClass(Subscription.SID.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Subscription {

    public enum Event {
        NEW_DOC,
        DOC_UPDATE,
        USER_ADDED,
        GROUP_ADDED,
        ALL
    }

    public enum SubscriberType {
        USER,
        GROUP
    }


    public enum SubscribedObjectType {
        TAG,
        DOC
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class SID implements Serializable {
        SubscribedObjectType objType;
        String objid;
        SubscriberType subscriberType;
        String subscriberId;
        Event event;
    }

    @Id
    SubscribedObjectType objType;
    @Id
    String objid;
    @Id
    SubscriberType subscriberType;
    @Id
    String subscriberId;
    @Id
    Event event;

    String state;

    Long lastUpdTime;
}
