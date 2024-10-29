/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cps.rest;

import static dev.galasa.extensions.common.Errors.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;

import com.google.gson.JsonSyntaxException;

import dev.galasa.extensions.common.Errors;
import dev.galasa.extensions.common.api.HttpClientFactory;
import dev.galasa.extensions.common.api.LogFactory;
import dev.galasa.framework.api.beans.GalasaProperty;
import dev.galasa.framework.api.beans.GalasaPropertyData;
import dev.galasa.framework.api.beans.GalasaPropertyMetadata;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStore;
import dev.galasa.framework.spi.utils.GalasaGson;

import org.apache.commons.logging.Log;

/**
 * This class is used when the RestCPS class is being operated as the Key-Value
 * store for the Configuration property store. This class registers the
 * Configuration property store as the only CPS.
 *
 * This implementation of the CPS interface gets all it's data from a remote 
 * Galasa ecosystem using HTTPS REST calls.
 * 
 * It is used by local test runs who want to run in a hybrid environment such that
 * the CPS properties are shared in a remote ecosystem.
 * 
 * This CPS store is read-only. Write and Delete operations are not supported.
 */
public class RestCPS implements IConfigurationPropertyStore {

    private static final String REST_API_VERSION_CODED_AGAINST = "0.33.0" ; 

    // The prefix to URLs which guide the galasa framework into passing the request to this
    // CPS implementation. 
    //
    // We register this implementation as being associated with this URL schema type.
    // So when the framework sees that the framework.config.store is set to galasacps://myhost/api
    // it knows to instantiate this CPS implementation and direct calls to it.
    //
    // For example: galasacps://myHost/api
    // Such URLs would get turned into https://myhost/api eventually before they are used to 
    // talk to the API endpoint.
    public static final String URL_SCHEMA_REST = "galasacps";

    // Do we want gson sent to the server to be pretty printed ?
    // true because a few extra bytes won't hurt performance of local tests overly.
    public static final boolean PRETTY_PRINTING_ENABLED = true;

    // Symbolic constants used when searching for a property but we don't have a prefix or suffix...etc.
    public static final String NULL_PREFIX = null ;
    public static final String NULL_INFIX = null ;
    public static final String NULL_SUFFIX = null ;

    /** Galasa json adds some serialisation of dates to avoid security vulnerabilities. */
    public GalasaGson gson = new GalasaGson();

    private class PropertyName {
        String namespace;
        String simpleName;
    }

    /** What is the URI to the rest api endpoint ? This will be of the form https://myhost/api */
    private URI ecosystemRestApiUri;

    /** 
     * We create an HTTP client on initialisation of the instance of this CPS implementation,
     * and use that one until the Gaalsa framework shuts us down.
     */
    private CloseableHttpClient apiClient;

    /** The jwt we will use to contact the remote Galasa system. */
    private String jwt; 

    private Log log ;

    /** 
     * A set of property keys which the local test runs should not be reading or using.
     * For these properties, the value of null is returned, implying the value isn't set in the CPS,
     * so the value gets defaulted.
     * The key to the set is the fully-qualified property name (including the namespace)
     */
    private Set<String> redactedPropertyKeys ;

    /**
     * A set of namespaces which cannot be read by a local run.
     * For any queries of these properties, a null is returned, causing defaulted behaviour.
     * The key to the set is the namespace name.
     */
    private Set<String> redactedNamespacesSet;

    public RestCPS(
        URI                 ecosystemRestApiUri, 
        HttpClientFactory   httpClientFactory, 
        JwtProvider         jwtProvider, 
        LogFactory          logFactory
    ) throws ConfigurationPropertyStoreException {

        // Check that the URL passed starts with "galasacps"
        if (!ecosystemRestApiUri.toString().startsWith(URL_SCHEMA_REST+"://")) {
            String msg = ERROR_URI_DOESNT_START_WITH_EXPECTED_SCHEME.getMessage(ecosystemRestApiUri.toString(),URL_SCHEMA_REST+"://");
            throw new ConfigurationPropertyStoreException(msg);
        }

        // Replace the 'galasacps' part with 'https'
        // eg: galasacps://myhost/api gets turned into https://myhost/api
        try {
            this.ecosystemRestApiUri = new URI(ecosystemRestApiUri.toString().replaceAll(URL_SCHEMA_REST,"https"));
        } catch(URISyntaxException ex) {
            String msg = ERROR_URI_IS_INVALID.getMessage(ecosystemRestApiUri.toString(),ex.toString());
            throw new ConfigurationPropertyStoreException(msg, ex);
        }

        this.log = logFactory.getLog(this.getClass());

        this.jwt = jwtProvider.getJwt();
        this.log.info("Got a jwt OK.");

        this.apiClient = httpClientFactory.createClient();

        this.redactedPropertyKeys = createRedactedPropertyKeySet();
        this.redactedNamespacesSet = createRedactedNamespaceSet();
    }

