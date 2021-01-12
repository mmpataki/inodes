package inodes.service;

import inodes.models.Document;
import inodes.service.api.DataService;
import inodes.service.api.TrendingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class FsBasedTrendingServiceImpl implements TrendingService {

    @Autowired
    DataService DS;

    PriorityQueue<TrendingItem<String>> tTags = new PriorityQueue<>();
    Map<String, TrendingItem<String>> tag2Item = new HashMap<>();

    Set<Document> tDocs = new HashSet<>();

    @PostConstruct
    void init() {
        DS.register(DataService.ObservableEvents.SEARCH, o -> {
            Set<String> tags = new HashSet<>();
            for (Document doc : (List<Document>)o) {
                if(doc.getTags() != null) {
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
        DS.register(DataService.ObservableEvents.NEW, d -> {
            tDocs.add((Document) d);
        });
    }

    @Override
    public Collection<Document> getTrendingDocs(int max) {
        return tDocs;
    }

    @Override
    public Collection<TrendingItem<String>> getTrendingTags(int max) throws Exception {
        List<TrendingItem<String>> ret = new LinkedList<>();
        Iterator<TrendingItem<String>> it = tTags.iterator();
        for (int i = 0; i < max && it.hasNext(); i++) {
            ret.add(it.next());
        }
        return ret;
    }
}
