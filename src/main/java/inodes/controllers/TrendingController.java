package inodes.controllers;

import inodes.models.Document;
import inodes.service.api.TrendingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@CrossOrigin
public class TrendingController {

    @Autowired
    TrendingService TS;

    @RequestMapping(value = "/trendingDocs", method = RequestMethod.GET)
    public Collection<Document> getTrendingObjects(Integer max) throws Exception {
        if(max == null) {
            max = 10;
        }
        return TS.getTrendingDocs(max);
    }

    @RequestMapping(value = "/trendingTags", method = RequestMethod.GET)
    public Collection<TrendingService.TrendingItem<String>> getTrendingTags(Integer max) throws Exception {
        if(max == null) {
            max = 10;
        }
        return TS.getTrendingTags(max);
    }

}
