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
    @Value("${solr.max.results}")
    private int MAX_RESULTS;

    @Autowired
    private UserList users;

    @RequestMapping("/search")
    public SolrDocumentList search(
            @RequestParam(name="core", defaultValue = "gettingstarted") String core,
            @RequestParam("q") String query,
            @RequestParam("user") String userName)
            throws IOException, SolrServerException {
        User user = getUser(userName);
        if (user.getCredits() == 0) {
            throw new RuntimeException("No more credits");
        }
        String urlString = SOLR_URL + core;
        HttpSolrClient solr = new HttpSolrClient.Builder(urlString).build();
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.set("q", query + securityQuery(user));
        solrQuery.set("rows", MAX_RESULTS);
        QueryRequest req = new QueryRequest(solrQuery);
        req.setBasicAuthCredentials(USER_NAME, PASSWORD);
        QueryResponse response = req.process(solr);

        SolrDocumentList docList = response.getResults();
        // hide some fields
        docList.forEach(doc -> doc.removeFields(PROFILES_FIELD));
        user.spendCredit(docList.size());
        users.persistAsync();
        System.out.println("Returning search results");
        return docList;
    }

    private String securityQuery(User user) {
        List profiles = user.getProfiles();
        String securityString = " AND " + PROFILES_FIELD + ":("
                + profiles.stream().collect(Collectors.joining(" OR "))
                + ")";
        return securityString;
    }


    private User getUser(String user) {
        return users.getUsers().stream().parallel()
                .filter(u -> u.getUserName().equals(user))
                .findAny()
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @RequestMapping("/reload")
    public String reloadUsers() throws IOException, SolrServerException, InterruptedException {
        users.persistAsync();
        Thread.sleep(3000);
        return "Users loaded: " + users.loadUsers();
    }
}
