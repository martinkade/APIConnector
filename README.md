# HTTPJson
Framework to make async HTTP requests sending and receiving Java objects.
These objects will be encoded to JSON strings and decoded from JSON strings, respectively, using Java annotations.

## Usage
### Defining an entity class
To start with, one has to specify an entity class using annotations to identify relevant attributes.
```java
public class ResponseEntity implements APIService.Entity {
  
    @JSONAttribute(name = "status")
    private String status;
    
    /*...*/
}
```
### Sending an object
Then setup the service instance itself and the delegate object, either by implementing its methods at class level
or as object (see below).
```java
final APIService.Delegate delegate = new APIService.Delegate<ResponseEntity>() { /*...*/ };
final APIService<ResponseEntity, RequestEntity> service = new APIService<>(delegate);

/*...*/
```
Then you are ready to build a post request, for instance, setting some parameters like timeout or charset. You can post Java
objects directly as long as the class of each of these objects is implementing the `APIService.Entity` interface. Alternatively,
you can post a plain `java.util.Map` by calling `post.setRawRequestData(map)`.
```java
/*...*/

final APIPutRequest<ResponseEntity, RequestEntity> put = new APIPostRequest<>("http://api.example.com/data", ResponseEntity.class);
put.setTimeoutSeconds(5);
put.addUrlParam("key", "value");

// init entity object to be posted
final RequestEntity e = new RequestEntity();
e.setFoo("foo");
e.setBar("bar");

put.setRequestData(e);

/*...*/
```
Then you are ready to execute the request via service asynchroneously. The delegate object will be notifyed when response is here.
You can tag the request with a string in order to be able to identify it in the delegate method implementation.
```java
/*...*/

service.exec(put, "put-test");
```
### Receiving an object
You can access the entity objects through the delegate methods.
```java

@Override
public void apiServiceWillSendResponse(Runnable runnable) { /*...*/ }

@Override
public void apiServiceDidReceiveResponse(ResponseEntity obj, long execTimeMillis, String id, int httpStatusCode) {
    /* TODO: do something with the data */
}

@Override
public void apiServiceDidThrowException(APIException ex, String id, int httpStatusCode) { /*...*/ }
```
