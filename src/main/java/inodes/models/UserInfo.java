package inodes.models;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Data
public class UserInfo {

    User basic;
    Map<String, Long> postsCount;
    List<String> groups;

    public UserInfo(User basic) {
        this.basic = basic;
        this.postsCount = new HashMap<>();
    }

    void addPostTypeCount(String key, Long cnt) {
        this.postsCount.put(key, cnt);
    }
}
