/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.internal.rps.ResourceString;

/**
 * This class provides the Resource pooling service to the framework. It can
 * generate resources to a number of specifications. When params are not given,
 * defaults are used.
 * 
 *  
 */
public class FrameworkResourcePoolingService implements IResourcePoolingService {
    private static final int           DEFAULTNUMBEROFRESOURCES    = 10;
    private static final int           DEFAULTCONSECUTIVERESOURCES = 1;
    private IDynamicStatusStoreService defaultDss                  = new StubbedDss();
    private String                     defaultKeyPrefix            = "";
    private Random                     random                      = new Random();

    /**
     * This method obtaines resources from given definitions, but allowing rejected
     * resources to be disgarded from any returned list.
     * 
     * The default return list length is 10.
     * 
     * @param resourceStrings   - this is a string list of all the definitions for
     *                          resources to select from.
     * @param rejectedResources - this is a list of resource names NOT to be
     *                          included in the returned list.
     * @return - a list of available reosurces from the defined acceptable
     *         resources.
     */
    public List<String> obtainResources(@NotNull List<String> resourceStrings, List<String> rejectedResources)
            throws InsufficientResourcesAvailableException {
        List<ResourceString> resourceDefinitions;
        try {
            resourceDefinitions = createResourceDefintions(resourceStrings);
            return generateResources(resourceDefinitions, rejectedResources, DEFAULTNUMBEROFRESOURCES, defaultDss,
                    defaultKeyPrefix, DEFAULTCONSECUTIVERESOURCES);
        } catch (ResourcePoolingServiceException | DynamicStatusStoreException e) {
            throw new InsufficientResourcesAvailableException(
                    "Could not generate resource Strings from the definitions provided. ", e);
        }
    }

    /**
     * This method obtaines resources from given definitions, but allowing rejected
     * resources to be disgarded from any returned list. Additionally this method
     * checks against a Dynamic status store for the resources to ensure they are
     * not in use.
     * 
     * The default return list length is 10.
     * 
     * @param resourceStrings   - this is a string list of all the definitions for
     *                          resources to select from.
     * @param rejectedResources - this is a list of resource names NOT to be
     *                          included in the returned list.
     * @param dss               - the dynamic status store to check for the
     *                          resources.
     * @param keyPrefix         - the keyprefix for the resource for it to be found
     *                          in the DSS.
     * @return - a list of available reosurces from the defined acceptable
     *         resources.
     */
    public List<String> obtainResources(@NotNull List<String> resourceStrings, List<String> rejectedResources,
            IDynamicStatusStoreService dss, String keyPrefix) throws InsufficientResourcesAvailableException {
        List<ResourceString> resourceDefinitions;
        try {
            resourceDefinitions = createResourceDefintions(resourceStrings);
            return generateResources(resourceDefinitions, rejectedResources, DEFAULTNUMBEROFRESOURCES, dss, keyPrefix,
                    DEFAULTCONSECUTIVERESOURCES);
        } catch (ResourcePoolingServiceException | DynamicStatusStoreException e) {
            throw new InsufficientResourcesAvailableException(
                    "Could not generate resource Strings from the definitions provided. ", e);
        }
    }

    /**
     * This method obtaines resources from given definitions, but allowing rejected
     * resources to be disgarded from any returned list. Additonally this method
     * allows for the number of resources returned in the list to be defined.
     * 
     * @param resourceStrings   - this is a string list of all the definitions for
     *                          resources to select from.
     * @param rejectedResources - this is a list of resource names NOT to be
     *                          included in the returned list.
     * @param returnMinimum     - the number of resources to return.
     * @return - a list of available reosurces from the defined acceptable
     *         resources.
     */
    public List<String> obtainResources(@NotNull List<String> resourceStrings, List<String> rejectedResources,
            int returnMinimum) throws InsufficientResourcesAvailableException {
        List<ResourceString> resourceDefinitions;
        try {
            resourceDefinitions = createResourceDefintions(resourceStrings);
            return generateResources(resourceDefinitions, rejectedResources, returnMinimum, defaultDss,
                    defaultKeyPrefix, DEFAULTCONSECUTIVERESOURCES);
        } catch (ResourcePoolingServiceException | DynamicStatusStoreException e) {
            throw new InsufficientResourcesAvailableException(
                    "Could not generate resource Strings from the definitions provided. ", e);
        }
    }

