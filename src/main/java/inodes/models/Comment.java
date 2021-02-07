package inodes.models;

import lombok.Data;

@Data
public class Comment {
    public String postid;
    public String user;
    public Long time;
    public String comment;

    public Comment(String postid, String user, Long time, String comment) {
        this.postid = postid;
        this.user = user;
        this.time = time;
        this.comment = comment;
    }
}
