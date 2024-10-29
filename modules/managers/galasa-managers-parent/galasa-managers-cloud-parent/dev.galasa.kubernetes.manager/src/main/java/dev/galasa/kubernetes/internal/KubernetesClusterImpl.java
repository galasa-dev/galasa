/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.kubernetes.internal;

import java.net.URL;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsToken;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourcePoolingService;
import dev.galasa.framework.spi.InsufficientResourcesAvailableException;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;
import dev.galasa.kubernetes.KubernetesManagerException;
import dev.galasa.kubernetes.internal.properties.KubernetesCredentials;
import dev.galasa.kubernetes.internal.properties.KubernetesMaxSlots;
import dev.galasa.kubernetes.internal.properties.KubernetesNamespaces;
import dev.galasa.kubernetes.internal.properties.KubernetesNodePortProxy;
import dev.galasa.kubernetes.internal.properties.KubernetesUrl;
import dev.galasa.kubernetes.internal.properties.KubernetesValidateCertificate;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.JSON;
import io.kubernetes.client.util.Config;

/**
 * Represents a Kubernetes Cluster
 * 
 *  
 *
 */
public class KubernetesClusterImpl {

    private final Log                        logger = LogFactory.getLog(getClass());

    private final String                     clusterId;
    private final IDynamicStatusStoreService dss;
    private final IFramework                 framework;
    
    private ApiClient                        apiClient;

    public KubernetesClusterImpl(String clusterId, IDynamicStatusStoreService dss, IFramework framework) {
        this.clusterId = clusterId;     
        this.dss       = dss;
        this.framework = framework;
    }

    public String getId() {
        return this.clusterId;
    }

    /**
     * Return the current availability of namespaces in the cluster
     * 
     * @return a percentage of available namespaces from 0.0-1.0, or null if there is no availability
     * @throws KubernetesManagerException there is problem accessing the CPS or DSS 
     */
    public Float getAvailability() throws KubernetesManagerException {

        try {
            int maxSlots = KubernetesMaxSlots.get(this);
            int currentSlots = 0;
            String sCurrentSlots = dss.get("cluster." + this.clusterId + ".current.slots");
            if (sCurrentSlots != null) {
                currentSlots = Integer.parseInt(sCurrentSlots);
            }

            if (currentSlots >= maxSlots) { 
                return null; // so fuzzy floats don't get involved
            }

            return 1.0f - (((float)currentSlots) / ((float)maxSlots));
        } catch (Exception e) {
            throw new KubernetesManagerException("Unable to determine current slot count for cluster " + this.clusterId, e);
        }
    }

    /**
     * Allocate a Namespace Object and set the fields in the DSS
     * 
     * @return A Namespace object or null if there is no room
     */
    public KubernetesNamespaceImpl allocateNamespace(String namespaceTag) {
        try {
            IResourcePoolingService pooling = this.framework.getResourcePoolingService();
            String runName = this.framework.getTestRunName();

            ArrayList<String> rejectedNamespaces = new ArrayList<>();

            String dssKeyPrefix = "cluster." + this.clusterId + ".namespace.";

            List<String> definedNamespaces = KubernetesNamespaces.get(this);

            String selectedNamespace = null;
            while(selectedNamespace == null) {
                // TODO ask pooling to return what it can.   can then set to 10, 1
                List<String> possibleNamespaces = pooling.obtainResources(definedNamespaces, rejectedNamespaces, 1, 1, dss, dssKeyPrefix);

                if (possibleNamespaces.isEmpty()) { // there are no available namespaces
                    return null;
                }

                for(String possibleNamespace : possibleNamespaces) {
                    String namespacePrefix = dssKeyPrefix + possibleNamespace;
                    //*** First reserve the name
                    HashMap<String, String> otherValues = new HashMap<>();
                    otherValues.put(namespacePrefix + ".run", runName);
                    otherValues.put(namespacePrefix + ".allocated", Instant.now().toString());
                    if (!dss.putSwap(namespacePrefix, null, "allocating", otherValues)) {
                        rejectedNamespaces.add(possibleNamespace);
                        continue; //*** Unable to reserve this name,  add to rejected and try next
                    }

                    //*** Now we have a namespace,  increase the slot count
                    while(true) { //*** have to loop around incase another test changed the current slot count
                        int maxSlots = KubernetesMaxSlots.get(this);
                        int currentSlots = 0;
                        String sCurrentSlots = dss.get("cluster." + this.clusterId + ".current.slots");
                        if (sCurrentSlots != null) {
                            currentSlots = Integer.parseInt(sCurrentSlots);
                        }

                        if (currentSlots >= maxSlots) {
                            dss.deletePrefix(namespacePrefix); // Clear the reserved namespace
                            return null; // no availability
                        }

                        currentSlots++;
                        HashMap<String, String> slotOtherValues = new HashMap<>();
                        slotOtherValues.put(namespacePrefix, "active");
                        slotOtherValues.put("slot.run." + runName + ".cluster." + this.clusterId + ".namespace." + possibleNamespace, "active");
                        slotOtherValues.put("slot.run." + runName + ".cluster." + this.clusterId + ".namespace." + possibleNamespace + ".tag", namespaceTag);
                        if (dss.putSwap("cluster." + this.clusterId + ".current.slots", sCurrentSlots, Integer.toString(currentSlots), slotOtherValues)) {
                            selectedNamespace = possibleNamespace;
                            break;
                        }
                    }
                    
                    if (selectedNamespace != null) {
                        break;
                    }
                }
            }
            
            KubernetesNamespaceImpl newNamespace = new KubernetesNamespaceImpl(this, selectedNamespace, namespaceTag, this.framework, this.dss);
            newNamespace.initialiseNamespace();
            return newNamespace;
        } catch(InsufficientResourcesAvailableException e) {
            return null;
        } catch(Exception e) {
            logger.warn("Problem allocating namespace",e);
            return null;
        }
    }
    