    /**
     * Some property namespaces and specific keys have no part to play in a local test run, so the 
     * values are redacted.
     */
    private Set<String> createRedactedNamespaceSet() {
        Set<String> namespaces = new HashSet<String>();

        // Local runs should never access the secure namespace.
        // Local credentials should be used instead.
        namespaces.add("secure");

        return namespaces;
    }

    private boolean isNamespaceRedacted(String namespaceToCheck) {
        return this.redactedNamespacesSet.contains(namespaceToCheck);
    }

    private Set<String> createRedactedPropertyKeySet() {
        Set<String> keys = new HashSet<String>();

        // Local test runs should not be using the remote DSS.
        keys.add("framework.dynamicstatus.store");

        // Local test runs should not be using the remote creds store.
        keys.add("framework.credentials.store"); 

        return keys;
    }

    private boolean isPropertyRedacted(String fullyQualifiedPropertyName) {
        PropertyName propName = splitPropName(fullyQualifiedPropertyName);
        boolean isKeyRedacted = isNamespaceRedacted(propName.namespace);
        if (isKeyRedacted) {
            log.info("galasacps: over rest : key "+fullyQualifiedPropertyName+" is redacted because that whole namespace is redacted.");
        } else {
            isKeyRedacted = this.redactedPropertyKeys.contains(fullyQualifiedPropertyName);
            if (isKeyRedacted) {
                log.info("galasacps: over rest : key "+fullyQualifiedPropertyName+" is redacted. Not used for local runs.");
            }
        }
        return isKeyRedacted;
    }

    /**
     * This method implements the getProperty method from the framework property
     * file class, returning a string value from a key inside the property file, or
     * null if empty.
     * 
     * @param fullyQualifiedPropertyName The key of the property to get.
     * @throws ConfigurationPropertyStoreException - Something went wrong.
     */
    @Override
    public @Null String getProperty(@NotNull String fullyQualifiedPropertyName) throws ConfigurationPropertyStoreException {

        String propertyValueResult = null ;

        // Some properties are not available to local test runs.
        if (!isPropertyRedacted(fullyQualifiedPropertyName)) {

            // We could have used the /cps/{namespace}/property/{propertyName} endpoint, BUT
            // if the property isn't there, we get a 404 NOT FOUND error, and we can't tell the difference between
            // the endpoint being unavailable/wrong URL, and the property not being set.
            // So we use the /cps/{namespace}/properties?prefix=xxxx so that if the endpoint isn't available, we get 404, 
            // and if the property doesn't exist, then we get null in the map.
            // Although it's not as efficient on the server-side, performance isn't everything in this case, as local runs
            // can be slower/less performant than the ecosystem runs.
            Map<String,String> properties = getPrefixedProperties(fullyQualifiedPropertyName);
            propertyValueResult = properties.get(fullyQualifiedPropertyName);
        }

        return propertyValueResult;
    }

    private void checkResponseHttpCode(CloseableHttpResponse response, Errors errorIfBad, URI targetUri) throws ConfigurationPropertyStoreException {
        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();

        if (statusCode!=HttpStatus.SC_OK) {
            String msg = errorIfBad.getMessage(targetUri.toString(),Integer.toString(statusCode));
            throw new ConfigurationPropertyStoreException(msg);
        }
    }

