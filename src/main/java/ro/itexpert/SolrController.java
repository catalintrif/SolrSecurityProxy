package ro.itexpert;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController()
public class SolrController {

    @Value("${solr.url}")
    private String SOLR_URL;
    @Value("${solr.users.user}")
    private String USER_NAME;
    @Value("${solr.password}")
    private String PASSWORD;
    private String PROFILES_FIELD = "security";

    @Autowired
    private UserList users;

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
                + profiles.parallelStream().collect(Collectors.joining(" OR "))
                + ")";
        return security;
    }


    private List<String> getUserProfiles(String user) {
        List<List<String>> userFound = users.getUsers().stream().parallel()
                .filter(u -> u.userName.equals(user))
                .map(u -> u.profiles)
                .collect(Collectors.toList());
        if (userFound.size() == 0) {
            throw new RuntimeException("User not found");
        } else {
            return userFound.get(0);
        }
    }

    @RequestMapping("/reload")
    public String reloadUsers() throws IOException, SolrServerException {
        return "Users loaded: " + users.loadUsers();
    }
}
