<details><summary>Obtain a Kubernetes Namespace</summary>

```java
@KubernetesEcosystem
public IKubernetesEcosystem ecosystem;
    
@KubernetesNamespace
public IKubernetesNamespace namespace;
```

This code requests that the Galasa Ecosystem be provisioned in a Kubernetes Namespace. The default tag for both of them is 
PRIMARY.
</details>

<details><summary>Retrieve the RAS Endpoint</summary>

```java
@KubernetesEcosystem
public IKubernetesEcosystem ecosystem;

URI ras = ecosystem.getEndpoint(EcosystemEndpoint.RAS);

```

This snippet demonstrates how to retrieve the Result Archive Store (RAS) endpoint. Be aware, that the URI is 
prefixed with the store type, e.g. couchdb:http://couchdb.server:5984. This is the same for the CPS, DSS and CREDS.
</details>

<details><summary>Set and retrieve a CPS property</summary>

```java
ecosystem.setCpsProperty("bob", "hello");

String value = ecosystem.getCpsProperty("bob")
```

Sets the CPS property `bob` with the value `hello` and retrieves it again.
</details>