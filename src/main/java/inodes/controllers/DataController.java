package inodes.controllers;

import inodes.models.Document;
import inodes.service.api.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class DataController extends AuthenticatedController {

    @Autowired
    DataService DS;

    @RequestMapping(value = "/data", method = RequestMethod.GET)
    public DataService.SearchResponse search(
            @RequestParam(required = false, defaultValue = "*") String q,
            @RequestParam(required = false, defaultValue = "0") Long offset,
            @RequestParam(required = false, defaultValue = "0") Integer pageSize,
            @RequestParam(required = false) List<String> sortOn,
            @RequestParam(required = false) List<String> fq,
            @RequestParam(required = false, defaultValue = "10") Integer fqLimit,
            @ModelAttribute("loggedinuser") String user
    ) throws Exception {
        return DS.search(
                user,
                DataService.SearchQuery.builder()
                    .q(q)
                    .offset(offset)
                    .pageSize(pageSize)
                    .sortOn(sortOn)
                    .fq(fq)
                    .fqLimit(fqLimit)
                    .build()
        );
    }

    @RequestMapping(value = "/data", method = RequestMethod.POST)
    public void put(@RequestBody Document doc, @ModelAttribute("loggedinuser") String user) throws Exception {
        DS.putData(user, doc);
    }

    @RequestMapping(value = "/data/{docId}/approve", method = RequestMethod.POST)
    public void approve(@ModelAttribute("loggedinuser") String userId, @PathVariable("docId") String docId) throws Exception {
        DS.approve(userId, docId);
    }

    @RequestMapping(value = "/data/{docId}/flag", method = RequestMethod.POST)
    public void flag(@ModelAttribute("loggedinuser") String userId, @PathVariable("docId") String docId) throws Exception {
        DS.flag(userId, docId);
    }

    @RequestMapping(value = "/data/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable String id, @ModelAttribute("loggedinuser") String user) throws Exception {
        DS.deleteObj(user, id);
    }
}
