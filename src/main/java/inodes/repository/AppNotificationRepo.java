package inodes.repository;

import inodes.models.AppNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface AppNotificationRepo extends PagingAndSortingRepository<AppNotification, AppNotification.NID> {

    List<AppNotification> findByNForInOrderByPtimeDesc(List<String> ugids, Pageable page);

    Integer countByNForInAndSeenFalse(List<String> ugids);

}
