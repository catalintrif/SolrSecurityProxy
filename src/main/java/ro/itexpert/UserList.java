package ro.itexpert;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
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

    public UserList() {}

    @PostConstruct
    public int loadUsers() throws IOException, SolrServerException {
        System.out.println("Loading users...");
        HttpSolrClient solr = new HttpSolrClient.Builder(SOLR_URL).build();
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.set("q", "*");
        QueryRequest req = new QueryRequest(solrQuery);
        req.setBasicAuthCredentials(USER_NAME, PASSWORD);
        QueryResponse response = req.process(solr);

        SolrDocumentList docList = response.getResults();
        users = docList.stream().parallel()
                .filter(doc -> doc.containsKey("user"))
                .map(doc -> new User(
                        (String) ((List) doc.getFieldValue("user")).get(0),
                        doc.getFieldValues("roles").stream()
                                .map(role -> (String) role)
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
        return users.size();
    }

    public List<User> getUsers() {
        return users;
    }
}
