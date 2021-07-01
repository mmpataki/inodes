package inodes.controllers;

import inodes.models.AppNotification;
import inodes.service.api.AppNotificationService;
import inodes.service.api.UnAuthorizedException;
import inodes.util.SecurityUtil;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

    @GetMapping("/unseen")
    public Integer getUnseenNotificationCount() throws Exception {
        return ANS.getUnseenNotificationCount();
    }

    @PostMapping("/markasseen")
    public void markAsSeen(@RequestParam("id") Long id) {
        ANS.markAsSeen(id);
    }

    @GetMapping(value = "/stream", produces = "text/event-stream")
    public ResponseEntity<SseEmitter> registerNotificationStream() throws UnAuthorizedException {
        if(SecurityUtil.getCurrentUser() == null)
            throw new UnAuthorizedException("you are not logged in");
        return new ResponseEntity<>(ANS.userLoggedIn(SecurityUtil.getCurrentUser()), HttpStatus.OK);
    }

}
