package inodes.service.api;

import inodes.models.Document;
import inodes.models.Klass;
import inodes.models.PermissionRequest;
import inodes.repository.PermissionRequestsRepo;
import inodes.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Service
public class SecurityService extends Observable {

    @Autowired
    DataService DS;

    @Autowired
    KlassService KS;

    @Autowired
    UserGroupService UG;

    @Autowired
    PermissionRequestsRepo PRR;

    @Autowired
    AuthorizationService AS;

    enum EventTypes {
        APPROVAL_NEEDED,
        PERMISSION_NEEDED,
        PERMISSION_GIVEN;
    }

    @PostConstruct
    public void init() {

        Interceptor interceptor = ed -> {
            Document doc = (Document) ed.get("doc");
            Klass klass = KS.getKlass(doc.getType());
            if (klass.isEditApprovalNeeded()) {
                doc.setNeedsApproval(true);
                doc.setSavedVisibility(doc.getVisibility());
                doc.setVisibility(Arrays.asList(DataService.getUserTag(doc.getOwner()), DataService.getGroupTag(UserGroupService.SECURITY)));
                notifyPostEvent(EventTypes.APPROVAL_NEEDED, EventData.of("doc", doc));
            }
        };

        /* if user has no permissions on this object, he won't be able to access the content
         * but can see it in search result. So that he can ask permission.
         */
        Interceptor permissionIntereptor = ed -> {
            List<Document> docs = (List<Document>) ed.get("results");
            String user = ed.getPublisher();
            for (Document doc : docs) {
                Klass klass = KS.getKlass(doc.getType());
                if (!klass.isPermissionNeeded() || doc.getVisibility().contains(DataService.getUserTag(user))) {
                    doc.setCanRead();
                    continue;
                }
                boolean hasPerm = false;
                for (String grp : UG.getGroupsOf(user)) {
                    if (doc.getVisibility().contains(DataService.getGroupTag(grp))) {
                        hasPerm = true;
                        break;
                    }
                }
                if (!hasPerm) {
                    if(getPermReqByFor(user, doc.getId()) != null)
                        doc.setPermRequested();
                    else
                        doc.setPermNeeded();
                    doc.setContent("");
                } else {
                    doc.setCanRead();
                }
            }
        };

        DS.registerPreEvent(DataService.ObservableEvents.NEW, interceptor);
        DS.registerPreEvent(DataService.ObservableEvents.UPDATE, interceptor);
        DS.registerPostEvent(DataService.ObservableEvents.SEARCH, permissionIntereptor);
    }

    public List<PermissionRequest> getPermRequests() {
        return PRR.findByReqBy(SecurityUtil.getCurrentUser());
    }

    public PermissionRequest getPermReqByFor(String ugid, String docId) {
        return PRR.findOne(PermissionRequest.PRID.builder().reqBy(ugid).docId(docId).build());
    }

    public void askPermission(String docId) throws Exception {
        Document doc = DS.get(docId);
        if(doc == null) {
            throw new UnAuthorizedException("no such document is visible for you");
        }
        PermissionRequest preq = PermissionRequest.builder()
                .reqTo(doc.getVisibility()).reqBy(SecurityUtil.getCurrentUser()).docId(docId).build();
        notifyPreEvent(EventTypes.PERMISSION_NEEDED, EventData.of("req", preq));
        preq.setReqTime(System.currentTimeMillis());
        PRR.save(preq);
        notifyPostEvent(EventTypes.PERMISSION_NEEDED, EventData.of("req", preq));
    }

    public void givePermission(String docId, String userid) throws Exception {
        Document doc = DS.get(docId);
        if (!doc.canRead()) {
            throw new UnAuthorizedException("You don't have permissions on the object to delegate them");
        }
        notifyPreEvent(EventTypes.PERMISSION_GIVEN, EventData.of("docId", docId, "userId", userid));
        doc.getVisibility().add(DataService.getUserTag(userid));
        DS._putData(doc);
        PRR.delete(PermissionRequest.PRID.builder().reqBy(userid).docId(docId).build());
        notifyPostEvent(EventTypes.PERMISSION_GIVEN, EventData.of("docId", docId, "userId", userid));
    }

}
