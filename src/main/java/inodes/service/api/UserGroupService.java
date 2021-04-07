package inodes.service.api;

import inodes.models.Credential;
import inodes.models.Group;
import inodes.models.User;
import inodes.models.UserInfo;
import inodes.repository.GroupRepo;
import inodes.repository.UserRepo;
import inodes.util.Hasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

import static inodes.util.TryCatchUtil.tc;

@Service
public class UserGroupService extends Observable {

    Logger LOG = LoggerFactory.getLogger(UserGroupService.class);

    @Autowired
    EventService ES;

    @Autowired
    UserRepo UR;

    @Autowired
    GroupRepo GR;

    Random R = new Random();

    enum Events {
        USER_REGISTERED,
        USER_ADDED_TO_GROUP,
        USER_SEARCH
    }

    @PostConstruct
    public void _init() {

        /* send user registration emails : push to show this is NEEDED */
        registerPostEvent(Events.USER_REGISTERED, o -> {
            ES.post(EventService.Type.REGISTER_USER, o);
        });

        /* add user groups to user info */
        registerPostEvent(Events.USER_SEARCH, o -> {
            UserInfo u = (UserInfo) o;
            u.addExtraInfo("groups", getGroupsOf(u.getBasic().getUserName()));
        });

        tc(() -> _register(new User("mmp", "Madhusoodan Pataki", "m@123", true, "CREATE,DELETE,EDIT,UPVOTE,DOWNVOTE,COMMENT", "", "")));
        tc(() -> _register(new User("admin", "Admin", "a@123", true, "CREATE,DELETE,EDIT,UPVOTE,DOWNVOTE,COMMENT,ADMIN", "", "")));

        tc(() -> _createGroup(new Group(ADMIN, "admin group", "", "")));
        tc(() -> _createGroup(new Group(PUBLIC, "everyone", "", "")));
        tc(() -> _createGroup(new Group(SECURITY, "security group to review content", "", "")));

        tc(() -> _addUserToGroup(ADMIN, "admin"));
        tc(() -> _addUserToGroup(SECURITY, "admin"));
    }

    public boolean authenticate(Credential cred) throws Exception {
        User c = getUser(cred.getUserName());
        if (c != null) {
            return c.isVerified() && c.getPassword().equals(Hasher.hash(cred.getPassword()));
        }
        return false;
    }

    public void validate(String uid, String tok) throws Exception {
        User u = getUser(uid);
        if (!u.__getRegTok().equals(tok)) {
            throw new Exception("token don't match, re-register");
        }
        u.setVerified(true);
        UR.save(u);
    }

    public void register(User user) throws Exception {
        if (getUser(user.getUserName()) != null) {
            throw new UserExistsException(user.getUserName() + " already exists");
        }

        user.setRoles("UPVOTE,DOWNVOTE,COMMENT");
        user.setVerified(false);
        user.setRegTok(R.nextDouble() + "-" + R.nextInt());
        user.setPassword(Hasher.hash(user.getPassword()));

        notifyPreEvent(Events.USER_REGISTERED, user);
        _register(user);
        notifyPostEvent(Events.USER_REGISTERED, user);
    }

    private void _register(User cred) throws Exception {
        UR.save(cred);
    }

    /**
     * @param userName
     * @throws Exception
     * @returnb : null if not present, clone of the user otherwise
     */
    public User getUser(String userName) throws Exception {
        User cred = UR.findOne(userName);
        if (cred != null) {
            return cred.clone();
        }
        return null;
    }

    public boolean isAdmin(String userId) throws Exception {
        if(userId == null) return false;
        return getUser(userId).getRoles().contains("ADMIN");
    }

    public List<User> getUsers() throws Exception {
        List<User> ret = new LinkedList<>();
        UR.findAll().forEach(ret::add);
        return ret;
    }

