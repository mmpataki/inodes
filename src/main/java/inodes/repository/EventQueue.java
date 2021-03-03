package inodes.repository;

import inodes.service.api.EventService;
import org.springframework.stereotype.Repository;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Repository
public class EventQueue {

    BlockingQueue<EventService.Event> Q = new LinkedBlockingQueue<>();

    public void enqueue(EventService.Event event) {
        Q.offer(event);
    }

    public EventService.Event deque() throws InterruptedException {
        return Q.take();
    }

    public boolean isEmpty() {
        return Q.isEmpty();
    }

}
