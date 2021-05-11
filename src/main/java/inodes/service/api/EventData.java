package inodes.service.api;

import inodes.util.SecurityUtil;

import java.util.HashMap;
import java.util.Map;

public class EventData {

    String publisher;
    Map<Object, Object> data;

    public EventData(Map<Object, Object> data, String publisher) {
        this.data = data;
        this.publisher = publisher;
    }

    public Object get(Object key) {
        return data.get(key);
    }

    public static EventData of(Object... elems) {
        if (elems.length % 2 != 0) {
            throw new IllegalArgumentException("args should be of type (key value)+");
        }
        Map<Object, Object> data = new HashMap<>();
        for (int i = 0; i < elems.length; i += 2) {
            data.put(elems[i], elems[i + 1]);
        }
        return new EventData(data, SecurityUtil.getCurrentUser());
    }

    @Override
    public String toString() {
        return "{publisher" + publisher + " , data: " + data + '}';
    }

    public String getPublisher() {
        return publisher;
    }
}
