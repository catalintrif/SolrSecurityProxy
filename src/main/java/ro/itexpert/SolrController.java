package ro.itexpert;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController()
public class SolrController {

    public static final String PROFILES_FIELD = "security";

    @RequestMapping("/search")
    public SolrDocumentList search(
            @RequestParam(name="core", defaultValue = "gettingstarted") String core,
            @RequestParam("q") String query,
            @RequestParam("user") String user)
            throws IOException, SolrServerException {
        String urlString = "http://localhost:8983/solr/" + core;
        HttpSolrClient solr = new HttpSolrClient.Builder(urlString).build();
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.set("q", query + securityQuery(user));

        QueryResponse response = solr.query(solrQuery);
        SolrDocumentList docList = response.getResults();
        for (SolrDocument doc : docList) {
            doc.removeFields(PROFILES_FIELD);
        }
        return docList;
    }

    private String securityQuery(String user) {
        String security = " AND " + PROFILES_FIELD + ":(";
        String[] profiles = getUserProfiles(user);
        for (int i = 0; i < profiles.length-1; i++) {
            security += profiles[i] + " OR ";
        }
        security += profiles[profiles.length-1] + ")";
        return security;
    }


    private String[] getUserProfiles(String user) {
        // TODO replace this test implementation
        if (user.equals("maria")) {
            return new String[]{ "client2role1"};
        } else if (user.equals("vasile")) {
            return new String[]{"client1role1", "client2role1"};
        }
        throw new RuntimeException("User not found");
    }
}
