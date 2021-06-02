package inodes.service.api;

import inodes.models.AppNotification;
import inodes.repository.AppNotificationRepo;
import inodes.util.SecurityUtil;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Log4j
@Service
public class AppNotificationService {

    @Autowired
    AppNotificationRepo ANR;

    @Autowired
    UserGroupService UGS;

    @Autowired
    AuthorizationService AS;

    public List<AppNotification> getAllNotifs() {
        return StreamSupport.stream(ANR.findAll().spliterator(), false).collect(Collectors.toList());
    }

    public List<AppNotification> getNotificationsFor(String userId, int start, int size) throws Exception {
        List<String> grps = new ArrayList<>(UGS.getGroupsOf(userId)).stream().map(DataService::getGroupTag).collect(Collectors.toList());
        if (userId != null)
            grps.add(DataService.getUserTag(userId));
        return ANR.findByNForInOrderByData_PtimeDesc(grps, new PageRequest(start, size));
    }

    public void postNotification(List<String> ugids, AppNotification.NotificationData ndata) throws Exception {
        AS.checkNotificationSendPermission(ugids);
        ANR.save(ugids.stream().map(u -> AppNotification.builder().nFor(u).data(ndata).seen(false).build()).collect(Collectors.toList()));
    }

    public void markAsSeen(Long id) {
        AppNotification notification = ANR.findOne(id);
        notification.setSeen(true);
        ANR.save(notification);
    }

    public Integer getUnseenNotificationCount() throws Exception {
        String userId = SecurityUtil.getCurrentUser();
        List<String> grps = new ArrayList<>(UGS.getGroupsOf(userId)).stream().map(DataService::getGroupTag).collect(Collectors.toList());
        if (userId != null)
            grps.add(DataService.getUserTag(userId));
        return ANR.countByNForInAndSeenFalse(grps);
    }

}
