package inodes.service.api;

import java.util.List;

public abstract class SubscriptionService {

    enum Event {
        NEW_DOC,
        DOC_UPDATE,
        USER_ADDED,
        GROUP_ADDED,
        ALL
    }

    enum SubscriberType {
        USER,
        GROUP
    }

    public static class Subscription {
        String subscriberId;
        String docid;
        SubscriberType typ;
        Event evnt;
        String state;
        long lastUpdTime;

        public Subscription(String subscriberId, String docid, SubscriberType typ, Event evnt, String state, long lastUpdTime) {
            this.subscriberId = subscriberId;
            this.docid = docid;
            this.typ = typ;
            this.evnt = evnt;
            this.state = state;
            this.lastUpdTime = lastUpdTime;
        }
    }

    public void subscribeUser(String user, String id, Event evnt) {
        _saveSubscription(new Subscription(user, id, SubscriberType.USER, evnt, "", System.currentTimeMillis()));
    }

    public void subscribeGroup(String user, String id, Event evnt) {
        _saveSubscription(new Subscription(user, id, SubscriberType.GROUP, evnt, "", System.currentTimeMillis()));
    }

    protected abstract void _saveSubscription(Subscription subscription);

    protected abstract List<Subscription> getSubscribers(String id);

}
