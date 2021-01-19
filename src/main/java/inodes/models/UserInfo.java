package inodes.models;

import inodes.service.api.UserService;

import java.util.HashMap;
import java.util.Map;

public class UserInfo {

    UserService.User basic;
    Map<String, Long> postsCount;

    public UserInfo(UserService.User basic) {
        this.basic = basic;
        this.postsCount = new HashMap<>();
    }

    public UserService.User getBasic() {
        return basic;
    }

    public void setBasic(UserService.User basic) {
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
