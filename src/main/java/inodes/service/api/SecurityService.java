package inodes.service.api;

import inodes.models.Document;
import inodes.models.Klass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Service
public class SecurityService extends Observable {

    public static final String PERM_NEEDED = "\"-perm-needed-\"";

    @Autowired
    DataService DS;

    @Autowired
    KlassService KS;

    @Autowired
    UserGroupService UG;

    enum EventTypes {
        APPROVAL_NEEDED
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
                if (!klass.isPermissionNeeded() || doc.getVisibility().contains(DataService.getUserTag(user)))
                    continue;
                boolean hasPerm = false;
                for (String grp : UG.getGroupsOf(user)) {
                    if (doc.getVisibility().contains(DataService.getGroupTag(grp))) {
                        hasPerm = true;
                        break;
                    }
                }
                if (!hasPerm)
                    doc.setContent(PERM_NEEDED);
            }
        };

        DS.registerPreEvent(DataService.ObservableEvents.NEW, interceptor);
        DS.registerPreEvent(DataService.ObservableEvents.UPDATE, interceptor);
        DS.registerPostEvent(DataService.ObservableEvents.SEARCH, permissionIntereptor);
    }

}
