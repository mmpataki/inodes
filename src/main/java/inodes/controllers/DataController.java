package inodes.controllers;

import inodes.models.Document;
import inodes.models.PermissionRequest;
import inodes.service.api.DataService;
import inodes.service.api.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class DataController {

    @Autowired
    DataService DS;

    @Autowired
    SecurityService SS;

    @GetMapping("/data")
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

    @PostMapping("/data")
    public void put(@RequestBody Document doc, @RequestParam String changeNote) throws Exception {
        DS.putData(doc, changeNote);
    }

    @PostMapping("/data/{docId}/approve")
    public void approve(@PathVariable("docId") String docId) throws Exception {
        DS.approve(docId);
    }

    @PostMapping("/data/{docId}/flag")
    public void flag(@PathVariable("docId") String docId) throws Exception {
        DS.flag(docId);
    }

    @PostMapping("/data/{docId}/askPermission")
    public void askPermission(@PathVariable("docId") String docId) throws Exception {
        SS.askPermission(docId);
    }

    @PostMapping("/data/{docId}/givePermission/{userid}")
    public void givePermission(@PathVariable("docId") String docId, @PathVariable("userid") String userid) throws Exception {
        SS.givePermission(docId, userid);
    }

    @GetMapping("/data/permission-requests")
    public List<PermissionRequest> permissionRequests() {
        return SS.getPermRequests();
    }

    @DeleteMapping("/data/{id}")
    public void delete(@PathVariable String id) throws Exception {
        DS.deleteObj(id);
    }

    @PostMapping("/data/bulkupdate")
    public Map<String, Object> bulkUpdate(@RequestBody DataService.BulkUpdateRequest req) throws Exception {
        return DS.bulkUpdateContent(req);
    }
}
