# Solr security proxy

### Solr configuration

The target Solr server is indicated by the following properties in application.properties configuration file:
```
solr.url = http://localhost:8983/solr/
solr.user = solr
solr.password = solr123
```
The documents must have the "security" field defined, which is holding the allowed user roles.
Any core can be used by passing the "core" parameter to the /search REST API. 

#### User profiles source
On startup, the service will load the user profiles from a Solr schema indicated by the following properties in application.properties configuration file:
```
solr.users.url = http://localhost:8983/solr/gettingstarted
solr.users.user = solr
solr.users.password = solr123
``` 
The user objects in Solr should be defined as:
```
{
        "id": 1,
        "user": "ana",
        "roles": [
          "client2role1"
        ],
        "credits": 50
}
```
For each successful search, the "credits" field of the user is decremented.

To improve Solr update performance by taking advantage of [in-place updates](https://lucene.apache.org/solr/guide/6_6/updating-parts-of-documents.html), the "credits" field should be defined as:
```
<field name="credits" type="float" indexed="false" stored="false" docValues="true"/>
```  

### Building
> mvn clean package

### Running
Example:
> java -jar target\SolrProxy-0.1.jar

To run or debug from the IDE:
> mvn spring-boot:run

### Testing
By default the service will be running on port 8080.

Example: [http://localhost:8080/search?core=poc&q=name:Ana&user=maria]()
