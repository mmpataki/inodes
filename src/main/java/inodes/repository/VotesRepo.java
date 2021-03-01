package inodes.repository;

import inodes.models.VoteEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VotesRepo extends JpaRepository<VoteEntry, String> {}