    /**
     * Create an APIClient for the Cluster.  Can't use the default way of doing this as we 
     * could be talking to two or clusters at the same time.
     * 
     * @return An APIClient.  never null
     * @throws KubernetesManagerException - If there is a problem with authentication or communication
     */
    @NotNull
    public synchronized ApiClient getApi() throws KubernetesManagerException {
        if (this.apiClient != null) {
            return this.apiClient;
        }
        
        URL url = KubernetesUrl.get(this);
        boolean validateCertificate = KubernetesValidateCertificate.get(this);
        String credentialsId = KubernetesCredentials.get(this);
        
        ICredentials credentials = null;
        try {
            ICredentialsService creds = this.framework.getCredentialsService();
            credentials = creds.getCredentials(credentialsId);
        } catch (CredentialsException e) {
            throw new KubernetesManagerException("Problem accessing credentials " + credentialsId, e);
        }
        
        if (credentials == null) {
            throw new KubernetesManagerException("Credentials " + credentialsId + " are missing");
        }
        
        if (!(credentials instanceof ICredentialsToken)) {
            throw new KubernetesManagerException("Credentials " + credentialsId + " is not a token credentials");
        }
        
        
        try {
            this.apiClient = Config.fromToken(url.toString(), new String(((ICredentialsToken)credentials).getToken()), validateCertificate);
            //TODO do, raise issue because Quantity is not being serialized properly
            applyNewGson(this.apiClient);
            this.apiClient.setDebugging(false);
            
            return this.apiClient;
        } catch(Exception e) {
            throw new KubernetesManagerException("Unable the initialise the Kubernetes API Client", e);
        }
        
    }

    /**
     * For some reason, v7 of the client does not serialize Quantity or IntOrString.  Should raise an issue
     * but in the meantime...
     * 
     * @param apiClient The APClient to rework
     */
    private static void applyNewGson(ApiClient apiClient) {
        
        JSON json = apiClient.getJSON();
        Gson existingGson = json.getGson();
        
        /* This section has not been incorporated into the GalasaGsonWrapper as it involves kubernetes packages 
         * and would result in a circullar refernce.
         */
        GalasaGsonBuilder newGsonBuilder = new GalasaGsonBuilder()
            .registerTypeAdapter(OffsetDateTime.class, existingGson.getAdapter(OffsetDateTime.class))
            .registerTypeAdapter(Date.class, existingGson.getAdapter(Date.class))
            .registerTypeAdapter(java.sql.Date.class, existingGson.getAdapter(java.sql.Date.class))
            .registerTypeAdapter(byte[].class, existingGson.getAdapter(byte[].class))
            .registerTypeAdapter(Quantity.class, new Quantity.QuantityAdapter())
            .registerTypeAdapter(IntOrString.class, new IntOrString.IntOrStringAdapter());
        
        json.setGson(newGsonBuilder.getGson());   
    }

    /**
     * Retrieve the hostname that should be used to access nodeports.
     * 
     * @return The hostname, will default to the API hostname
     * @throws KubernetesManagerException If there is a problem with the CPS
     */
    @NotNull
    public String getNodePortProxyHostname() throws KubernetesManagerException {
        return KubernetesNodePortProxy.get(this);
    }
    
    
}
