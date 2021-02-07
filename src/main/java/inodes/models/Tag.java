package inodes.models;

import lombok.Data;

@Data
public class Tag {
    String name;
    String desc;
    long hits;
}
