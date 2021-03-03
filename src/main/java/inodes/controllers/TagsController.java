package inodes.controllers;

import inodes.models.PageResponse;
import inodes.models.Tag;
import inodes.service.api.TagsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class TagsController {

    @Autowired
    TagsService TS;

    @GetMapping("/tags/{name}")
    public Tag getTag(@PathVariable String name) {
        return TS.getTag(name);
    }

    @GetMapping("/tags")
    public PageResponse<Tag> getTags(long start, int size) {
        return TS.getTags(start, size);
    }

    @PostMapping("/tags")
    public void putTag(@ModelAttribute("loggedinuser") String user, Tag tag) throws Exception {
        TS.addTag(user, tag);
    }

}
