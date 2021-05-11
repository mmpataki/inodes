package inodes.service.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Observable {

    Logger LOG = LoggerFactory.getLogger(Observable.class);

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

    public void notifyPreEvent(Object tag, EventData args) {
        notify(tag, preobservers, args);
    }

    public void notifyPostEvent(Object tag, EventData args) {
        notify(tag, postobservers, args);
    }

    private void notify(Object tag, Map<Object, List<Interceptor>> observers, EventData args) {
        if (observers.containsKey(tag)) {
            LOG.debug(String.format("Notifying event (%s : %s) to %s", tag.toString(), args.toString(), observers.get(tag)));
            for (Interceptor interceptor : observers.get(tag)) {
                try {
                    interceptor.intercept(args);
                } catch (Exception e) {
                    LOG.error("Failed to notify interceptor: " + interceptor.getClass().getName(), e);
                }
            }
        }
    }
}
