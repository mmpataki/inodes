package inodes.service.api;

import com.google.gson.Gson;
import inodes.Inodes;
import inodes.models.Document;
import inodes.models.Group;
import inodes.models.Subscription;
import inodes.models.User;
import inodes.repository.EventQueue;
import inodes.service.EmailService;
import inodes.service.TeamsNotificationSenderService;
import inodes.util.UrlUtil;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EventService {

    @Autowired
    CollabService CS;

    @Autowired
    DataService DS;

    @Autowired
    UserGroupService US;

    @Autowired
    EmailService ES;

    @Autowired
    TeamsNotificationSenderService TS;

    @Autowired
    SecurityService SS;

    @Autowired
    SubscriptionService SUS;

    @Autowired
    EventQueue EQ;

    static EventService es;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @With
    @Builder
    static class Reciepient {
        String id;
        RecipientType typ;
    }

    enum RecipientType {
        USER, GROUP
    }

    interface RecipientResolver {
        Set<Reciepient> getUsers(Object o) throws Exception;
    }

    interface EventToEmailNotificationTransformer {
        EmailService.EmailObject transform(Object payload);
    }

    interface EventToTeamsNotificationTransformer {
        TeamsNotificationSenderService.TeamsNotification transform(Object payload);
    }

    enum Type {

        REGISTER_USER
                (es.getUserResolver(), es.getRegisterEmailTemplateBuilder(), null),

        USER_ADD_TO_GROUP
                (es.getUserResolver(), es.getUserAddedToGroupEmailBuilder(), null),

        NEW_DOC
                (es.getDocWatcherResolver(), es.getNewDocEmailBuilder(), es.getNewDocTeamsNBuilder()),

        NEW_COMMENT
                (es.getNewCommentWatcherResolver(), es.getNewCommentEmailBuilder(), null),

        ADMIN
                (o -> Collections.singleton(new Reciepient().withId(UserGroupService.ADMIN).withTyp(RecipientType.GROUP)), null, null),

        APPROVAL_NEEDED
                (o -> Collections.singleton(new Reciepient().withId(UserGroupService.SECURITY).withTyp(RecipientType.GROUP)), es.getApprovalNeededEmailBuilder(), null),

        NEW_SUBSCRIPTION
                (es.getNewSubscriptionRecptResolver(), es.getNewSubscriptionEmailBuilder(), null)

        ;

        RecipientResolver ur;
        EventToEmailNotificationTransformer eet;
        EventToTeamsNotificationTransformer ett;

        Type(RecipientResolver ur, EventToEmailNotificationTransformer eet, EventToTeamsNotificationTransformer ett) {
            this.ur = ur;
            this.eet = eet;
            this.ett = ett;
        }

        Set<Reciepient> resolveRecipients(Object payload) throws Exception {
            return this.ur.getUsers(payload);
        }

        public TeamsNotificationSenderService.TeamsNotification getTeamsNotificationPayload(Object payLoad) {
            if (ett != null)
                return ett.transform(payLoad);
            return null;
        }

        public EmailService.EmailObject getEmailPayload(Object payLoad) {
            if (eet != null)
                return eet.transform(payLoad);
            return null;
        }
    }

    @Data
    @Builder
    public static class Event {
        Type typ;
        Object payLoad;
    }

    interface EventToPayloadTransformer {
        NotificationPayLoad generate(Event e);
    }


    public enum NotificationType {

        EMAIL(e -> e.getTyp().getEmailPayload(e.getPayLoad())),
        TEAMS_NOTIFICATION(e -> e.getTyp().getTeamsNotificationPayload(e.getPayLoad()));

        private final EventToPayloadTransformer pg;

        NotificationType(EventToPayloadTransformer pg) {
            this.pg = pg;
        }

        public NotificationPayLoad getPayload(Event e) {
            return pg.generate(e);
        }
    }

    @With
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Notification {
        String channelInfo;
        NotificationType typ;
        Event event;
    }

    private RecipientResolver getNewCommentWatcherResolver() {
        return o -> {
            List<String> chunks = (List<String>) o;
            String comment = (new Gson()).fromJson(chunks.get(2), String.class);
            Document d = DS.get("admin", chunks.get(1));
            Set<Reciepient> rcpnts = new HashSet<>();
            rcpnts.add(new Reciepient().withId(d.getOwner()).withTyp(RecipientType.USER));
            Matcher m = Pattern.compile("\\@([0-9A-Za-z]+)").matcher(comment);
            while (m.find()) {
                String id = m.group(1);
                if (id.startsWith("g:")) {
                    rcpnts.add(new Reciepient().withId(id.substring(2)).withTyp(RecipientType.GROUP));
                } else {
                    rcpnts.add(new Reciepient().withId(id).withTyp(RecipientType.USER));
                }
            }
            return rcpnts;
        };
    }

    private RecipientResolver getDocWatcherResolver() {
        return o -> {
            Document doc = (Document) o;
            Set<Reciepient> users = new HashSet<>();
            doc.getTags().forEach(tag -> {
                SUS.getSubscribers(Subscription.SubscribedObjectType.TAG, tag).forEach(s -> {
                    users.add(
                            Reciepient.builder()
                                    .id(s.getSubscriberId())
                                    .typ(s.getSubscriberType() == Subscription.SubscriberType.USER ? RecipientType.USER : RecipientType.GROUP)
                                    .build()
                    );
                });
            });
            SUS.getSubscribers(Subscription.SubscribedObjectType.DOC, doc.getId()).forEach(s -> {
                users.add(
                        Reciepient.builder()
                                .id(s.getSubscriberId())
                                .typ(s.getSubscriberType() == Subscription.SubscriberType.USER ? RecipientType.USER : RecipientType.GROUP)
                                .build()
                );
            });
            return users;
        };
    }

    private RecipientResolver getNewSubscriptionRecptResolver() {
        return o -> {
            Subscription s = (Subscription) o;
            return Collections.singleton(
                new Reciepient()
                    .withTyp(s.getSubscriberType() == Subscription.SubscriberType.USER ? RecipientType.USER : RecipientType.GROUP)
                    .withId(s.getSubscriberId())
            );
        };
    }

    private RecipientResolver getUserResolver() {
        return o -> Collections.singleton(Reciepient.builder().id(((User) o).getUserName()).typ(RecipientType.USER).build());
    }

    private EventToEmailNotificationTransformer getRegisterEmailTemplateBuilder() {
        return o -> {
            User u = (User) o;
            String url = String.format("%s/auth/validate/%s?tok=%s", Inodes.getLocalAddr(), u.getUserName(), u.__getRegTok());
            return new EmailService.EmailObject()
                    .withSubject("Verify your account")
                    .withBody(
                            String.format(
                                    "Thanks for registering on inodes. <br/><br/><a href='%s'>Click here</a> to verify your inodes account, or open this url manually<br/>%s<br/><br/>Intial credentials<br/>%s / %s",
                                    url, url, u.getUserName(), u.getPassword()
                            )
                    );
        };
    }


    private EventToTeamsNotificationTransformer getNewDocTeamsNBuilder() {
        return o -> {
            Document doc = (Document) o;
            return new TeamsNotificationSenderService.TeamsNotification()
                    .withBody(
                        String.format(
                            "**New node** published in the tags you follow \n %s \n **Link** : <a href='%s'>%s</a>",
                                doc.getTags().toString(),
                                UrlUtil.getDocUrl(doc.getId()), UrlUtil.getDocUrl(doc.getId())
                        )
                    );
        };
    }

    private EventToEmailNotificationTransformer getNewDocEmailBuilder() {
        return o -> {
            Document doc = (Document) o;
            return new EmailService.EmailObject()
                    .withSubject("New node published in the tags you follow")
                    .withBody(
                            String.format(
                                    "<b>New node</b> published in the tags you follow <br/> <i>%s</i> <br/> <b>Link</b> : <a href='%s'>%s</a>",
                                    doc.getTags().toString(),
                                    UrlUtil.getDocUrl(doc.getId()), UrlUtil.getDocUrl(doc.getId())
                            )
                    );
        };
    }


    private EventToEmailNotificationTransformer getUserAddedToGroupEmailBuilder() {
        return o -> {
            List<String> ll = (List<String>) o;
            String adder = ll.get(0);
            String group = ll.get(2);
            return new EmailService.EmailObject()
                    .withSubject(String.format("%s added you to %s", adder, group))
                    .withBody(
                            String.format(
                                    "Congratualations! <a href='%s'>%s</a> added you to group <a href='%s'>%s</a>",
                                    UrlUtil.getUserUrl(adder), adder, UrlUtil.getGroupUrl(group), group
                            )
                    );
        };
    }

    private EventToEmailNotificationTransformer getApprovalNeededEmailBuilder() {
        return o -> new EmailService.EmailObject().withSubject("New applet alert").withBody(
                String.format("New <a href='%s'>applet alert</a>", UrlUtil.getDocUrl(((Document) o).getId())));
    }

    private EventToEmailNotificationTransformer getNewCommentEmailBuilder() {
        return o -> {
            List<String> ll = (List<String>) o;
            String user = ll.get(0);
            String id = ll.get(1);
            String comment = ll.get(2);

            return new EmailService.EmailObject()
                    .withSubject(user + " commented on your post")
                    .withBody(String.format(
                            "<div style='padding: 10px; border: solid 1px skyblue; display: block'>" +
                                    "<a href='%s'>%s</a> commented on your <a href='%s'>post</a></br>" +
                                    "<div style='padding: 10px; background-color: #f2f2f2; border: solid 1px gray; display: block'>%s</div>" +
                                    "</div>",
                            UrlUtil.getUserUrl(user), user, UrlUtil.getDocUrl(id), comment)
                    );
        };
    }

    private EventToEmailNotificationTransformer getNewSubscriptionEmailBuilder() {
        return o -> {
            Subscription s = (Subscription) o;
            return new EmailService.EmailObject()
                    .withSubject("inodes - subscription")
                    .withBody(String.format("You have subscribed for %s <a href='%s'>%s</a> for %s event", s.getObjType(), UrlUtil.getDocUrl(s.getObjid()), UrlUtil.getDocUrl(s.getObjid()), s.getEvent()));
        };
    }

    public void post(Type typ, Object content) {
        System.out.printf("New event: %s [%s]\n", typ.toString(), content.toString());
        EQ.enqueue(Event.builder().typ(typ).payLoad(content).build());
    }

    @PostConstruct
    public void init() {

        es = this;

        DS.registerPostEvent(DataService.ObservableEvents.NEW, o -> post(Type.NEW_DOC, o));
        SS.registerPostEvent(SecurityService.EventTypes.APPROVAL_NEEDED, o -> post(Type.APPROVAL_NEEDED, o));
        US.registerPostEvent(UserGroupService.Events.USER_ADDED_TO_GROUP, o -> post(EventService.Type.USER_ADD_TO_GROUP, o));
        CS.registerPostEvent(CollabService.EventType.NEW_COMMENT, o -> post(Type.NEW_COMMENT, o));
        SUS.registerPostEvent(SubscriptionService.EventType.NEW_SUBSCRIPTION, o -> post(Type.NEW_SUBSCRIPTION, o));

        /* start the event processing thread */
        new Thread(() -> {

            while (true) {

                try {

                    Event e = EQ.deque();
                    e.getTyp()
                            .resolveRecipients(e.getPayLoad())
                            .stream()
                            .flatMap(r -> {
                                List<Notification> notifications = new LinkedList<>();
                                if (r.getTyp() == RecipientType.GROUP) {
                                    try {
                                        notifications.addAll(getNotifications(e, US.getGroup(r.getId())));
                                    } catch (Exception exception) {
                                        exception.printStackTrace();
                                    }
                                } else {
                                    try {
                                        notifications.addAll(getNotifications(e, US.getUser(r.getId())));
                                    } catch (Exception exception) {
                                        exception.printStackTrace();
                                    }
                                }
                                return notifications.stream();
                            })
                            .forEach(n -> {
                                switch (n.getTyp()) {
                                    case EMAIL:
                                        try {
                                            ES.send(n.getChannelInfo(), (EmailService.EmailObject) n.getTyp().getPayload(n.getEvent()));
                                        } catch (Throwable exception) {
                                            exception.printStackTrace();
                                        }
                                        break;
                                    case TEAMS_NOTIFICATION:
                                        try {
                                            TS.send(n.getChannelInfo(), (TeamsNotificationSenderService.TeamsNotification) n.getTyp().getPayload(n.getEvent()));
                                        } catch (Throwable exception) {
                                            exception.printStackTrace();
                                        }
                                        break;
                                }
                            });

                } catch (Throwable t) {
                    t.printStackTrace();
                }

            }

        }).start();
    }

    private List<? extends Notification> getNotifications(Event e, Group grp) {
        if (grp == null) return Collections.EMPTY_LIST;
        List<Notification> notifications = new LinkedList<>();
        if (grp.getEmail() != null && !grp.getEmail().isEmpty())
            notifications.add(new Notification().withChannelInfo(grp.getEmail()).withTyp(NotificationType.EMAIL).withEvent(e));
        if (grp.getTeamsUrl() != null && !grp.getTeamsUrl().isEmpty())
            notifications.add(new Notification().withChannelInfo(grp.getTeamsUrl()).withTyp(NotificationType.TEAMS_NOTIFICATION).withEvent(e));
        return notifications;
    }

    private List<? extends Notification> getNotifications(Event e, User user) {
        if (user == null) return Collections.EMPTY_LIST;
        List<Notification> notifications = new LinkedList<>();
        if (user.getEmail() != null && !user.getEmail().isEmpty())
            notifications.add(new Notification().withChannelInfo(user.getEmail()).withTyp(NotificationType.EMAIL).withEvent(e));
        if (user.getTeamsUrl() != null && !user.getTeamsUrl().isEmpty())
            notifications.add(new Notification().withChannelInfo(user.getTeamsUrl()).withTyp(NotificationType.TEAMS_NOTIFICATION).withEvent(e));
        return notifications;
    }

}
