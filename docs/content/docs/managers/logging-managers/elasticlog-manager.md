---
path: "/docs/managers/elasticlog-manager"
title: "ElasticLog Manager"
---

This Manager is at Alpha level. <br>


[Overview](#overview)<br>
[Configuring](#configuring)<br>


# <a name="overview"></a>Overview
This Manager exports test results to an elastic search endpoint, where the data can be visualized on a Kibana dashboard.  Other Managers can contribute to the information that is exported to Elastic. <br><br> As an absolute minimum, the CPS properties <br> <code>elasticlog.endpoint.address</code><br>and<br><code>elasticlog.endpoint.index</code><br> must be provided. By default, this Manager only logs automated tests. To enable logging from locally run tests, <br> <code>elasticlog.local.run.log</code> must be set to true.<br> The bundle must also be loaded by the framework by using<br> <code>framework.extra.bundles=dev.galasa.elasticlog.manager</code><br> in bootstrap.properties. <br><br> This Manager provides two ElasticSearch indexes; one of all test data, and one of the latest run for each test case and each  test environment.

## Limitations
The Manager logs the following test information:<br> <br> - testCase<br> - runId<br> - startTimestamp<br> - endTimestamp<br> - requestor<br> - result<br> - testTooling<br> - testType<br> - testingEnvironment<br> - productRelease<br> - buildLevel<br> - customBuild<br> - testingAreas<br> - tags<br> <br> If additional testing information is required, please raise a GitHub issue.<br><br> 





## <a name="configuring"></a>Configuration Properties

The following are properties used to configure the ElasticLog Manager.
 
<details>
<summary>ElasticLog Endpoint Address CPS Property</summary>

| Property: | ElasticLog Endpoint Address CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | elastic.endpoint.address |
| Description: | Provides an address to send elastic requests to |
| Required:  | Yes |
| Default value: | $default |
| Valid values: | Any valid URI string |
| Examples: | <code>elastic.endpoint.address=https://yoursitehere.com/elasticendpoint</code> |

</details>
 
<details>
<summary>ElasticLog Endpoint Index CPS Property</summary>

| Property: | ElasticLog Endpoint Index CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | elastic.endpoint.index |
| Description: | Provides the index in elasticsearch to which requests are directed |
| Required:  | Yes |
| Default value: | $default |
| Valid values: | Any lowercase, single-word string |
| Examples: | <code>elastic.endpoint.index=galasa</code> |

If the index does not exist, the index is created and is mapped to the Galasa run.</br> If the index exists, it must be mapped to the relevant Galasa run.

</details>
 
<details>
<summary>ElasticLog Endpoint Local Run CPS Property</summary>

| Property: | ElasticLog Endpoint Local Run CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | elastic.local.run.log |
| Description: | Activates the ElasticLog Manager for local runs |
| Required:  | Yes |
| Default value: | false |
| Valid values: | true, false |
| Examples: | <code>elastic.local.run.log=true</code> |

ElasticLog Manager will not run automatically for a local run. <br> By setting this property to true, the manager will activate locally.

</details>
