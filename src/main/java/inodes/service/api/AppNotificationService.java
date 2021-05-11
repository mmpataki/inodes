package inodes.service.api;

import inodes.models.AppNotification;
import inodes.repository.AppNotificationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AppNotificationService {

    @Autowired
    AppNotificationRepo ANR;

    @Autowired
    UserGroupService UGS;


    public List<AppNotification> getNotificationsFor(String userId, int start, int size) throws Exception {
        List<String> grps = new ArrayList<>(UGS.getGroupsOf(userId));
        if(userId != null)
            grps.add(userId);
        return ANR.findByNForInOrderByPtimeDesc(grps, new PageRequest(start, size));
    }

    public void postNotification(AppNotification notification) {
        ANR.save(notification);
    }

}
