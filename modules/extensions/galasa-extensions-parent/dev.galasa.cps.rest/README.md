# CPS over REST adapter

This extension allows a local test to run, while drawing configuration data from the remote CPS
on a Galasa server.

The configuration on the ecosystem is considered 'shared'.

The configuration on the ecosystem is read-only. Set and Delete operations are not implemented.

To configure Galasa to load and use this adapter:

The galasactl tool can be configured to communicate with that CPS (see the latest docs on the galasactl tool).

To do this, assuming `https://myhost/api/bootstrap` can be used to 
communicate with the remote server, add the following to your `bootstrap.properties` file, 
```
# Tell the galasactl tool that local tests should use the REST API to get shared configuration properties from a remote server.
# https://myhost/api is the location of the Galasa REST API endpoints.
framework.config.store=galasacps://myhost/api
# Tells the framework to load this extension, so it can register to react when the `galasacps` URL scheme is used.
framework.extra.bundles=dev.galasa.cps.rest
```

The CPS over REST feature has a cache which can be turned on using the `framework.cps.rest.cache.is.enabled` property.
- Set it to `true` to enable caching of CPS properties on the client-side, with an agressive cache-priming which loads all
CPS properties into the cache at the start.
- Set it to `false` or don't have that property in your CPS store, and the caching will be disabled.



