package inodes.repository;

import inodes.models.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagsRepo extends JpaRepository<Tag, String> {

}