    /**
     * This method obtaines resources from given definitions, but allowing rejected
     * resources to be disgarded from any returned list. Additionally this method
     * checks against a Dynamic status store for the resources to ensure they are
     * not in use. As well, this method allows the number of resources returned to
     * be defined.
     * 
     * 
     * @param resourceStrings   - this is a string list of all the definitions for
     *                          resources to select from.
     * @param rejectedResources - this is a list of resource names NOT to be
     *                          included in the returned list.
     * @param dss               - the dynamic status store to check for the
     *                          resources.
     * @param keyPrefix         - the keyprefix for the resource for it to be found
     *                          in the DSS.
     * @param returnMinimum     - the number of resources to return.
     * @return - a list of available reosurces from the defined acceptable
     *         resources.
     */
    public List<String> obtainResources(@NotNull List<String> resourceStrings, List<String> rejectedResources,
            int returnMinimum, IDynamicStatusStoreService dss, String keyPrefix)
            throws InsufficientResourcesAvailableException {
        List<ResourceString> resourceDefinitions;
        try {
            resourceDefinitions = createResourceDefintions(resourceStrings);
            return generateResources(resourceDefinitions, rejectedResources, returnMinimum, dss, keyPrefix,
                    DEFAULTCONSECUTIVERESOURCES);
        } catch (ResourcePoolingServiceException | DynamicStatusStoreException e) {
            throw new InsufficientResourcesAvailableException(
                    "Could not generate resource Strings from the definitions provided. ", e);
        }
    }

    /**
     * This method obtaines resources from given definitions, but allowing rejected
     * resources to be disgarded from any returned list. Additonally this method
     * allows for the number of resources returned in the list to be defined, in
     * sections of consecutive resources. For example if i asked for 30 resources
     * with a returnConsecutive of 10, then 3 lots of 10 resources would be in the
     * list. Each 10 would be in a continuous chain.
     * 
     * @param resourceStrings   - this is a string list of all the definitions for
     *                          resources to select from.
     * @param rejectedResources - this is a list of resource names NOT to be
     *                          included in the returned list.
     * @param returnMinimum     - the number of resources to return.
     * @param returnConsecutive - the size of the "chunks" to find consectutive
     *                          reosources in.
     * @return - a list of available reosurces from the defined acceptable
     *         resources.
     */
    public List<String> obtainResources(@NotNull List<String> resourceStrings, List<String> rejectedResources,
            int returnMinimum, int returnConsecutive) throws InsufficientResourcesAvailableException {
        List<ResourceString> resourceDefinitions;
        if ((returnMinimum % returnConsecutive) != 0) {
            throw new InsufficientResourcesAvailableException(
                    "The number of consecutive resources required needs to be a multiple of the total number of resources required.");
        }
        try {
            resourceDefinitions = createResourceDefintions(resourceStrings);
            return generateResources(resourceDefinitions, rejectedResources, returnMinimum, defaultDss,
                    defaultKeyPrefix, returnConsecutive);
        } catch (ResourcePoolingServiceException | DynamicStatusStoreException e) {
            throw new InsufficientResourcesAvailableException(
                    "Could not generate resource Strings from the definitions provided. ", e);
        }
    }

