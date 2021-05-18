package inodes.controllers;

import inodes.models.AppNotification;
import inodes.service.api.AppNotificationService;
import inodes.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class AppNotificationController {

    @Autowired
    AppNotificationService ANS;

    @GetMapping("")
    public List<AppNotification> getMyNotifications(
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "10") Integer size) throws Exception {
        return ANS.getNotificationsFor(SecurityUtil.getCurrentUser(), offset, size);
    }

    @GetMapping("/unseen")
    public Integer getUnseenNotificationCount() {
        return ANS.getUnseenNotificationCount(SecurityUtil.getCurrentUser());
    }

    @PostMapping("/markasseen")
    public void markAsSeen(@RequestParam("for") String nFor,
                           @RequestParam("from") String nFrom,
                           @RequestParam("ptime") long ptime) {
        ANS.markAsSeen(nFor, nFrom, ptime);
    }

    @DeleteMapping("/delete")
    public void deleteNotification(@RequestParam("for") String nFor,
                           @RequestParam("from") String nFrom,
                           @RequestParam("ptime") long ptime) {
        ANS.deleteNotification(nFor, nFrom, ptime);
    }

}