    private GalasaProperty[] extractPropertiesFromPayload(CloseableHttpResponse response, URI targetUri) throws ConfigurationPropertyStoreException {

        GalasaProperty[] properties ;

        String contentJson ;
        try {
            InputStream inStream = response.getEntity().getContent();
            try {
                contentJson = IOUtils.toString(inStream, "UTF-8");
            } finally {
                // Make sure input streams are always closed.
                inStream.close();
            }

            properties = this.gson.fromJson(contentJson, GalasaProperty[].class);

        } catch(JsonSyntaxException syntaxEx) {
            String msg = ERROR_GALASA_REST_CALL_TO_GET_CPS_PROPERTY_BAD_JSON_RETURNED.getMessage(targetUri.toString(),syntaxEx.getMessage());
            throw new ConfigurationPropertyStoreException(msg,syntaxEx);
        } catch(IOException ioEx) {
            String msg = ERROR_GALASA_REST_CALL_TO_GET_CPS_PROPERTY_FAILED.getMessage(targetUri.toString(),ioEx.getMessage());
            throw new ConfigurationPropertyStoreException(msg,ioEx);
        } 

        checkPropertiesAreWellFormed(properties);

        return properties; 
    }

    /**
     * Double-check that the server returned what we expected. Belt and braces approach.
     * @param properties The properties we want to check.
     * @throws ConfigurationPropertyStoreException Something was wrong with the properties returned from the Galasa server.
     */
    private void checkPropertiesAreWellFormed(GalasaProperty[] properties) throws ConfigurationPropertyStoreException {
        for( GalasaProperty property : properties ) {
            GalasaPropertyMetadata metadata = property.getMetadata();
            if (metadata==null) {
                String msg = Errors.ERROR_GALASA_CPS_PROPERTIES_RETURNED_BADLY_FORMED_METADATA.getMessage();
                throw new ConfigurationPropertyStoreException(msg);
            }
            String namespace = metadata.getNamespace();
            if (namespace==null) {
                String msg = Errors.ERROR_GALASA_CPS_PROPERTIES_RETURNED_BADLY_FORMED_NAMESPACE.getMessage();
                throw new ConfigurationPropertyStoreException(msg);
            }
            String name = metadata.getName();
            if (name==null) {
                String msg = Errors.ERROR_GALASA_CPS_PROPERTIES_RETURNED_BADLY_FORMED_NAME.getMessage();
                throw new ConfigurationPropertyStoreException(msg);
            }
            GalasaPropertyData data = property.getData();
            if (data==null) {
                String msg = Errors.ERROR_GALASA_CPS_PROPERTIES_RETURNED_BADLY_FORMED_DATA.getMessage();
                throw new ConfigurationPropertyStoreException(msg);
            }
            String value = data.getValue();
            if (value==null) {
                String msg = Errors.ERROR_GALASA_CPS_PROPERTIES_RETURNED_BADLY_FORMED_VALUE.getMessage();
                throw new ConfigurationPropertyStoreException(msg);
            }
        }
    }

    private HttpGet constructGetRequest(URI targetUri, String jwt) {
        HttpGet req = new HttpGet();
        req.setURI(targetUri);
        req.addHeader(HttpHeaders.AUTHORIZATION,"Bearer "+this.jwt);
        req.addHeader(HttpHeaders.CONTENT_TYPE,"application/json");
        req.addHeader(HttpHeaders.ACCEPT,"application/json");

        // Tell the server which version of the API we are coded and tested against.
        req.addHeader("ClientApiVersion",REST_API_VERSION_CODED_AGAINST);
        return req;
    }

    /**
     * @param fullyQualifiedPropertyName Of the form namespace.propertyName
     * @return The property name broken into pieces.
     */
    private PropertyName splitPropName(String fullyQualifiedPropertyName ) {
        String[] parts = fullyQualifiedPropertyName.split("[.]");

        PropertyName propName = new PropertyName();
        propName.namespace = parts[0];

        // The simple property name is got by stripping out the leading namespace, plus the '.' separator character.
        propName.simpleName = fullyQualifiedPropertyName.substring( propName.namespace.length()+1 );

        return propName;
    }
    
