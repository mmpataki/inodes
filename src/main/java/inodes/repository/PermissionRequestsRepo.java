package inodes.repository;

import inodes.models.PermissionRequest;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PermissionRequestsRepo extends CrudRepository<PermissionRequest, PermissionRequest.PRID> {

    List<PermissionRequest> findByReqBy(String ugid);

}
