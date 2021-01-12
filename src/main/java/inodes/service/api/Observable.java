package inodes.service.api;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Observable {

    Map<Object, List<Interceptor>> observers = new HashMap<>();

    public void register(Object tag, Interceptor interceptor) {
        observers.computeIfAbsent(tag, s -> new LinkedList<>());
        observers.get(tag).add(interceptor);
    }

    public void notifyObservers(Object tag, Object o) {
        if(observers.containsKey(tag)) {
            for (Interceptor interceptor : observers.get(tag)) {
                try {
                    interceptor.intercept(o);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
