package inodes.service.api;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Observable {

    Map<Object, List<Interceptor>> preobservers = new HashMap<>();
    Map<Object, List<Interceptor>> postobservers = new HashMap<>();

    public void registerPostEvent(Object tag, Interceptor interceptor) {
        postobservers.computeIfAbsent(tag, s -> new LinkedList<>());
        postobservers.get(tag).add(interceptor);
    }

    public void registerPreEvent(Object tag, Interceptor interceptor) {
        preobservers.computeIfAbsent(tag, s -> new LinkedList<>());
        preobservers.get(tag).add(interceptor);
    }

    public void notifyPreEvent(Object tag, Object o) {
        notify(tag, preobservers, o);
    }

    public void notifyPostEvent(Object tag, Object o) {
        notify(tag, postobservers, o);
    }

    private void notify(Object tag, Map<Object, List<Interceptor>> observers, Object o) {
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
