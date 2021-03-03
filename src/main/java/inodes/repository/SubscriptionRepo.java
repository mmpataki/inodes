package inodes.repository;

import inodes.models.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepo extends JpaRepository<Subscription, Subscription.SID> {

}
