package inodes.service.api;

import inodes.models.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    public SearchResponse search(String user, String q, long offset, int pageSize, List<String> sortOn) throws Exception {
        SearchResponse resp = _search(user, q, null, offset, pageSize, sortOn);
        notifyObservers(ObservableEvents.SEARCH, resp.getResults());
        return resp;
    }

    public void deleteObj(String user, String id) throws Exception {
        AS.checkDeletePermission(user, get(user, id));
        _deleteObj(id);
    }

    public Document get(String user, String id) throws Exception {
        try {
            return _search(user, "", id, 0, 1, null).getResults().get(0);
        } catch (IndexOutOfBoundsException i) {
            throw new NoSuchDocumentException(id);
        }
    }

    public void putData(String user, Document doc) throws Exception {
        assert
            Objects.nonNull(doc.getContent()) &&
            Objects.nonNull(doc.getTags()) &&
            Objects.nonNull(doc.getVisibility()) &&
            Objects.nonNull(doc.getType());

        AS.checkCreatePermission(user, doc);
        doc.setPostTime(System.currentTimeMillis());
        doc.setOwner(user);
        notifyObservers(ObservableEvents.NEW, doc);
        _putData(doc);
    }

    public void updateDocument(String user, Document doc) throws Exception {
        AS.checkUpdatePermission(user, get(user, doc.getId()), doc);
        _updateDoc(user, doc);
    }

    protected abstract void _updateDoc(String user, Document doc);

    protected abstract SearchResponse _search(String user, String q, String id, long offset, int pageSize, List<String> sortOn) throws Exception;

    protected abstract void _deleteObj(String id) throws IOException, Exception;

    protected abstract void _putData(Document doc) throws IOException;

    public abstract Map<String, Long> getTopTags(String type, int max) throws Exception;

    public abstract Map<String, Long> getUserPostsFacets(String user) throws Exception;

}
