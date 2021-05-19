package inodes.controllers;

import inodes.models.AppNotification;
import inodes.service.api.AppNotificationService;
import inodes.util.SecurityUtil;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Log4j
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

    @PostMapping("")
    public void postNotification(@RequestParam("ugids") ArrayList<String> ugids, @RequestParam("txt") String txt) throws Exception {
        for (String ugid : ugids) {
            AppNotification nnotif = AppNotification.builder().nFrom(SecurityUtil.getCurrentUser()).nFor(ugid).ntext(txt).ptime(System.currentTimeMillis()).build();
            try {
                ANS.postNotification(nnotif);
            } catch (Exception e) {
                log.error("error while sending notifications", e);
            }
        }
    }

    @GetMapping("/unseen")
    public Integer getUnseenNotificationCount() throws Exception {
        return ANS.getUnseenNotificationCount();
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
