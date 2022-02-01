package inodes.service.api;

import inodes.models.Document;
import inodes.models.Klass;
import inodes.models.PermissionRequest;
import inodes.repository.PermissionRequestsRepo;
import inodes.util.SecurityUtil;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Log4j
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
    VersionControlService VCS;

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

        Interceptor approvalIntereptor = ed -> {
            List<Document> docs = (List<Document>) ed.get("results");
            String user = ed.getPublisher();
            List<Document> retSet = docs.stream().map(doc -> {
                if (!doc.isNeedsApproval() || (user != null && user.equals(doc.getOwner())))
                    return doc;
                try {
                    List<VersionControlService.DocEdit> edits = VCS.getHistoryOf(doc.getId()).getEdits();
                    Collections.sort(edits);
                    if(edits.isEmpty())
                        return doc;
                    for (int i = edits.size() - 1; i >= 0; i--) {
                        VersionControlService.DocEdit edit = edits.get(i);
                        Document vdoc = VCS.getDocWithVersion(doc.getId(), edit.getMtime(), edit.getAuthor());
                        if (!vdoc.isNeedsApproval() || user.equals(vdoc.getOwner()))
                            return vdoc;
                    }
                    return null;
                } catch (Exception e) {
                    log.error("failed to get the document history");
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            docs.clear();
            docs.addAll(retSet);
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
                    if (getPermReqByFor(user, doc.getId()) != null)
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
        DS.registerPostEvent(DataService.ObservableEvents.SEARCH, approvalIntereptor);
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
        if (doc == null) {
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
