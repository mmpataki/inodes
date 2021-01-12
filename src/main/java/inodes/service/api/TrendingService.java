package inodes.service.api;

import inodes.models.Document;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public interface TrendingService {

    class TrendingItem<T> implements Comparable<TrendingItem<T>> {
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

    Collection<Document> getTrendingDocs(int max) throws Exception;

    Collection<TrendingItem<String>> getTrendingTags(int max) throws Exception;

}
