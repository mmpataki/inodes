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

    @Autowired
    Configuration conf;

    Connection CONN;
    Random R = new Random();

    @PostConstruct
    void init() throws Exception {
        try {
            CONN = DriverManager.getConnection(conf.getProperty("subscriptionservice.db.url"), conf.getProperty("subscriptionservice.db.user"), conf.getProperty("subscriptionservice.db.password"));
            tc(() -> CONN.createStatement().execute("CREATE TABLE subscription (id VARCHAR(32) PRIMARY KEY, typ INT)"));
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    protected void _saveSubscription(SubscriptionService.Subscription subscription) {

    }

    @Override
    public List<SubscriptionService.Subscription> getSubscribers(String id) {
        return null;
    }
}