    @Override
    public @NotNull Map<String, String> getPrefixedProperties(@NotNull String prefixWithNamespace)
            throws ConfigurationPropertyStoreException {
        PropertyName propName = splitPropName(prefixWithNamespace);

        Map<String,String> results = new HashMap<String,String>();

        String namespace = propName.namespace;

        // Some namespaces are not available to local test runs.
        if (!isNamespaceRedacted(namespace)) {
            String prefix = propName.simpleName;
            
            URI targetUri = calculateQueryPropertyUri(namespace, prefix, NULL_SUFFIX, NULL_INFIX);
            HttpGet req = constructGetRequest(targetUri, this.jwt);

            // Note: The response is always closed properly.
            try ( CloseableHttpResponse response = (CloseableHttpResponse) this.apiClient.execute(req) ) {

                checkResponseHttpCode(response, ERROR_GALASA_REST_CALL_TO_GET_CPS_PROPERTIES_FAILED_NON_OK_STATUS, targetUri);

                GalasaProperty[] properties = extractPropertiesFromPayload(response, targetUri);

                results = propertiesToMap(properties);

            } catch(IOException ioEx) {
                String msg = ERROR_GALASA_REST_CALL_TO_GET_CPS_PROPERTIES_FAILED.getMessage(targetUri.toString(),ioEx.getMessage());
                throw new ConfigurationPropertyStoreException(msg,ioEx);
            } 
        }
        
        return results;
    }

    private URI calculateQueryPropertyUri( String namespace, String prefix, String suffix, String infix) throws ConfigurationPropertyStoreException {
        URI targetUri ;
        try {
            // format is /cps/{namespace}/properties?prefix=&suffix=&infix="
            String baseUri = this.ecosystemRestApiUri +"/cps/"+namespace+"/properties";
            URIBuilder uriBuilder = new URIBuilder(baseUri);
            if (prefix!=null) {
                uriBuilder = uriBuilder.addParameter("prefix", prefix);
            }
            if (suffix!=null) {
                uriBuilder = uriBuilder.addParameter("suffix", suffix);
            }
            if (infix!=null) {
                uriBuilder = uriBuilder.addParameter("infix", infix);
            }
            targetUri = uriBuilder.build();
            
        } catch(URISyntaxException ex ) {
            String msg = ERROR_GALASA_CONSTRUCTED_URL_TO_REMOTE_CPS_INVALID_SYNTAX.getMessage(this.ecosystemRestApiUri.toString(),ex.getMessage());
            throw new ConfigurationPropertyStoreException(msg,ex);
        }
        return targetUri;
    }

    /**
     * This method implements the setProperty method from the framework property
     * file class.
     * 
     * @param key The key of the property to be set
     * @param value The value we set the property to
     * @throws ConfigurationPropertyStoreException  - Something went wrong.
     */
    @Override
    public void setProperty(@NotNull String key, @NotNull String value) throws ConfigurationPropertyStoreException {
        String msg = ERROR_GALASA_CPS_SET_OPERATIONS_NOT_PERMITTED.getMessage();
        throw new ConfigurationPropertyStoreException(msg);
    }
    
    @Override
    public void deleteProperty(@NotNull String key) throws ConfigurationPropertyStoreException {
        String msg = ERROR_GALASA_CPS_DELETE_OPERATIONS_NOT_PERMITTED.getMessage();
        throw new ConfigurationPropertyStoreException(msg);
    }

    /**
     * This method returns all properties for a given namespace from the framework property
     * file class.
     * 
     * @param namespace The namespace we want to get the property from.
     * @return The properties returned.
     * @throws ConfigurationPropertyStoreException if there was a problem accessing the CPS
     */
    @Override
    public Map<String,String> getPropertiesFromNamespace(String namespace) throws ConfigurationPropertyStoreException {

        Map<String,String> results = new HashMap<String,String>();

        URI targetUri = calculateQueryPropertyUri(namespace, NULL_PREFIX, NULL_SUFFIX, NULL_INFIX);
        HttpGet req = constructGetRequest(targetUri, this.jwt);

        // Note: The response is always closed properly.
        try ( CloseableHttpResponse response = (CloseableHttpResponse) this.apiClient.execute(req) ) {

            checkResponseHttpCode(response, ERROR_GALASA_REST_CALL_TO_GET_ALL_CPS_PROPERTIES_NON_OK_STATUS, targetUri);
            GalasaProperty[] properties = extractPropertiesFromPayload(response, targetUri);
            results = propertiesToMap(properties);

        } catch(IOException ioEx) {
            String msg = ERROR_GALASA_REST_CALL_TO_GET_CPS_PROPERTIES_FAILED.getMessage(targetUri.toString(),ioEx.getMessage());
            throw new ConfigurationPropertyStoreException(msg,ioEx);
        }

        return results;
    }

