package inodes.service.api;

import inodes.models.Document;
import inodes.models.PageResponse;
import inodes.models.Tag;
import inodes.models.TagInfo;
import inodes.repository.TagsRepo;
import inodes.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public abstract class TagsService extends Observable {

    @Autowired
    AuthorizationService AS;

    @Autowired
    TagsRepo TR;

    @Autowired
    DataService DS;

    public enum Events {
        SEARCH
    }

    @PostConstruct
    public void _init() {

        /* save the tags when there are new ones */
        Interceptor interceptor = o -> {
            Document d = (Document)o.get("doc");
            List<Tag> newTags = new LinkedList<>();
            d.getTags().forEach(tag -> {
                if (TR.findOne(tag) == null) {
                    newTags.add(Tag.builder().name(tag).description("").build());
                }
            });
            TR.save(newTags);
        };
        DS.registerPreEvent(DataService.ObservableEvents.NEW, interceptor);
        DS.registerPreEvent(DataService.ObservableEvents.UPDATE, interceptor);

        /* add the hits for tags */
        registerPostEvent(Events.SEARCH, ed -> {
            TagInfo tag = (TagInfo) ed.get("tag");
            DataService.SearchResponse resp = DS.search(
                    DataService.SearchQuery.builder()
                            .q("#" + tag.getBasic().getName())
                            .pageSize(0)
                            .offset(0)
                            .fq(Arrays.asList("type"))
                            .build()
            );
            tag.addMoreInfo("docCount", resp.getFacetResults().get("type"));
        });
    }

    public TagInfo getTag(String name) {
        notifyPreEvent(Events.SEARCH, EventData.of("tagName", name));
        TagInfo tagInfo = TagInfo.builder().basic(TR.findOne(name)).build();
        notifyPostEvent(Events.SEARCH, EventData.of("tag", tagInfo));
        return tagInfo;
    }

    public List<Tag> findTagsLike(String sQuery) {
        return TR.findByNameContainingIgnoreCase(sQuery);
    }

    public PageResponse<Tag> getTags(long start, int size) {
        start = start < 1 ? 0 : start;
        size = size < 0 ? 10 : size;
        Page<Tag> page = TR.findAll(new PageRequest((int) start, size));

        return PageResponse.<Tag>builder()
                .pageSize(size)
                .offset(start)
                .items(page.getContent())
                .totalItems(page.getTotalElements())
                .build();
    }

    public abstract List<Tag> getTopTags(int n);

    public void addTag(Tag tag) throws Exception {
        AS.checkTagCreatePermission();
        tag.setHits(0);
    }

    public class TrendingItem<T> implements Comparable<TrendingItem<T>> {
        public long hits;
        T item;

        public TrendingItem(long hits, T item) {
            this.hits = hits;
            this.item = item;
        }

        public long getHits() {
            return hits;
        }

        public void setHits(long hits) {
            this.hits = hits;
        }

        public T getItem() {
            return item;
        }

        public void setItem(T item) {
            this.item = item;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TrendingItem<?> that = (TrendingItem<?>) o;
            return item.equals(that.item);
        }

        @Override
        public int compareTo(TrendingItem<T> o) {
            return (int) -(hits - o.hits);
        }
    }

    public abstract Collection<Document> getTrendingDocs(int max) throws Exception;

    public abstract Map<String, Long> getTrendingTags(String type, int max) throws Exception;

}
