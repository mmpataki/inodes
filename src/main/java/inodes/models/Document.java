package inodes.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.solr.client.solrj.beans.Field;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class Document {

    enum ReadState {
        CAN_READ,
        PERM_NEEDED,
        PERM_REQUESTED
    }


    @Field
    String id;

    @Field
    String content;

    @Field
    String type;

    @Field
    String owner;

    @Field
    long postTime;

    @Field
    boolean needsApproval;

    /* solj doesn't support set datatype in beans, so the @Field annotation is on setter */

    Set<String> tags;

    Set<String> visibility;

    Set<String> savedVisibility;

    @Field
    @JsonProperty("tags")
    public void setTags(List<String> tags) {
        this.tags = new HashSet<>(tags);
    }

    @Field
    @JsonProperty("visibility")
    public void setVisibility(List<String> visibility) {
        this.visibility = new HashSet<>(visibility);
    }

    @Field
    @JsonProperty("savedVisibility")
    public void setSavedVisibility(List<String> savedVisibility) {
        this.savedVisibility = new HashSet<>(savedVisibility);
    }

    /* compatibility setters, don't allow from REST APIs */
    @JsonIgnore
    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    @JsonIgnore
    public void setVisibility(Set<String> visibility) {
        this.visibility = visibility;
    }

    @JsonIgnore
    public void setSavedVisibility(Set<String> savedVisibility) {
        this.savedVisibility = savedVisibility;
    }

    private transient ReadState readState = ReadState.PERM_NEEDED;
    transient long votes;
    transient List<Comment> comments;

    public boolean canRead() {
        return readState.equals(ReadState.CAN_READ);
    }

    public boolean permissionNeeded() {
        return readState.equals(ReadState.PERM_NEEDED);
    }

    public boolean permissionRequested() {
        return readState.equals(ReadState.PERM_REQUESTED);
    }

    public void setCanRead() {
        readState = ReadState.CAN_READ;
    }

    public void setPermNeeded() {
        readState = ReadState.PERM_NEEDED;
    }

    public void setPermRequested() {
        readState = ReadState.PERM_REQUESTED;
    }

    public boolean upVotable() {
        return true;
    }

    public boolean downVotable() {
        return true;
    }

    public boolean commentable() {
        return true;
    }

}
