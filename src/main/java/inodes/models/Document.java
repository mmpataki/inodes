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

    transient long votes;
    transient List<Comment> comments;

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
