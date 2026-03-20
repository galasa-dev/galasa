---
title: "HTTP Client Manager"
---

You can view the [Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/http/package-summary.html){target="_blank"}.


## Overview

This Manager provides a variety of common HTTP client operations you can use in your tests.
For example, you can use this Manager in a test where you want to determine if a particular web page contains (or does not contain) some specific content.
This is exactly how it is used in the [Docker Manager IVT](https://github.com/galasa-dev/galasa/tree/main/modules/managers/galasa-managers-parent/galasa-managers-cloud-parent/dev.galasa.docker.manager.ivt/src/main/java/dev/galasa/docker/manager/ivt/DockerManagerIVT.java){target="_blank"} (Installation Verification Test).
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

The following code shows how to download a file using the streaming API.

```java
@HttpClient
public IHttpClient client;

File f = new File("/tmp/dev.galasa_0.7.0.jar");

client.setURI(new URI("https://p2.galasa.dev"));

// Use try-with-resources to ensure the response is properly closed
try (HttpFileResponse response = client.getFileStream("/plugins/dev.galasa_0.7.0.jar")) {
    if (response.isSuccessful()) {
        // Get the input stream from the response
        InputStream in = response.getContent();

        // Create output stream for the file
        try (OutputStream out = new FileOutputStream(f)) {
            int count;
            byte data[] = new byte[2048];

            // Transfer data in 2048 byte chunks
            while ((count = in.read(data)) != -1) {
                out.write(data, 0, count);
            }
        }
    } else {
        fail("Download failed with response code " + response.getStatusCode());
    }
}
```

The snippet begins by declaring `client` and `f`, an instance of `File`. The client's URI is set and its `getFileStream` method is called, which returns an `HttpFileResponse` instance.

The response is used in a try-with-resources block to ensure proper cleanup. The `isSuccessful()` method checks if the HTTP status code indicates success (2xx or 3xx). If successful, the input stream is obtained from the response and the data is transferred to the output file in 2048 byte chunks.

You can also specify accepted content types:

```java
try (HttpFileResponse response = client.getFileStream("/path/to/file",
        ContentType.APPLICATION_OCTET_STREAM, ContentType.APPLICATION_X_TAR)) {
    if (response.isSuccessful()) {
        // Process the file
        InputStream in = response.getContent();
        // ... use the stream
    }
}
```
