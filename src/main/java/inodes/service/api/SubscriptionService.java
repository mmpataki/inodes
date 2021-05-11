package inodes.service.api;

import inodes.models.Subscription;
import inodes.models.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SubscriptionService extends Observable {

    @Autowired
    AuthorizationService AS;

    @Autowired
    UserGroupService US;

    enum EventType {
        NEW_SUBSCRIPTION
    }

    @PostConstruct
    public void init() {
        US.registerPostEvent(UserGroupService.Events.USER_SEARCH, ed -> {
            UserInfo u = (UserInfo) ed.get("userInfo");
            u.addExtraInfo("direct_subscriptions", getSubscribers(Subscription.SubscriberType.USER, u.getBasic().getUserName()));
            Map<String, List<Subscription>> gSubscriptions = new HashMap<>();
            US.getGroupsOf(u.getBasic().getUserName()).forEach(gid -> {
                gSubscriptions.put(gid, getSubscribers(Subscription.SubscriberType.GROUP, gid));
            });
            u.addExtraInfo("group_subscriptions", gSubscriptions);
        });
    }

    public void subscribe(
            Subscription.SubscriberType subscriberType, String subscriberId,
            Subscription.SubscribedObjectType objTyp, String objId,
            Subscription.Event event) throws Exception {

        AS.checkSubscribePermission(subscriberType, subscriberId);

        Subscription s = Subscription.builder()
                .subscriberType(subscriberType).subscriberId(subscriberId)
                .objType(objTyp).objid(objId)
                .event(event)
                .lastUpdTime(System.currentTimeMillis())
                .build();
        notifyPreEvent(EventType.NEW_SUBSCRIPTION, EventData.of("subscription", s));
        _saveSubscription(s);
        notifyPostEvent(EventType.NEW_SUBSCRIPTION, EventData.of("subscription", s));
    }

    protected abstract void _saveSubscription(Subscription subscription);

    public abstract List<Subscription> getSubscriptions();

    public abstract List<Subscription> getSubscribers(Subscription.SubscribedObjectType objType, String objid);

    public abstract List<Subscription> getSubscribers(Subscription.SubscriberType subsciberTyp, String subscriberId);

    public abstract List<Subscription> getSubscriptions(Subscription.SubscribedObjectType objType, String objid, Subscription.SubscriberType subsciberTyp, String subscriberId);
}
