package inodes.models;

import inodes.service.api.UserGroupService;

import java.util.HashMap;
import java.util.Map;

public class UserInfo {

    UserGroupService.User basic;
    Map<String, Long> postsCount;

    public UserInfo(UserGroupService.User basic) {
        this.basic = basic;
        this.postsCount = new HashMap<>();
    }

    public UserGroupService.User getBasic() {
        return basic;
    }

    public void setBasic(UserGroupService.User basic) {
        this.basic = basic;
    }

    public Map<String, Long> getPostsCount() {
        return postsCount;
    }

    public void setPostsCount(Map<String, Long> postsCount) {
        this.postsCount = postsCount;
    }

    void addPostTypeCount(String key, Long cnt) {
        this.postsCount.put(key, cnt);
    }
}
