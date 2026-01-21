---
title: "HTTP Client Manager"
---

This Manager is at Release level. You can view the [Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/http/package-summary.html){target="_blank"}.


## Overview

This Manager provides a variety of common HTTP client operations you can use in your tests.
For example, you can use this Manager in a test where you want to determine if a particular web page contains (or does not contain) some specific content.
This is exactly how it is used in the [Docker Manager IVT](https://github.com/galasa-dev/managers/blob/main/galasa-managers-parent/galasa-managers-cloud-parent/dev.galasa.docker.manager.ivt/src/main/java/dev/galasa/docker/manager/ivt/DockerManagerIVT.java){target="_blank"} (Installation Verification Test).
As well as providing client functionality to people who write tests, it may also be used internally by other Managers to enrich their range of offered services.
This Manager supports outbound HTTP calls, JSON requests, HTTP file transfer and Web Services calls. SSL is supported.

## Code snippets

Use the following code snippets to help you get started with the HTTP Client Manager.
 

### Instantiate an HTTP Client

This code instantiates an HTTP Client.

```java
@HttpClient
public IHttpClient client;
```

You can just as simply instantiate multiple HTTP Clients.

```java
@HttpClient
public IHttpClient client1;

@HttpClient
public IHttpClient client2;
```


### Set the target URI for an HTTP Client

This code sets an HTTP Client's target URI.

```java
@HttpClient
public IHttpClient client;

client.setURI(new URI("http://www.google.com"));
```

You would typically use this call prior to, say, an outbound HTTP call
to retrieve the contents of a web page.


### Make an outbound HTTP call

This code makes a get request to the given path.

```java
client.setURI(new URI("https://httpbin.org"));
String pageContent = client.getText("/get").getContent();
```

These two lines use the HTTPClient to perform a GET request against the URL https://httpbin.org/get.
The getText method is used as we want to retrieve the response as a string.  Alternatives for XML, JSON and JAXB objects exist.
There are also methods for the other HTTP verbs such as PUT, POST and DELETE


### Use streams to download a file

The following code is an example of one way to download a file using streams.

```java
@HttpClient
public IHttpClient client;

File f = new File("/tmp/dev.galasa_0.7.0.jar");

client.setURI(new URI("https://p2.galasa.dev"));
// Note: The response closes when we leave the code block.
try( CloseableHttpResponse response = client.getFile("/plugins/dev.galasa_0.7.0.jar")) {
  // Note: The input stream auto-closes when we leave the code block.
  try (InputStream in = response.getEntity().getContent()) {
    // Note: The output stream auto-closes when we leave the code block.
    try (OutputStream out = new FileOutputStream(f)) { 
      int count;
      byte data[] = new byte[2048];
      while((count = in.read(data)) != -1) {
          out.write(data, 0, count);
      }
    }
  }
}
```

The snippet begins by declaring `client` as before and `f`, an instance of `File`. The client's URI is set and its `getFile` method called to return `response` - an instance of `CloseableHttpResponse`.

The two streams `in` and `out` are declared and initialized and the data transferred from `in` to `out` in 2048 byte chunks, after which the output stream is flushed and then closed.

