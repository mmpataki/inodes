package inodes.repository;

import inodes.models.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepo extends CrudRepository<User, String> {

    public List<UserNameAndFullName> findByUserNameContainingIgnoreCaseFullNameContainingIgnoreCase(@Param("keyword") String keyword);

    interface UserNameAndFullName {
        String getUserName();
        String getFullName();
    }

}
