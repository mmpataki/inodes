package inodes.service;

import inodes.Configuration;
import inodes.models.Subscription;
import inodes.repository.SubscriptionRepo;
import inodes.service.api.NoSuchUserException;
import inodes.service.api.SubscriptionService;
import inodes.service.api.UserExistsException;
import inodes.service.api.UserGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import static inodes.util.TryCatchUtil.tc;

@Service
public class DBBasedSubscriptionService extends SubscriptionService {

    @Autowired
    SubscriptionRepo SR;

    @Override
    protected void _saveSubscription(Subscription subscription) {
        SR.save(subscription);
    }

    @Override
    public List<Subscription> getSubscribers(Subscription.SubscribedObjectType objType, String objid) {
        return SR.findAll(Example.of(
            Subscription.builder()
                .objType(objType)
                .objid(objid)
                .build()
        ));
    }

    @Override
    public List<Subscription> getSubscribers(Subscription.SubscriberType subsciberTyp, String subscriberId) {
        return SR.findAll(Example.of(
                Subscription.builder()
                        .subscriberType(subsciberTyp)
                        .subscriberId(subscriberId)
                        .build()
        ));
    }

    @Override
    public List<Subscription> getSubscriptions(Subscription.SubscribedObjectType objType, String objid, Subscription.SubscriberType subsciberTyp, String subscriberId) {
        return SR.findAll(Example.of(
                Subscription.builder()
                        .subscriberType(subsciberTyp)
                        .subscriberId(subscriberId)
                        .objType(objType)
                        .objid(objid)
                        .build()
        ));
    }

    public List<Subscription> getSubscriptions() {
        return SR.findAll();
    }

}