    private Map<String,String> propertiesToMap(GalasaProperty[] properties) throws ConfigurationPropertyStoreException {
        Map<String,String> results = new HashMap<String,String>();

        for( GalasaProperty property : properties ) {
            // Calculate the fully qualified property name from the metadata available.
            GalasaPropertyMetadata metadata = property.getMetadata();
            String namespace = metadata.getNamespace();
            String name = metadata.getName();
            String fullyQualifiedPropName = namespace + "." + name;

            GalasaPropertyData data = property.getData();
            String value = data.getValue();

            // log.info("galasacps: over rest (with prefix): "+fullyQualifiedPropName+" : "+value);

            if (!isPropertyRedacted(fullyQualifiedPropName)) {
                results.put(fullyQualifiedPropName, value);
            }
        }
        return results;
    }

    /**
     * Return all Namespaces for the framework property file
     * 
     * @return - List of namespaces
     * @throws ConfigurationPropertyStoreException if there was a problem accessing the CPS
     */
    @Override
    public List<String> getNamespaces() throws ConfigurationPropertyStoreException {
        List<String> results = new ArrayList<String>();

        URI targetUri = calculateQueryNamespaceUri();
        HttpGet req = constructGetRequest(targetUri, this.jwt);

        // Note: The response is always closed properly.
        try ( CloseableHttpResponse response = (CloseableHttpResponse) this.apiClient.execute(req) ) {

            checkResponseHttpCode(response, ERROR_GALASA_REST_CALL_TO_GET_ALL_CPS_NAMESPACES_NON_OK_STATUS, targetUri);
            results = extractNamespacesFromPayload(response, targetUri);

        } catch(IOException ioEx) {
            String msg = ERROR_GALASA_REST_CALL_TO_GET_CPS_NAMESPACES_FAILED.getMessage(targetUri.toString(),ioEx.getMessage());
            throw new ConfigurationPropertyStoreException(msg,ioEx);
        } 

        return results;
    }

    private List<String> extractNamespacesFromPayload(CloseableHttpResponse response, URI targetUri) throws ConfigurationPropertyStoreException {

        List<String> namespaces = new ArrayList<String>();

        String contentJson ;
        try {
            InputStream inStream = response.getEntity().getContent();
            try {
                contentJson = IOUtils.toString(inStream, "UTF-8");
            } finally {
                // Make sure input streams are always closed.
                inStream.close();
            }

            String[] namespacesArray = this.gson.fromJson(contentJson, String[].class);

            for(String namespace : namespacesArray) {
                namespaces.add(namespace);
            }

        } catch(JsonSyntaxException syntaxEx) {
            String msg = ERROR_GALASA_REST_CALL_TO_GET_CPS_NAMESPACES_BAD_JSON_RETURNED.getMessage(targetUri.toString(),syntaxEx.getMessage());
            throw new ConfigurationPropertyStoreException(msg,syntaxEx);
        } catch(IOException ioEx) {
            String msg = ERROR_GALASA_REST_CALL_TO_GET_CPS_NAMESPACES_FAILED.getMessage(targetUri.toString(),ioEx.getMessage());
            throw new ConfigurationPropertyStoreException(msg,ioEx);
        } 

        return namespaces; 
    }

    private URI calculateQueryNamespaceUri() throws ConfigurationPropertyStoreException {
        URI targetUri ;
        try {
            String baseUri = this.ecosystemRestApiUri +"/cps/namespace/";
            targetUri = new URI(baseUri);            
        } catch(URISyntaxException ex ) {
            String msg = ERROR_GALASA_CONSTRUCTED_URL_TO_REMOTE_CPS_INVALID_SYNTAX.getMessage(this.ecosystemRestApiUri.toString(),ex.getMessage());
            throw new ConfigurationPropertyStoreException(msg,ex);
        }
        return targetUri;
    }


    @Override
    public void shutdown() throws ConfigurationPropertyStoreException {
        try {
            if (this.apiClient!=null) {
                this.apiClient.close();
            }
        } catch(IOException ioEx) {
            String msg = Errors.ERROR_GALASA_CPS_SHUTDOWN_FAILED.getMessage(ioEx.getMessage());
            throw new ConfigurationPropertyStoreException(msg,ioEx);
        }
    }
}