    /**
     * This method obtaines resources from given definitions, but allowing rejected
     * resources to be disgarded from any returned list. Additonally this method
     * allows for the number of resources returned in the list to be defined, in
     * sections of consecutive resources. For example if i asked for 30 resources
     * with a returnConsecutive of 10, then 3 lots of 10 resources would be in the
     * list. Each 10 would be in a continuous chain.
     * 
     * This method also checks the DSS for the resources to ensure they are not in
     * use.
     * 
     * @param resourceStrings   - this is a string list of all the definitions for
     *                          resources to select from.
     * @param rejectedResources - this is a list of resource names NOT to be
     *                          included in the returned list.
     * @param returnMinimum     - the number of resources to return.
     * @param dss               - the dynamic status store to check for the
     *                          resources.
     * @param keyPrefix         - the keyprefix for the resource for it to be found
     *                          in the DSS.
     * @param returnConsecutive - the size of the "chunks" to find consectutive
     *                          reosources in.
     * @return - a list of available reosurces from the defined acceptable
     *         resources.
     */
    public List<String> obtainResources(@NotNull List<String> resourceStrings, List<String> rejectedResources,
            int returnMinimum, int returnConsecutive, IDynamicStatusStoreService dss, String keyPrefix)
            throws InsufficientResourcesAvailableException {
        List<ResourceString> resourceDefinitions;
        if ((returnMinimum % returnConsecutive) != 0) {
            throw new InsufficientResourcesAvailableException(
                    "The number of consecutive resources required needs to be a multiple of the total number of resources required.");
        }
        try {
            resourceDefinitions = createResourceDefintions(resourceStrings);
            return generateResources(resourceDefinitions, rejectedResources, returnMinimum, dss, keyPrefix,
                    returnConsecutive);
        } catch (ResourcePoolingServiceException | DynamicStatusStoreException e) {
            throw new InsufficientResourcesAvailableException(
                    "Could not generate resource Strings from the definitions provided. ", e);
        }
    }

    /**
     * This method creates all the resource string defintions from the basic strings
     * passed which explain them.
     * 
     * @param resourceStrings - list of string like: APPLID{9}{4-7}{F}{z}, etc
     * @return - returns a list of the resource strings which can be used to gather
     *         reosources.
     *
     * @throws ResourcePoolingServiceException
     */
    private List<ResourceString> createResourceDefintions(List<String> resourceStrings)
            throws ResourcePoolingServiceException {
        List<ResourceString> list = new ArrayList<>();
        for (String input : resourceStrings) {
            list.add(new ResourceString(input));
        }
        return list;
    }

    /**
     * This method generates the resources from the given restrictions. On the first
     * pass random generation is attempted to make sure load is equally spread
     * across resources. If random generation fails, sequential generation of
     * resources is then attempted. The InsufficentResourceException is thrown if
     * this too cannot generate the defined resources.
     * 
     * @param resourceDefinitions - the resource strings passed that define the
     *                            availabble resources to generate.
     * @param rejectedResources   - the resource strings which are to be rejected if
     *                            generated.
     * @param numberOfResources   - the number of resources required to be
     *                            generated.
     * @param dss                 - the dynamic status store to check against.
     * @param keyPrefix           - the prefix for the resource if it was to be
     *                            found in the DSS.
     * @param returnConsecutive   - the "chunk" size to generate consecutive
     *                            resources too.
     * @return - a list of generated resource within the constraits passed on this
     *         method.
     * @throws DynamicStatusStoreException
     * @throws InsufficientResourcesAvailableException
     */
    private List<String> generateResources(List<ResourceString> resourceDefinitions, List<String> rejectedResources,
            int numberOfResources, IDynamicStatusStoreService dss, String keyPrefix, int returnConsecutive)
            throws DynamicStatusStoreException, InsufficientResourcesAvailableException {
        List<String> generatedResources = new ArrayList<>();
        List<String> bannedResources = new ArrayList<>();

        if (rejectedResources == null) {
            rejectedResources = new ArrayList<>();
        }

        bannedResources.addAll(rejectedResources);

        try {
            for (int i = 0; i < numberOfResources; i += returnConsecutive) {
                ResourceString randomDefinition = resourceDefinitions.get(random.nextInt(resourceDefinitions.size()));

                List<String> newResources = generateRandomResources(randomDefinition, bannedResources, dss, keyPrefix,
                        returnConsecutive);
                generatedResources.addAll(newResources);
                bannedResources.addAll(newResources);
            }
            return generatedResources;
        } catch (InsufficientResourcesAvailableException | DynamicStatusStoreException e) {
            bannedResources.clear();
            bannedResources.addAll(rejectedResources);
            generatedResources.clear();
        }

        try {
            for (ResourceString definition : resourceDefinitions) {
                for (int i = 0; i < numberOfResources; i += returnConsecutive) {
                    if (generatedResources.size() == numberOfResources) {
                        break;
                    } else {
                        List<String> newResources = generateSequentialResources(definition, bannedResources, dss,
                                keyPrefix, returnConsecutive);
                        generatedResources.addAll(newResources);
                        bannedResources.addAll(newResources);
                    }
                }
                if (generatedResources.size() == numberOfResources) {
                    break;
                }
            }
            if (generatedResources.size() != numberOfResources) {
                throw new InsufficientResourcesAvailableException("There is not enough available resource.");
            }
            return generatedResources;
        } catch (InsufficientResourcesAvailableException | DynamicStatusStoreException e) {
            throw new InsufficientResourcesAvailableException("There is not enough resource available", e);
        }
    }

