package ro.itexpert;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@RestController()
public class SolrController {

    public static final String SOLR_URL = "http://localhost:8983/solr/";
    public static final String USER_NAME = "solr";
    public static final String PASSWORD = "solr123";
    public static final String PROFILES_FIELD = "security";

    @RequestMapping("/search")
    public SolrDocumentList search(
            @RequestParam(name="core", defaultValue = "gettingstarted") String core,
            @RequestParam("q") String query,
            @RequestParam("user") String user)
            throws IOException, SolrServerException {
        String urlString = SOLR_URL + core;
        HttpSolrClient solr = new HttpSolrClient.Builder(urlString).build();
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.set("q", query + securityQuery(user));
        QueryRequest req = new QueryRequest(solrQuery);
        req.setBasicAuthCredentials(USER_NAME, PASSWORD);
        QueryResponse response = req.process(solr);

        SolrDocumentList docList = response.getResults();
        for (SolrDocument doc : docList) {
            // hide some fields
            doc.removeFields(PROFILES_FIELD);
        }
        return docList;
    }

    private String securityQuery(String user) {
        List profiles = getUserProfiles(user);
        String security = " AND " + PROFILES_FIELD + ":("
                + profiles.stream().collect(Collectors.joining(" OR "))
                + ")";
        return security;
    }


    private List<String> getUserProfiles(String user) {
        // TODO replace this test implementation
        List<User> users = asList(new User("maria", asList("client2role1")),
                new User("vasile", asList("client1role1", "client2role1")));

        List<List<String>> userFound = users.stream()
                .filter(u -> u.userName.equals(user))
                .map(u -> u.profiles)
                .collect(Collectors.toList());
        if (userFound.size() == 0) {
            throw new RuntimeException("User not found");
        } else {
            return userFound.get(0);
        }
    }
}
