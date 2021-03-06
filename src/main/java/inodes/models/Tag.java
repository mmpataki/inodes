package inodes.models;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "TAGS")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Tag {
    @Id
    String name;
    String description;
    long hits;
    long createdOn;
}
