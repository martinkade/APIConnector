# APIConnector
Framework to make async http requests sending and receiving Java objects.
These objects will be encoded to json strings and decoded from json strings, respectively, using Java annotations.

## Usage
### Defining an entity class
To start with, you have to specify an entity class using annotations to identify its attributes.
```java
public class TestEntity implements APIService.Entity {
  
    @JSONAttribute(name = "user")
    private String user;
    
    /*...*/
}
```
### Sending an object
Then setup the service instance itself and the delegate object, either by implementing its methods at class level
or as object (see below).
```java
final APIService.Delegate delegate = new APIService.Delegate<TestEntity>() { /*...*/ };
final APIService<TestEntity> service = new APIService<>(delegate);

/*...*/
```
Then you are ready to build a post request, for instance, setting some parameters like timeout or charset. You can post Java
objects directly as long as the class of each of these objects is implementing the `APIService.Entity` interface. Alternatively,
you can post a plain `java.util.Map` by calling `post.setRawRequestData(map)`.
```java
/*...*/

final APIPostRequest<TestEntity> post = new APIPostRequest<>("http://example.com/data.php", TestEntity.class);
post.setTimeoutSeconds(5);
post.addUrlParam("key", "value");

// init entity object to be posted
final TestEntity e = new TestEntity();
e.setUser("martinkade");
e.setPassword("1***56");

post.setRequestData(e);

/*...*/
```
Then you are ready to execute the request via service asynchroneously. The delegate object will be notifyed when response is here.
You can tag the request with a string in order to be able to identify it in the delegate method implementation.
```java
/*...*/

service.exec(post, 0, "post_test");
```
### Receiving an object
You can access the entity objects through the delegate methods.
```java
@Override
public void apiServiceDidReceiveResponse(TestEntity obj, long execTimeMillis, String id, int httpStatusCode) {
    /* TODO: do something with the data */
}

@Override
public void apiServiceDidThrowException(APIException ex, String id, int httpStatusCode) { /*...*/ }
```
