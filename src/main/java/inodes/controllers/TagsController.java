package inodes.controllers;

import inodes.models.*;
import inodes.service.api.TagsService;
import inodes.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TagsController {

    @Autowired
    TagsService TS;

    @GetMapping("/tags/{name}")
    public TagInfo getTag(@PathVariable String name) {
        return TS.getTag(name);
    }

    @GetMapping("/tags")
    public PageResponse<Tag> getTags(long start, int size) {
        return TS.getTags(start, size);
    }

    @GetMapping("/find-tags-like")
    public List<Tag> getTagsLike(@RequestParam("term") String sQuery) {
        return TS.findTagsLike(sQuery);
    }

    @PostMapping("/tags")
    public void putTag(Tag tag) throws Exception {
        TS.addTag(tag);
    }

}
