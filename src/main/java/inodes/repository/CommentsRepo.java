package inodes.repository;

import inodes.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentsRepo extends JpaRepository<Comment, Comment.CID> {}