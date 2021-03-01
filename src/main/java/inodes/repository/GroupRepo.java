package inodes.repository;

import inodes.models.Group;
import inodes.models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepo extends CrudRepository<Group, String> {

    List<String> findUsersByGroupName(String groupName);

    public static interface JustGroupName {
        String getGroupName();
    }

    List<JustGroupName> findGroupNameByUsers(User userName);

}
