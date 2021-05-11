package inodes.controllers;

import inodes.models.Comment;
import inodes.service.api.CollabService;
import inodes.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class CollabController {

    @Autowired
    CollabService CS;

    @RequestMapping(value = "/posts/upvote/{id}", method = RequestMethod.POST)
    public void upVote(@PathVariable String id) throws Exception {
        CS.upVote(id);
    }

    @RequestMapping(value = "/posts/downvote/{id}", method = RequestMethod.POST)
    public void downVote(@PathVariable String id) throws Exception {
        CS.downVote(id);
    }

    @RequestMapping(value = "/posts/comment/{id}", method = RequestMethod.POST)
    public Comment comment(@PathVariable String id, @RequestBody String comment) throws Exception {
        if(comment.trim().isEmpty()) {
            throw new Exception("Empty comments not allowed!");
        }
        return CS.comment(id, comment);
    }

    @RequestMapping(value = "/posts/comment/{postid}/{owner}/{time}", method = RequestMethod.DELETE)
    public void deleteComment(@PathVariable("postid") String postId, @PathVariable("owner") String owner, @PathVariable("time") String time) throws Exception {
        CS.deleteComment(postId, owner, Long.parseLong(time));
    }

    @RequestMapping(value = "/posts/comments/{id}", method = RequestMethod.GET)
    public List<Comment> getVotes(@PathVariable String id) throws Exception {
        return CS.getComments(id);
    }

}
