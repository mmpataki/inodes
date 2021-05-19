package inodes.service.api;

import inodes.models.AppNotification;
import inodes.repository.AppNotificationRepo;
import inodes.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppNotificationService {

    @Autowired
    AppNotificationRepo ANR;

    @Autowired
    UserGroupService UGS;

    @Autowired
    AuthorizationService AS;


    public List<AppNotification> getNotificationsFor(String userId, int start, int size) throws Exception {
        List<String> grps = new ArrayList<>(UGS.getGroupsOf(userId)).stream().map(DataService::getGroupTag).collect(Collectors.toList());
        if(userId != null)
            grps.add(DataService.getUserTag(userId));
        return ANR.findByNForInOrderByPtimeDesc(grps, new PageRequest(start, size));
    }

    public void postNotification(AppNotification notification) throws Exception {
        AS.checkNotificationSendPermission(notification);
        notification.setSeen(false);
        ANR.save(notification);
    }

    public void markAsSeen(String nFor, String nFrom, long ptime) {
        AppNotification notification = ANR.findOne(AppNotification.NID.builder().nFrom(nFrom).nFor(nFor).ptime(ptime).build());
        notification.setSeen(true);
        ANR.save(notification);
    }

    public Integer getUnseenNotificationCount() throws Exception {
        String userId = SecurityUtil.getCurrentUser();
        List<String> grps = new ArrayList<>(UGS.getGroupsOf(userId)).stream().map(DataService::getGroupTag).collect(Collectors.toList());
        if(userId != null)
            grps.add(DataService.getUserTag(userId));
        return ANR.countByNForInAndSeenFalse(grps);
    }

    public void deleteNotification(String nFor, String nFrom, long ptime) {
        ANR.delete(AppNotification.NID.builder().nFor(nFor).nFrom(nFrom).ptime(ptime).build());
    }
}
