package inodes.models;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.solr.client.solrj.beans.Field;

import java.beans.Transient;
import java.util.List;

@Data
public class Document {

    @Field
    String id;

    @Field
    String content;

    @Field
    String type;

    @Field
    List<String> tags;

    @Field
    String owner;

    @Field
    long postTime;

    @Field
    List<String> visibility;

    @Field
    List<String> savedVisibility;

    @Field
    boolean needsApproval;

    enum ReadState {
        CAN_READ,
        PERM_NEEDED,
        PERM_REQUESTED
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
