package inodes.repository;

import inodes.models.AppNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AppNotificationRepo extends CrudRepository<AppNotification, AppNotification.NID> {

    List<AppNotification> findByNForInOrderByPtimeDesc(List<String> ugids, Pageable page);

}
