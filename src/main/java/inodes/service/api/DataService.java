package inodes.service.api;

import inodes.models.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public abstract class DataService extends Observable {

    public static enum ObservableEvents {
        SEARCH,
        NEW
    }

    public static class SearchResponse {
        List<Document> results;
        long totalResults;

        public List<Document> getResults() {
            return results;
        }

        public void setResults(List<Document> results) {
            this.results = results;
        }

        public long getTotalResults() {
            return totalResults;
        }

        public void setTotalResults(long totalResults) {
            this.totalResults = totalResults;
        }
    }

    @Autowired
    AuthorizationService AS;

    @Autowired
    CollabService CS;

    public DataService() {
        register(ObservableEvents.SEARCH, o -> {
            Map<String, Long> votes = CS.getVotes(((List<Document>) o).stream().map(d -> d.getId()).collect(Collectors.toList()));
            for (Document doc : (List<Document>)o) {
                Long l = votes.get(doc.getId());
                doc.setVotes(l == null ? 0 : l);
            }
        });
    }

    public SearchResponse search(String q, long offset, int pageSize, List<String> sortOn) throws Exception {
        SearchResponse resp = _search(q, null, offset, pageSize, sortOn);
        notifyObservers(ObservableEvents.SEARCH, resp.getResults());
        return resp;
    }

    public void deleteObj(String user, String id) throws Exception {
        AS.checkDeletePermission(user, get(id));
        _deleteObj(id);
    }

    public Document get(String id) throws Exception {
        return _search("", id, 0, 1, null).getResults().get(0);
    }

    public void putData(String user, Document doc) throws Exception {
        AS.checkCreatePermission(user, doc);
        doc.setPostTime(System.currentTimeMillis());
        doc.setOwner(user);
        notifyObservers(ObservableEvents.NEW, doc);
        _putData(doc);
    }

    protected abstract SearchResponse _search(String q, String id, long offset, int pageSize, List<String> sortOn) throws Exception;

    protected abstract void _deleteObj(String id) throws IOException, Exception;

    protected abstract void _putData(Document doc) throws IOException;

    public abstract Map<String, Long> getTopTags(int max) throws Exception;

    public abstract Map<String, Long> getUserPostsFacets(String user) throws Exception;

}
