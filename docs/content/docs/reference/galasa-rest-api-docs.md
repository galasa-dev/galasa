---
title: "Ecosystem REST API documentation"
---

When the Galasa service is deployed on Kubernetes, it exposes a REST API that you can use 
to interact with your ecosystem and perform several tasks, 
for example, retrieving test artifacts, configuring CPS properties, and submitting test runs, 
allowing you to create programmatic integrations with your own applications.

See the [Galasa REST API documentation](./rest-api/index.html) for a detailed definition of the
available commands and message payload structures.

If you have a Galasa service deployed, you can also find the definition of the REST API from your 
service itself, from the endpoint using a URL of the form `https://{myHost}/api/openapi`, where `{myHost}` 
is the name of your hosted service which can be resolved.
