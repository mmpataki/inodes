package inodes.controllers;

import inodes.models.Subscription;
import inodes.service.api.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SubscriptionController extends AuthenticatedController {

    @Autowired
    SubscriptionService SS;

    @PostMapping("/subscribe")
    public void subscribe(
            @ModelAttribute("loggedinuser") String user,
            @RequestParam Subscription.SubscriberType subscriberType, @RequestParam String subscriberId,
            @RequestParam Subscription.SubscribedObjectType objTyp, @RequestParam String objId,
            @RequestParam Subscription.Event event) throws Exception {
        SS.subscribe(user, subscriberType, subscriberId, objTyp, objId, event);
    }

    @GetMapping("/subscriptionsof/{subscriberType}/{gid}")
    public List<Subscription> getSubscriptionsof(@PathVariable Subscription.SubscriberType subscriberType, @PathVariable String gid) {
        return SS.getSubscribers(subscriberType, gid);
    }

    @GetMapping("/subscriptionsfor/{objType}/{objid}")
    public List<Subscription> getSubscriptionsFor(@PathVariable Subscription.SubscribedObjectType objType, @PathVariable String objid) {
        return SS.getSubscribers(objType, objid);
    }

    @GetMapping("/subscriptions")
    public List<Subscription> getSubscriptions() {
        return SS.getSubscriptions();
    }

}