    /**
     * This method is used for the random generation of the resources.
     * 
     * @param definition            -the resource string passed that define the
     *                              available resources to generate.
     * @param bannedReosurceStrings - the resources that are not allowed to be added
     *                              to the return list
     * @param dss                   - the dynamic status store to check against.
     * @param keyPrefix             - the prefix for the resource if it was to be
     *                              found in the DSS.
     * @param returnConsecutive     - the "chunk" size to generate consecutive
     *                              resources too.
     * @return - return list of generated resources that are randomly distributed.
     * @throws DynamicStatusStoreException
     * @throws InsufficientResourcesAvailableException
     */
    private List<String> generateRandomResources(ResourceString definition, List<String> bannedReosurceStrings,
            IDynamicStatusStoreService dss, String keyPrefix, int returnConsecutive)
            throws DynamicStatusStoreException, InsufficientResourcesAvailableException {
        String randomResource = definition.getRandomResource();
        List<String> resources = new ArrayList<>();
        int attempts = 0;

        while (resources.size() < returnConsecutive) {
            if (!(bannedReosurceStrings.contains(randomResource)) && ((dss.get(keyPrefix + randomResource)) == null)) {
                resources.add(randomResource);
            } else {
                resources.clear();
                attempts++;
                if (attempts / definition.getNumberOfCombinations() > 0.4) {
                    throw new InsufficientResourcesAvailableException(
                            "Random generation is hitting too many banned resoruces");
                }
            }
            if (resources.size() < returnConsecutive) {
                try {
                    randomResource = definition.getNextResource();
                } catch (InsufficientResourcesAvailableException e) {
                    resources.clear();
                    randomResource = definition.getNextResource();
                }
            }

        }
        return resources;
    }

