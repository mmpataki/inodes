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

    @Autowired
    DataService DS;

    @Autowired
    KlassService KS;

    enum EventTypes {
        APPROVAL_NEEDED
    }

    @PostConstruct
    public void init() {

        Interceptor interceptor = o -> {
            Document doc = (Document) ((List)o).get(1);

            Klass klass = KS.getKlass(doc.getType());
            if (klass.isEditApprovalNeeded()) {
                doc.setNeedsApproval(true);
                doc.setSavedVisibility(doc.getVisibility());
                doc.setVisibility(Arrays.asList(DataService.getUserTag(doc.getOwner()), DataService.getGroupTag(UserGroupService.SECURITY)));
                notifyPostEvent(EventTypes.APPROVAL_NEEDED, doc);
            }
        };
        DS.registerPreEvent(DataService.ObservableEvents.NEW, interceptor);
        DS.registerPreEvent(DataService.ObservableEvents.UPDATE, interceptor);
    }

}
