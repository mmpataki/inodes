package inodes.service.api;

import inodes.models.Document;
import inodes.models.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public abstract class TagsService {

    @Autowired
    AuthorizationService AS;

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
