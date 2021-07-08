package inodes.repository;

import inodes.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface CommentsRepo extends JpaRepository<Comment, Comment.CID> {

    @Query("select c.postid, count(*) from COMMENTS c where c.postid in :ids group by postid")
    List<?> countWithPostidIn(@Param("ids") List<String> ids);
}