    public UserInfo getUserInfo(String uid) throws Exception {
        User user = getUser(uid);
        UserInfo uInfo = new UserInfo(user);
        notifyPostEvent(Events.USER_SEARCH, uInfo);
        return uInfo;
    }

    public void updateUser(String modifier, User u) throws Exception {

        if (!modifier.equals(u.getUserName()) && !isAdmin(modifier)) {
            throw new UnAuthorizedException("Unauthorized");
        }
        User modifierUser = getUser(modifier);
        if (u.getRoles() != null) {
            for (String role : u.getRoles().split(",")) {
                if (!modifierUser.getRoles().contains(role)) {
                    throw new UnAuthorizedException("You are not authorized to modify roles of this user");
                }
            }
        }

        User origUser = getUser(u.getUserName());
        for (String role : origUser.getRoles().split(",")) {
            if (!modifierUser.getRoles().contains(role)) {
                throw new UnAuthorizedException("You are not authorized to modify roles of this user");
            }
        }

        // this origUser is cached, so don't update it right now, let implementation do it.
        User copyUser = origUser.clone();

        if (u.getRoles() != null)
            copyUser.setRoles(u.getRoles());

        if (isAdmin(modifier) || modifier.equals(u.getUserName())) {
            copyUser.setFullName(u.getFullName());
            copyUser.setEmail(u.getEmail());
            copyUser.setTeamsUrl(u.getTeamsUrl());
            if (u.getPassword() != null && !u.getPassword().isEmpty())
                copyUser.setPassword(u.getPassword());
        }

        UR.save(copyUser);
    }

    public List<UserRepo.UserNameAndFullName> findUsersLike(String sQuery) {
        return UR.findByUserNameContainingIgnoreCaseOrFullNameContainingIgnoreCase(sQuery, sQuery);
    }

    public List<String> findGroupsLike(String sQuery) {
        return GR.findByGroupNameContainingIgnoreCase(sQuery).stream().map(g -> g.getGroupName()).collect(Collectors.toList());
    }

    public static final String SECURITY = "security";
    public static final String ADMIN = "admin";
    public static final String PUBLIC = "public";


    @Autowired
    AuthorizationService AS;

    public List<String> getAllGroups() throws Exception {
        List<String> ret = new LinkedList<>();
        GR.findAll().forEach(x -> ret.add(x.getGroupName()));
        return ret;
    }

    public List<String> getGroupsOf(String user) throws Exception {
        if (user == null) {
            return Collections.singletonList(PUBLIC);
        }
        List<String> grps = GR.findGroupNameByUsers(User.builder().userName(user).build()).stream().map(x -> x.getGroupName()).collect(Collectors.toList());
        grps.add(PUBLIC);
        return grps;
    }

    public Group getGroup(String groupName) throws Exception {
        return GR.findOne(groupName);
    }

    public void createGroup(String user, Group grp) throws Exception {
        AS.checkGroupCreationPermissions(user);
        _createGroup(grp);
    }

    public void addUserToGroup(String curUser, String group, String user) throws Exception {
        AS.checkAddUserToGroupPermission(curUser, group);
        _addUserToGroup(group, user);
        notifyPostEvent(Events.USER_ADDED_TO_GROUP, Arrays.asList(curUser, user, group));
    }

    public void deleteUserFromGroup(String curUser, String group, String user) throws Exception {
        AS.checkDeleteUserFromGroupPermission(curUser, group);
        _deleteUserFromGroup(group, user);
    }

    protected void _addUserToGroup(String groupName, String userName) throws Exception {
        Group grp = getGroup(groupName);
        User user = getUser(userName);
        grp.addUser(user);
        GR.save(grp);
    }

    protected void _createGroup(Group grp) throws Exception {
        GR.save(grp);
    }

    public void _deleteUserFromGroup(String group, String user) throws Exception {
        Group grp = getGroup(group);
        grp.deleteUser(user);
        GR.save(grp);
    }

}
