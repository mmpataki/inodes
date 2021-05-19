package inodes.service.api;

import com.google.gson.Gson;
import inodes.Inodes;
import inodes.models.*;
import inodes.repository.EventQueue;
import inodes.service.EmailService;
import inodes.service.TeamsNotificationSenderService;
import inodes.util.SecurityUtil;
import inodes.util.UrlUtil;
import lombok.*;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Log4j
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

    @Autowired
    AppNotificationService ANS;

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
        Set<Reciepient> getUsers(EventData o) throws Exception;
    }

    interface EventToEmailNotificationTransformer {
        EmailService.EmailObject transform(EventData payload);
    }

    interface EventToTeamsNotificationTransformer {
        TeamsNotificationSenderService.TeamsNotification transform(EventData payload);
    }

    interface EventToAppNotificationTransfomer {
        AppNotification transform(EventData payload);
    }

    enum Type {

        REGISTER_USER
                (es.getUserResolver(), es.getRegisterEmailTemplateBuilder(), null, null),

        USER_ADD_TO_GROUP
                (es.getUserResolver(), es.getUserAddedToGroupEmailBuilder(), null, null),

        NEW_DOC
                (es.getDocWatcherResolver(), es.getNewDocEmailBuilder(), es.getNewDocTeamsNBuilder(), null),

        NEW_COMMENT
                (es.getNewCommentWatcherResolver(), es.getNewCommentEmailBuilder(), null, null),

        ADMIN
                (o -> Collections.singleton(new Reciepient().withId(UserGroupService.ADMIN).withTyp(RecipientType.GROUP)), null, null, null),

        APPROVAL_NEEDED
                (o -> Collections.singleton(new Reciepient().withId(UserGroupService.SECURITY).withTyp(RecipientType.GROUP)), es.getApprovalNeededEmailBuilder(), null, null),

        PERMISSION_NEEDED
                (es.getPermissionProviderResolver(), null, null, es.getPermissionNeededAppNotificationBuilder()),

        PERMISSION_GIVEN
                (es.getPermissionGivenWatcherResolver(), null, null, es.getPermissionGivenAppNotificationBuilder()),

        NEW_SUBSCRIPTION
                (es.getNewSubscriptionRecptResolver(), es.getNewSubscriptionEmailBuilder(), null, null);

        RecipientResolver ur;
        EventToEmailNotificationTransformer eet;
        EventToTeamsNotificationTransformer ett;
        EventToAppNotificationTransfomer eat;

        Type(RecipientResolver ur, EventToEmailNotificationTransformer eet, EventToTeamsNotificationTransformer ett, EventToAppNotificationTransfomer eat) {
            this.ur = ur;
            this.eet = eet;
            this.ett = ett;
            this.eat = eat;
        }

        Set<Reciepient> resolveRecipients(EventData payload) throws Exception {
            return this.ur.getUsers(payload);
        }

        public TeamsNotificationSenderService.TeamsNotification getTeamsNotificationPayload(EventData payLoad) {
            if (ett != null)
                return ett.transform(payLoad);
            return null;
        }

        public EmailService.EmailObject getEmailPayload(EventData payLoad) {
            if (eet != null)
                return eet.transform(payLoad);
            return null;
        }

        public AppNotification getAppNotificationPayload(EventData payLoad) {
            if (eat != null)
                return eat.transform(payLoad);
            return null;
        }
    }

    @Data
    @Builder
    public static class Event {
        Type typ;
        EventData payLoad;
    }

    interface EventToPayloadTransformer {
        NotificationPayLoad generate(Event e);
    }


    public enum NotificationType {

        EMAIL(e -> e.getTyp().getEmailPayload(e.getPayLoad())),
        TEAMS_NOTIFICATION(e -> e.getTyp().getTeamsNotificationPayload(e.getPayLoad())),
        APP_NOTIFICATION(e -> e.getTyp().getAppNotificationPayload(e.getPayLoad()));

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


    private RecipientResolver getPermissionProviderResolver() {
        return o -> {
            Set<Reciepient> reciepients = new HashSet<>();
            for (String owner : (List<String>) o.get("currentOwners")) {
                String id;
                if ((id = DataService.getUFromUtag(owner)) != null) {
                    reciepients.add(Reciepient.builder().typ(RecipientType.USER).id(id).build());
                } else if ((id = DataService.getGFromGtag(owner)) != null) {
                    reciepients.add(Reciepient.builder().typ(RecipientType.GROUP).id(id).build());
                }
            }
            return reciepients;
        };
    }

    private RecipientResolver getPermissionGivenWatcherResolver() {
        return o -> {
            return Collections.singleton(Reciepient.builder().typ(RecipientType.USER).id((String) o.get("userId")).build());
        };
    }

    private RecipientResolver getNewCommentWatcherResolver() {
        return o -> {
            Set<Reciepient> rcpnts = new HashSet<>();
            try {
                SecurityUtil.setCurrentUser(o.getPublisher());
                String comment = (new Gson()).fromJson((String) o.get("comment"), String.class);
                Document d = DS.get((String) o.get("docid"));
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
            } finally {
                SecurityUtil.unsetCurrentUser();
            }
            return rcpnts;
        };
    }

    private RecipientResolver getDocWatcherResolver() {
        return o -> {
            Document doc = (Document) o.get("doc");
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
            Subscription s = (Subscription) o.get("subscription");
            return Collections.singleton(
                    new Reciepient()
                            .withTyp(s.getSubscriberType() == Subscription.SubscriberType.USER ? RecipientType.USER : RecipientType.GROUP)
                            .withId(s.getSubscriberId())
            );
        };
    }

    private RecipientResolver getUserResolver() {
        return o -> Collections.singleton(Reciepient.builder().id(((User) o.get("user")).getUserName()).typ(RecipientType.USER).build());
    }

    private EventToEmailNotificationTransformer getRegisterEmailTemplateBuilder() {
        return ed -> {
            User u = (User) ed.get("user");
            String url = String.format("%s/auth/validate/%s?tok=%s", Inodes.getLocalAddr(), u.getUserName(), u.__getRegTok());
            return new EmailService.EmailObject()
                    .withSubject("Verify your account")
                    .withBody(
                            String.format(
                                    "Thanks for registering on inodes. <br/><br/><a href='%s'>Click here</a> to verify your inodes account, or open this url manually<br/>%s<br/><br/>Your username<br/>%s",
                                    url, url, u.getUserName()
                            )
                    );
        };
    }

    private EventToTeamsNotificationTransformer getNewDocTeamsNBuilder() {
        return ed -> {
            Document doc = (Document) ed.get("docid");
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

    private EventToAppNotificationTransfomer getPermissionNeededAppNotificationBuilder() {
        return ed -> {
            String aGuy = (String) ed.get("for");
            String docId = (String) ed.get("docId");
            return AppNotification.builder()
                    .nFrom("permission service")
                    .nFor("")
                    .ntext(String.format(
                            "<a href=\"%s\"><b>%s</b></a> wants permission to access <a href=\"%s\" target=\"_blank\">this</a> object. " +
                                    "Click <a href=\"#\" onclick=\"post(`%s`).then(x => showSuccess('Done')).catch(e => showError(e.msg))\"><b>this</b></a> link, if you want to approve this request",
                            UrlUtil.getUserUrl(aGuy), aGuy, UrlUtil.getDocUrl(docId), UrlUtil.getDocPermApprovalLink(docId, aGuy)
                    ))
                    .ptime(System.currentTimeMillis())
                    .build();
        };
    }

    private EventToAppNotificationTransfomer getPermissionGivenAppNotificationBuilder() {
        return ed -> {
            String docId = (String) ed.get("docId");
            return AppNotification.builder()
                    .nFrom("permission service")
                    .nFor("")
                    .ntext(String.format(
                            "Your permission request for <a href=\"%s\">this</a> object is approved. Try accessing it now",
                            UrlUtil.getDocUrl(docId)
                    ))
                    .ptime(System.currentTimeMillis())
                    .build();
        };
    }

    private EventToEmailNotificationTransformer getNewDocEmailBuilder() {
        return ed -> {
            Document doc = (Document) ed.get("docid");
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
        return ed -> {
            String adder = (String) ed.getPublisher();
            String group = (String) ed.get("group");
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
        return ed -> new EmailService.EmailObject().withSubject("New applet alert").withBody(
                String.format("New <a href='%s'>applet alert</a>", UrlUtil.getDocUrl(((Document) ed.get("docid")).getId())));
    }

    private EventToEmailNotificationTransformer getNewCommentEmailBuilder() {
        return ed -> {
            String user = (String) ed.getPublisher();
            String id = (String) ed.get("docid");
            String comment = (String) ed.get("comment");

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
            Subscription s = (Subscription) o.get("subscription");
            return new EmailService.EmailObject()
                    .withSubject("inodes - subscription")
                    .withBody(String.format("You have subscribed for %s <a href='%s'>%s</a> for %s event", s.getObjType(), UrlUtil.getDocUrl(s.getObjid()), UrlUtil.getDocUrl(s.getObjid()), s.getEvent()));
        };
    }

    public void post(Type typ, EventData content) {
        EQ.enqueue(Event.builder().typ(typ).payLoad(content).build());
    }

    @PostConstruct
    public void init() {

        es = this;

        DS.registerPostEvent(DataService.ObservableEvents.NEW, o -> post(Type.NEW_DOC, o));
        DS.registerPostEvent(DataService.ObservableEvents.PERMISSION_NEEDED, o -> post(Type.PERMISSION_NEEDED, o));
        DS.registerPostEvent(DataService.ObservableEvents.PERMISSION_GIVEN, o -> post(Type.PERMISSION_GIVEN, o));
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
                                        log.error("error while creating group notifications", exception);
                                    }
                                } else {
                                    try {
                                        notifications.addAll(getNotifications(e, US.getUser(r.getId())));
                                    } catch (Exception exception) {
                                        log.error("error while creating user notifications", exception);
                                    }
                                }
                                return notifications.stream();
                            })
                            .forEach(n -> {
                                switch (n.getTyp()) {
                                    case EMAIL:
                                        try {
                                            ES.send(n.getChannelInfo(), (EmailService.EmailObject) n.getTyp().getPayload(n.getEvent()));
                                        } catch (Throwable ex) {
                                            log.error("error while sending email notifications", ex);
                                        }
                                        break;
                                    case TEAMS_NOTIFICATION:
                                        try {
                                            TS.send(n.getChannelInfo(), (TeamsNotificationSenderService.TeamsNotification) n.getTyp().getPayload(n.getEvent()));
                                        } catch (Throwable ex) {
                                            log.error("error while sending teams notifications", ex);
                                        }
                                        break;
                                    case APP_NOTIFICATION:
                                        try {
                                            AppNotification payload = (AppNotification) n.getTyp().getPayload(n.getEvent());
                                            payload.setNFor(n.getChannelInfo());
                                            ANS.postNotification(payload);
                                        } catch (Throwable t) {
                                            log.error("Error while sending app notifications: ", t);
                                        }
                                }
                            });

                } catch (Throwable t) {
                    log.error("error in notification sender thread: ", t);
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
        notifications.add(new Notification().withChannelInfo(DataService.getGroupTag(grp.getGroupName())).withTyp(NotificationType.APP_NOTIFICATION).withEvent(e));
        return notifications;
    }

    private List<? extends Notification> getNotifications(Event e, User user) {
        if (user == null) return Collections.EMPTY_LIST;
        List<Notification> notifications = new LinkedList<>();
        if (user.getEmail() != null && !user.getEmail().isEmpty())
            notifications.add(new Notification().withChannelInfo(user.getEmail()).withTyp(NotificationType.EMAIL).withEvent(e));
        if (user.getTeamsUrl() != null && !user.getTeamsUrl().isEmpty())
            notifications.add(new Notification().withChannelInfo(user.getTeamsUrl()).withTyp(NotificationType.TEAMS_NOTIFICATION).withEvent(e));
        notifications.add(new Notification().withChannelInfo(DataService.getUFromUtag(user.getUserName())).withTyp(NotificationType.APP_NOTIFICATION).withEvent(e));
        return notifications;
    }

}
