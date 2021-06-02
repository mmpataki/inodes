package inodes.controllers;

import inodes.models.AppNotification;
import inodes.service.api.AppNotificationService;
import inodes.util.SecurityUtil;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Log4j
@RestController
@RequestMapping("/notifications")
public class AppNotificationController {

    @Autowired
    AppNotificationService ANS;

    @GetMapping("/all")
    public List<AppNotification> getAllNotifs() {
        return ANS.getAllNotifs();
    }

    @GetMapping("")
    public List<AppNotification> getMyNotifications(
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "10") Integer size) throws Exception {
        return ANS.getNotificationsFor(SecurityUtil.getCurrentUser(), offset, size);
    }

    @PostMapping("")
    public void postNotification(@RequestParam("ugids") ArrayList<String> ugids, @RequestParam("txt") String txt) throws Exception {
        AppNotification.NotificationData ndata = AppNotification.NotificationData.builder().nFrom(SecurityUtil.getCurrentUser()).ntext(txt).ptime(System.currentTimeMillis()).build();
        ANS.postNotification(ugids, ndata);
    }

    @GetMapping("/unseen")
    public Integer getUnseenNotificationCount() throws Exception {
        return ANS.getUnseenNotificationCount();
    }

    @PostMapping("/markasseen")
    public void markAsSeen(@RequestParam("id") Long id) {
        ANS.markAsSeen(id);
    }

}
