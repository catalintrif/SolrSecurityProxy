# Solr security proxy

### Solr configuration

The target Solr server is indicated by the following properties in application.properties configuration file:
```
solr.url = http://localhost:8983/solr/
solr.user = solr
solr.password = solr123
```
The documents must have the "security" field defined, which is holding the allowed user roles.
```
{
        "id": "10",
        "name": [
          "maria"
        ],
        "security": [
          "client1role1"
        ]
}
```
Any core can be used by passing the "core" parameter to the /search REST API. 

#### User profiles source
On startup, the service will load the user profiles from a Solr schema indicated by the following properties in application.properties configuration file:
```
solr.users.url = http://localhost:8983/solr/users
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
For each successful search, the "credits" field of the user is decremented with the number of results.

To improve Solr update performance by taking advantage of [in-place updates](https://lucene.apache.org/solr/guide/6_6/updating-parts-of-documents.html), the "credits" field should be defined as:
```
<field name="credits" type="float" indexed="false" stored="false" docValues="true"/>
```  

### Building
> mvn clean package

### Running
Example:
> java -jar SolrProxy-0.1.jar

To run or debug from the IDE:
> mvn spring-boot:run

### Testing
By default the service will be running on port 8080.

Example URL: [http://localhost:8080/search?core=docs&q=name:*&user=ion]()

To enable basic authentication for the proxy service, change the setting to ```  auth.required=true ``` and set the credentials.

After modifying the number of credits for a user in Solr, a reload of the service is mandatory:
[localhost:9000/reload]()

#### Docker test environment
Setup test Solr instance on port 8983 and create 2 cores:
```
docker run --name test_solr -d -p 8983:8983 -t solr
docker exec -it my_solr solr create_core -c docs
docker exec -it my_solr solr create_core -c users
```
Get the hostname of the Docker machine:
```
docker exec -it my_solr hostname
5b5a16200f56
```
Use ```curl``` to populate test users and data:

```
docker exec -it my_solr curl "5b5a16200f56:8983/solr/users/update?commit=true" -H "Contenttype: application/json" -d "[{ "id": "1", "user": [ "ana" ], "roles": [ "client2role1" ], "credits": [ 100 ] }]" 
docker exec -it my_solr curl "5b5a16200f56:8983/solr/users/update?commit=true" -H "Contenttype: application/json" -d "[{ "id": "2", "user": "ion", "roles": "client1role1", "credits": 50 }]"

docker exec -it my_solr curl "5b5a16200f56:8983/solr/docs/update?commit=true" -H "Contenttype: application/json" -d "{ commit: {}, add: { doc: { id:1, name:'Ion Popescu', security:client1role1 }},  add: {doc: { id: 2, name: 'Maria Ionescu', security:[client1role1, client2role1]}}}"
```