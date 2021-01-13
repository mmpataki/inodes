package inodes.service;

import inodes.Configuration;
import inodes.models.Document;
import inodes.service.api.DataService;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private String getSearchQuery(String str, String id) {
        if (id != null) {
            return String.format("id:(%s)", id);
        }
        String[] chunks = str.split("\\s+");
        StringBuilder q = new StringBuilder();
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

    public SearchResponse _search(String q, String id, long offset, int pageSize, List<String> sortOn) throws Exception {
        SolrQuery query = new SolrQuery();
        q = getSearchQuery(q, id);
        System.out.println(q);
        query.set("q", q);
        query.setStart((int) offset);

        QueryResponse response = solr.query(query);
        SolrDocumentList docList = response.getResults();
        List<Document> docs = new ArrayList<>(docList.size());

        for (SolrDocument doc : docList) {
            Document d = new Document();
            d.setId((String) doc.getFieldValue("id"));
            d.setContent((String) ((List) doc.getFieldValue("content")).get(0));
            d.setType((String) ((List) doc.getFieldValue("type")).get(0));
            d.setTags((List<String>) doc.getFieldValue("tags"));
            d.setPostTime((Long) ((List) doc.getFieldValue("ctime")).get(0));
            try {
                d.setOwner((String) ((List) doc.getFieldValue("owner")).get(0));
            } catch (Exception e) {

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
        try {
            solr.add(idoc);
            solr.commit();
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
    }
}
