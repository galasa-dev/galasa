/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;


import java.util.*;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;

public class CPSFacade {

    private Map<String,CPSNamespace> bakedInNamespaceMap = new HashMap<String,CPSNamespace>();
    private IFramework framework;
    private IConfigurationPropertyStoreService cpsService;

    public CPSFacade(IFramework framework) throws ConfigurationPropertyStoreException  {
        this.framework = framework;
        this.cpsService = framework.getConfigurationPropertyService("framework");
        populateBakedInNamespace();
    }

    private void populateBakedInNamespace() throws ConfigurationPropertyStoreException  {
        addNamespace("framework", Visibility.NORMAL);
        addNamespace("secure", Visibility.SECURE);
        addNamespace("dss", Visibility.HIDDEN);
        addNamespace("dex", Visibility.HIDDEN);
    }

    private void addNamespace(String name, Visibility visibility) throws ConfigurationPropertyStoreException  {
        bakedInNamespaceMap.put(name, new CPSNamespace(name, visibility, this.framework));
    }

    public Map<String,CPSNamespace> getNamespaces() throws ConfigurationPropertyStoreException  {

        Map<String, CPSNamespace> namespaces = new HashMap<>();
        namespaces.putAll(bakedInNamespaceMap);

        List<String> namespaceNames = cpsService.getCPSNamespaces();
        for (String name : namespaceNames) {
            if (!bakedInNamespaceMap.containsKey(name) ) {
                namespaces.put(name, new CPSNamespace(name, Visibility.NORMAL, this.framework));
            }
        }

        return Collections.unmodifiableMap(namespaces);
    }

    public CPSNamespace getNamespace(String name) throws ConfigurationPropertyStoreException  {
        CPSNamespace namespace = bakedInNamespaceMap.get(name);
        if (namespace == null) {
            namespace = new CPSNamespace(name, Visibility.NORMAL ,this.framework);       
        }
        return namespace;
    }
}
