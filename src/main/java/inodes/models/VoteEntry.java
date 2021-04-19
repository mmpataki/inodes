package inodes.models;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class VoteEntry {

    @Id
    String id;

    Long votes;

}
