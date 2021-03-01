package inodes.service;

import inodes.Configuration;
import inodes.service.api.NoSuchUserException;
import inodes.service.api.SubscriptionService;
import inodes.service.api.UserExistsException;
import inodes.service.api.UserGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import static inodes.util.TryCatchUtil.tc;

@Service
public class DBBasedSubscriberService extends SubscriptionService {

    @Override
    protected void _saveSubscription(SubscriptionService.Subscription subscription) {

    }

    @Override
    public List<SubscriptionService.Subscription> getSubscribers(String id) {
        return null;
    }
}
