---
title: "ElasticLog Manager"
---

## Overview

This Manager exports test results to an elastic search endpoint, where the data can be visualized on a Kibana dashboard.  Other Managers can contribute to the information that is exported to Elastic.

As an absolute minimum, the CPS properties `elasticlog.endpoint.address` and `elasticlog.endpoint.index` must be provided.

By default, this Manager only logs automated tests. To enable logging from locally run tests, `elasticlog.local.run.log` must be set to true.

The bundle must also be loaded by the framework by using `framework.extra.bundles=dev.galasa.elasticlog.manager` in bootstrap.properties.

This Manager provides two ElasticSearch indexes; one of all test data, and one of the latest run for each test case and each test environment.


## Limitations

The Manager logs the following test information:

- testCase
- runId
- startTimestamp
- endTimestamp
- requestor
- result
- testTooling
- testType
- testingEnvironment
- productRelease
- buildLevel
- customBuild
- testingAreas
- tags

If additional testing information is required, please raise a GitHub issue.


## Configuration Properties

The following are properties used to configure the ElasticLog Manager.


### ElasticLog Endpoint Address CPS Property

| Property: | ElasticLog Endpoint Address CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | elastic.endpoint.address |
| Description: | Provides an address to send elastic requests to |
| Required:  | Yes |
| Default value: | $default |
| Valid values: | Any valid URI string |
| Examples: | `elastic.endpoint.address=https://yoursitehere.com/elasticendpoint` |


### ElasticLog Endpoint Index CPS Property

| Property: | ElasticLog Endpoint Index CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | elastic.endpoint.index |
| Description: | Provides the index in elasticsearch to which requests are directed |
| Required:  | Yes |
| Default value: | $default |
| Valid values: | Any lowercase, single-word string |
| Examples: | `elastic.endpoint.index=galasa` |

If the index does not exist, the index is created and is mapped to the Galasa run.

If the index exists, it must be mapped to the relevant Galasa run.


### ElasticLog Endpoint Local Run CPS Property

| Property: | ElasticLog Endpoint Local Run CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | elastic.local.run.log |
| Description: | Activates the ElasticLog Manager for local runs |
| Required:  | Yes |
| Default value: | false |
| Valid values: | true, false |
| Examples: | `elastic.local.run.log=true` |

ElasticLog Manager will not run automatically for a local run.

By setting this property to true, the manager will activate locally.

