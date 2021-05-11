package inodes.service;

import inodes.models.Comment;
import inodes.models.VoteEntry;
import inodes.repository.CommentsRepo;
import inodes.repository.VotesRepo;
import inodes.service.api.CollabService;
import inodes.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DBBasedCollabService extends CollabService {

    @Autowired
    VotesRepo VR;

    @Autowired
    CommentsRepo CR;

    @Override
    protected Map<String, Long> _getVotes(List<String> ids) throws Exception {
        Map<String, Long> ret = new HashMap<>();
        VR.findAll(ids).forEach(v -> ret.put(v.getId(), v.getVotes()));
        return ret;
    }

    @Override
    protected List<Comment> _getComments(String id) throws Exception {
        return CR.findAll(Example.of(Comment.builder().postid(id).build()));
    }

    @Override
    protected Comment _comment(String id, String comment) throws Exception {
        Comment c = Comment.builder().postid(id).userid(SecurityUtil.getCurrentUser()).itime(System.currentTimeMillis()).txt(comment).build();
        CR.save(c);
        return c;
    }

    @Override
    protected void _deleteComment(String id, String owner, long time) throws Exception {
        CR.delete(Comment.CID.builder().postid(id).userid(owner).itime(time).build());
    }

    @Override
    protected void _downvote(String id) throws Exception {
        vote(id, -1);
    }

    @Override
    protected void _upvote(String id) throws Exception {
        vote(id, +1);
    }

    protected VoteEntry _getVoteEnt(String id) throws Exception {
        return VR.findOne(id);
    }

    private void vote(String id, int delta) throws Exception {
        VoteEntry ve = _getVoteEnt(id);
        if(ve == null) {
            ve = new VoteEntry();
            ve.setId(id);
        }
        ve.setVotes(ve.getVotes() + delta);
        VR.save(ve);
    }
}