    /**
     * This method is used for sequential generation of resources if the random
     * generation fails. This should only trigger is the attempted number of random
     * generation number is 40% of the available reosurces.
     * 
     * @param definition            -the resource string passed that define the
     *                              available resources to generate.
     * @param bannedReosurceStrings - the resources that are not allowed to be added
     *                              to the return list
     * @param dss                   - the dynamic status store to check against.
     * @param keyPrefix             - the prefix for the resource if it was to be
     *                              found in the DSS.
     * @param returnConsecutive     - the "chunk" size to generate consecutive
     *                              resources too.
     * @return - return list of generated resources that are sequentially
     *         distributed.
     * @throws DynamicStatusStoreException
     */
    private List<String> generateSequentialResources(ResourceString definition, List<String> bannedReosurceStrings,
            IDynamicStatusStoreService dss, String keyPrefix, int returnConsecutive)
            throws DynamicStatusStoreException, InsufficientResourcesAvailableException {
        String resource = definition.getFirstResource();
        List<String> resources = new ArrayList<>();
        int attempt = 0;

        while (resources.size() < returnConsecutive) {
            if (!(bannedReosurceStrings.contains(resource)) && ((dss.get(keyPrefix + resource)) == null)) {
                resources.add(resource);
            } else {
                resources.clear();
                attempt++;
                if (attempt > definition.getNumberOfCombinations()) {
                    return new ArrayList<>();
                }
            }
            if (resources.size() < returnConsecutive) {
                try {
                    resource = definition.getNextResource();
                } catch (InsufficientResourcesAvailableException e) {
                    resources.clear();
                    resource = definition.getNextResource();
                    attempt++;
                }
            }
        }
        return resources;
    }

    /**
     * This class is used when a dss is not provided. When checking resources this
     * stubbed class returns null for the DSS checks.
     */
    private class StubbedDss implements IDynamicStatusStoreService {

        /**
         * Commenting as unused, but required from IDynamicStatusStore implementation.
         */
        public IDynamicResource getDynamicResource(String input) {
            return null;
        }

        /**
         * Commenting as unused, but required from IDynamicStatusStore implementation.
         */
        public IDynamicRun getDynamicRun() throws DynamicStatusStoreException {
            return null;
        }

        /**
         * Commenting as unused, but required from IDynamicStatusStore implementation.
         */
        public void deletePrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException {
            // EMPTY METHOD
        }

        /**
         * Commenting as unused, but required from IDynamicStatusStore implementation.
         */
        public void delete(@NotNull Set<String> keys) throws DynamicStatusStoreException {
            // EMPTY METHOD
        }

        /**
         * Commenting as unused, but required from IDynamicStatusStore implementation.
         */
        public void delete(@NotNull String key) throws DynamicStatusStoreException {
            // EMPTY METHOD
        }

        /**
         * Commenting as unused, but required from IDynamicStatusStore implementation.
         */
        public @NotNull Map<String, String> getPrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException {
            return new HashMap<>();
        }

        /**
         * Commenting as unused, but required from IDynamicStatusStore implementation.
         */
        public String get(@NotNull String key) throws DynamicStatusStoreException {
            return null;
        }

        /**
         * Commenting as unused, but required from IDynamicStatusStore implementation.
         */
        public boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue,
                @NotNull Map<String, String> others) throws DynamicStatusStoreException {
            return true;
        }

        /**
         * Commenting as unused, but required from IDynamicStatusStore implementation.
         */
        public boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue)
                throws DynamicStatusStoreException {
            return true;
        }

        /**
         * Commenting as unused, but required from IDynamicStatusStore implementation.
         */
        public void put(@NotNull Map<String, String> keyValues) throws DynamicStatusStoreException {
            // EMPTY METHOD
        }

        /**
         * Commenting as unused, but required from IDynamicStatusStore implementation.
         */
        public void put(@NotNull String key, @NotNull String value) throws DynamicStatusStoreException {
            // EMPTY METHOD
        }

        /**
         * Commenting as unused, but required from IDynamicStatusStore implementation.
         */
        @Override
        public UUID watch(IDynamicStatusStoreWatcher watcher, String key) throws DynamicStatusStoreException {
            return null;
        }

        @Override
        public UUID watchPrefix(IDynamicStatusStoreWatcher watcher, String keyPrefix)
                throws DynamicStatusStoreException {
            return null;
        }

        @Override
        public void unwatch(UUID watchId) throws DynamicStatusStoreException {

        }

        @Override
        public void performActions(IDssAction... actions) throws DynamicStatusStoreException {
            
        }

    }
}