package inodes.controllers;

import inodes.models.AppNotification;
import inodes.service.api.AppNotificationService;
import inodes.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AppNotificationController {

    @Autowired
    AppNotificationService ANS;

    @GetMapping("/notifications")
    public List<AppNotification> getMyNotifications(
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "10") Integer size) throws Exception {
        return ANS.getNotificationsFor(SecurityUtil.getCurrentUser(), offset, size);
    }

}
