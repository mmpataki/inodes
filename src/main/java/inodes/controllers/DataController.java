package inodes.controllers;

import inodes.models.Document;
import inodes.service.api.DataService;
import inodes.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class DataController {

    @Autowired
    DataService DS;

    @RequestMapping(value = "/data", method = RequestMethod.GET)
    public DataService.SearchResponse search(
            @RequestParam(required = false, defaultValue = "*") String q,
            @RequestParam(required = false, defaultValue = "0") Long offset,
            @RequestParam(required = false, defaultValue = "0") Integer pageSize,
            @RequestParam(required = false) List<String> sortOn,
            @RequestParam(required = false) List<String> fq,
            @RequestParam(required = false, defaultValue = "10") Integer fqLimit
    ) throws Exception {
        return DS.search(
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
    public void put(@RequestBody Document doc, @RequestParam String changeNote) throws Exception {
        DS.putData(doc, changeNote);
    }

    @RequestMapping(value = "/data/{docId}/approve", method = RequestMethod.POST)
    public void approve(@PathVariable("docId") String docId) throws Exception {
        DS.approve(docId);
    }

    @RequestMapping(value = "/data/{docId}/flag", method = RequestMethod.POST)
    public void flag(@PathVariable("docId") String docId) throws Exception {
        DS.flag(docId);
    }

    @RequestMapping(value = "/data/{docId}/askPermission", method = RequestMethod.POST)
    public void askPermission(@PathVariable("docId") String docId) throws Exception {
        DS.askPermission(docId);
    }

    @PostMapping(value = "/data/{docId}/givePermission/{userid}")
    public void givePermission(@PathVariable("docId") String docId, @PathVariable("userid") String userid) throws Exception {
        DS.givePermission(docId, userid);
    }

    @RequestMapping(value = "/data/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable String id) throws Exception {
        DS.deleteObj(id);
    }
}
