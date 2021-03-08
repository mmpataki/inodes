package inodes.service;

import inodes.Configuration;
import inodes.models.Document;
import inodes.models.Tag;
import inodes.service.api.DataService;
import inodes.service.api.Interceptor;
import inodes.service.api.TagsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.util.*;

@Service
public class DbBasedTagsServiceImpl extends TagsService {

    @Autowired
    DataService DS;

    @Autowired
    Configuration conf;

    PriorityQueue<TrendingItem<String>> tTags = new PriorityQueue<>();
    Map<String, TrendingItem<String>> tag2Item = new HashMap<>();
    Set<Document> tDocs = new HashSet<>();

    Connection CONN;

    @PostConstruct
    void init() throws Exception {
        DS.registerPostEvent(DataService.ObservableEvents.SEARCH, o -> {
            Set<String> tags = new HashSet<>();
            for (Document doc : (List<Document>) o) {
                if (doc.getTags() != null) {
                    for (String tag : doc.getTags()) {
                        tags.add(tag);
                    }
                }
            }
            for (String tag : tags) {
                TrendingItem<String> t = tag2Item.computeIfAbsent(tag, s -> new TrendingItem<String>(0l, tag));
                t.hits++;
                tTags.remove(t);
                tTags.offer(t);
            }
        });

        Interceptor newTagsCapturer = d -> {
            tDocs.add((Document) d);
        };
        DS.registerPostEvent(DataService.ObservableEvents.NEW, newTagsCapturer);
        DS.registerPostEvent(DataService.ObservableEvents.UPDATE, newTagsCapturer);
    }

    @Override
    public List<Tag> getTopTags(int n) {
        return null;
    }

    @Override
    public Collection<Document> getTrendingDocs(int max) {
        return tDocs;
    }

    @Override
    public Map<String, Long> getTrendingTags(String type, int max) throws Exception {
        throw new UnsupportedOperationException("not supported yet");
    }
}
