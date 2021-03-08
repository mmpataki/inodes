package inodes.models;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class TagInfo {

    Tag basic;

    Map<String, Object> extra;

    public void addMoreInfo(String key, Object o) {
        if(extra == null) {
            extra = new HashMap<>();
        }
        extra.put(key, o);
    }

}
