package inodes.controllers;

import inodes.models.Comment;
import inodes.service.api.CollabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class CollabController extends AuthenticatedController {

    @Autowired
    CollabService CS;

    @RequestMapping(value = "/posts/upvote/{id}", method = RequestMethod.POST)
    public void upVote(@PathVariable String id, @ModelAttribute("loggedinuser") String user) throws Exception {
        CS.upVote(user, id);
    }

    @RequestMapping(value = "/posts/downvote/{id}", method = RequestMethod.POST)
    public void downVote(@PathVariable String id, @ModelAttribute("loggedinuser") String user) throws Exception {
        CS.downVote(user, id);
    }

    @RequestMapping(value = "/posts/comment/{id}", method = RequestMethod.POST)
    public void comment(@PathVariable String id, @RequestBody String comment, @ModelAttribute("loggedinuser") String user) throws Exception {
        CS.comment(user, id, comment);
    }

    @RequestMapping(value = "/posts/comments/{id}", method = RequestMethod.GET)
    public List<Comment> getVotes(@PathVariable String id) throws Exception {
        return CS.getComments(id);
    }

}
