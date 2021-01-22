package inodes.service;

import inodes.Configuration;
import inodes.models.Document;
import inodes.service.api.DataService;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

@Service
public class SolrDataService extends DataService {

    @Autowired
    Configuration conf;

    HttpSolrClient solr;

    @PostConstruct
    void init() {
        String urlString = conf.getProperty("searchservice.solr.url");
        solr = new HttpSolrClient.Builder(urlString).build();
    }

    private String getSearchQuery(String userId, String str, String id) {
        String visibility = String.format("visibility:(public OR \"\"%s) AND ", userId == null ? "" : " OR \"" + userId +"\"");
        if (id != null) {
            return String.format("%s id:(%s)", visibility, id);
        }
        String[] chunks = str.split("\\s+");
        StringBuilder q = new StringBuilder();
        q.append(visibility);
        for (String chunk : chunks) {
            if (chunk.charAt(0) == '#') {
                q.append("tags:(")
                        .append(chunk.substring(1))
                        .append("*) AND ");
            } else if (chunk.charAt(0) == '~') {
                q.append("owner:(")
                        .append(chunk.substring(1))
                        .append("*) AND ");
            } else if (chunk.charAt(0) == '%') {
                q.append("type:(*")
                        .append(chunk.substring(1))
                        .append("*) AND ");
            } else if (chunk.charAt(0) == '@') {
                q.append("id:(")
                        .append(chunk.substring(1))
                        .append(") AND ");
            } else {
                q.append("content:(*")
                        .append(chunk)
                        .append("*) AND ");
            }
        }
        return q.substring(0, q.length() - 5);
    }

    @Override
    protected void _updateDoc(String user, Document doc) {

    }

    public SearchResponse _search(String userId, String q, String id, long offset, int pageSize, List<String> sortOn) throws Exception {
        SolrQuery query = new SolrQuery();
        q = getSearchQuery(userId, q, id);
        System.out.println(q);
        query.set("q", q);
        query.setStart((int) offset);

        QueryResponse response = solr.query(query);
        SolrDocumentList docList = response.getResults();
        List<Document> docs = new ArrayList<>(docList.size());

        for (SolrDocument doc : docList) {
            Document d = new Document();
            try {
                d.setId((String) doc.getFieldValue("id"));
                d.setContent((String) ((List) doc.getFieldValue("content")).get(0));
                d.setType((String) ((List) doc.getFieldValue("type")).get(0));
                d.setTags((List<String>) doc.getFieldValue("tags"));
                d.setPostTime((Long) ((List) doc.getFieldValue("ctime")).get(0));
                d.setOwner((String) ((List) doc.getFieldValue("owner")).get(0));
                d.setVisibility((String) ((List) doc.getFieldValue("visibility")).get(0));
            } catch (Exception e) {
                e.printStackTrace();
            }
            docs.add(d);
        }

        SearchResponse resp = new SearchResponse();
        resp.setResults(docs);
        resp.setTotalResults(response.getResults().getNumFound());

        return resp;
    }

    public void _deleteObj(String id) throws Exception {
        solr.deleteById(id);
        solr.commit();
    }

    public void _putData(Document doc) throws IOException {
        SolrInputDocument idoc = new SolrInputDocument();
        idoc.addField("content", doc.getContent());
        idoc.addField("type", doc.getType());
        idoc.addField("tags", doc.getTags());
        idoc.addField("ctime", doc.getPostTime());
        idoc.addField("owner", doc.getOwner());
        idoc.addField("visibility", doc.getVisibility());
        try {
            solr.add(idoc);
            solr.commit();
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, Long> getTopTags(String type, int max) throws Exception {
        if(max <= 0) {
            max = 10;
        }
        return getFacets("*", max, "count", Arrays.asList(type));
    }

    private Map<String, Long> getFacets(String sq, int max, String sortField, List<String> facetFields) throws SolrServerException, IOException {
        SolrQuery q = new SolrQuery();
        q.setQuery(sq);
        q.setFacet(true);
        q.setFacetLimit(max);
        if(sortField != null)
            q.setFacetSort(sortField);
        if(facetFields != null)
            facetFields.forEach(ff -> q.addFacetField(ff));

        List<FacetField> ffs = solr.query(q).getFacetFields();
        Map<String, Long> ret = new HashMap<>();
        ffs.get(0).getValues().forEach(f -> {
            ret.put(f.getName(), f.getCount());
        });
        return ret;
    }

    @Override
    public Map<String, Long> getUserPostsFacets(String user) throws Exception {
        return getFacets("owner:" + user, 1000000, null, Arrays.asList("type"));
    }
}
