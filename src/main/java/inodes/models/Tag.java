package inodes.models;

import lombok.Builder;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "TAGS")
@Data
@Builder
public class Tag {
    @Id
    String name;
    String desc;
    long hits;
    long createdOn;
}
