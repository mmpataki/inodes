package inodes.models;

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

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
