package inodes.service.api;

import inodes.models.Document;
import inodes.models.PageResponse;
import inodes.models.Tag;
import inodes.repository.TagsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public abstract class TagsService {

    @Autowired
    AuthorizationService AS;

    @Autowired
    TagsRepo TR;

    @Autowired
    DataService DS;

    @PostConstruct
    public void _init() {
        DS.registerPreEvent(DataService.ObservableEvents.NEW, o -> {

        });
    }

    public Tag getTag(String name) {
        return TR.findOne(name);
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

    public void addTag(String userId, Tag tag) throws Exception {
        AS.checkTagCreatePermission(userId);
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
