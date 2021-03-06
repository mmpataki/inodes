package inodes.repository;

import inodes.models.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TagsRepo extends JpaRepository<Tag, String> {
    public List<Tag> findByNameContainingIgnoreCase(@Param("keyword") String keyword);
}
