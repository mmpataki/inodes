package inodes.models;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Data
public class UserInfo {

    User basic;
    Map<String, Object> extra;


    public UserInfo(User basic) {
        this.basic = basic;
        this.extra = new HashMap<>();
    }

    public void addExtraInfo(String key, Object info) {
        extra.put(key, info);
    }

}
