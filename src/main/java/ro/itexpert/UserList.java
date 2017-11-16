package ro.itexpert;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
public class UserList {

    @Value("${solr.users.url}")
    private String SOLR_URL;
    @Value("${solr.users.user}")
    private String USER_NAME;
    @Value("${solr.users.password}")
    private String PASSWORD;

    private List<User> users;
    private HttpSolrClient solr;

    public UserList() {}

    @PostConstruct
    private void init() throws IOException, SolrServerException {
        solr = new HttpSolrClient.Builder(SOLR_URL).build();
        loadUsers();

    }

    public int loadUsers() throws IOException, SolrServerException {
        System.out.println("Loading users...");
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.set("q", "*");
        QueryRequest req = new QueryRequest(solrQuery);
        req.setBasicAuthCredentials(USER_NAME, PASSWORD);
        QueryResponse response = req.process(solr);

        SolrDocumentList docList = response.getResults();
        users = docList.stream().parallel()
                .filter(doc -> doc.containsKey("credits"))
                .map(doc -> new User(
                        (String) doc.getFieldValue("id"),
                        (String) ((List) doc.getFieldValue("user")).get(0),
                        doc.getFieldValues("roles").stream()
                                .map(role -> (String) role)
                                .collect(Collectors.toList()),
                        (Long) ((List) doc.getFieldValue("credits")).get(0)))
                .collect(Collectors.toList());
        return users.size();
    }

    public List<User> getUsers() {
        return users;
    }

    /**
     * Uses a different thread to persist the <code>credits</code> field of users that are flagged as dirty.
     */
    public void persistUsersAsync() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            System.out.print("Updating users asynchronously...");
            try {
                Long affected = users.stream()
                        .filter(user -> user.isDirty())
                        .map(user -> {
                            // create the document
                            SolrInputDocument sdoc = new SolrInputDocument();
                            sdoc.addField("id", user.getId());
                            Map<String, Object> fieldModifier = new HashMap<>(1);
                            fieldModifier.put("set", user.getCredits());
                            sdoc.addField("credits", fieldModifier);  // add the map as the field value
                            UpdateRequest req = new UpdateRequest();  // create request
                            req.add(sdoc);
                            req.setBasicAuthCredentials(USER_NAME, PASSWORD);
                            try {
                                req.process(solr);  // send it to the solr server
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            user.setDirty(false);
                            return user;
                        })
                        .collect(Collectors.counting());
                solr.commit();
                System.out.println(affected.toString() + " users updated");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
