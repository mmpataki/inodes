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
            @ModelAttribute("loggedinuser") String user
    ) throws Exception {
        return DS.search(user, q, offset, pageSize, sortOn);
    }

    @RequestMapping(value = "/data", method = RequestMethod.POST)
    public void put(@RequestBody Document doc, @ModelAttribute("loggedinuser") String user) throws Exception {
        DS.putData(user, doc);
    }

    @RequestMapping(value = "/data/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable String id, @ModelAttribute("loggedinuser") String user) throws Exception {
        DS.deleteObj(user, id);
    }
}
