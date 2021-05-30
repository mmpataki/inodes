package inodes.repository;

import inodes.models.AuthTok;
import inodes.models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthTokRepo extends CrudRepository<AuthTok, AuthTok.AuthTokId> {

}
