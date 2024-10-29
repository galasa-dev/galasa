/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import java.util.List;

import javax.validation.constraints.NotNull;

public interface IResourcePoolingService {

    /**
     * <p>
     * Generate and return a list of resource that are available for use by the
     * manager
     * </p>
     * 
     * <p>
     * Convenience method for obtainResources(resourceStrings, rejectedResources,
     * 10, 1, null, null)
     * </p>
     * 
     * @param resourceStrings   - A list of resource regex string used to generate
     *                          the available resource pool to select from
     * @param rejectedResources - A list of resources to be excluded from selection
     * @return - A list of available resources, at that time, other test runs may
     *         have acquired the same resources
     * @throws InsufficientResourcesAvailableException - If unable to generate the
     *                                                 return minimum
     */
    @NotNull
    List<String> obtainResources(@NotNull List<String> resourceStrings, List<String> rejectedResources)
            throws InsufficientResourcesAvailableException;

    /**
     * <p>
     * Generate and return a list of resource that are available for use by the
     * manager
     * </p>
     * 
     * <p>
     * Convenience method for obtainResources(resourceStrings, rejectedResources,
     * 10, 1, dss, keyprefix)
     * </p>
     * 
     * @param resourceStrings   - A list of resource regex string used to generate
     *                          the available resource pool to select from
     * @param rejectedResources - A list of resources to be excluded from selection
     * @param dss
     * @param keyPrefix
     * @return - A list of available resources, at that time, other test runs may
     *         have acquired the same resources
     * @throws InsufficientResourcesAvailableException - If unable to generate the
     *                                                 return minimum
     */
    @NotNull
    List<String> obtainResources(@NotNull List<String> resourceStrings, List<String> rejectedResources,
            IDynamicStatusStoreService dss, String keyPrefix) throws InsufficientResourcesAvailableException;

    /**
     * <p>
     * Generate and return a list of resource that are available for use by the
     * manager
     * </p>
     * 
     * <p>
     * Convenience method for obtainResources(resourceStrings, rejectedResources,
     * returnMinimum, 1, null, null)
     * </p>
     * 
     * @param resourceStrings   - A list of resource regex string used to generate
     *                          the available resource pool to select from
     * @param rejectedResources - A list of resources to be excluded from selection
     * @param returnMinimum     - The minimum number of resource names to generate
     * @return - A list of available resources, at that time, other test runs may
     *         have acquired the same resources
     * @throws InsufficientResourcesAvailableException - If unable to generate the
     *                                                 return minimum
     */
    @NotNull
    List<String> obtainResources(@NotNull List<String> resourceStrings, List<String> rejectedResources,
            int returnMinimum) throws InsufficientResourcesAvailableException;

    /**
     * <p>
     * Generate and return a list of resource that are available for use by the
     * manager
     * </p>
     * 
     * <p>
     * Convenience method for obtainResources(resourceStrings, rejectedResources,
     * returnMinimum, 1, dss, keyPrefix)
     * </p>
     * 
     * @param resourceStrings   - A list of resource regex string used to generate
     *                          the available resource pool to select from
     * @param rejectedResources - A list of resources to be excluded from selection
     * @param returnMinimum     - The minimum number of resource names to generate
     * @param dss               - The Dynamic Status Store to check if the resource
     *                          exists, can be null
     * @param keyPrefix         - The prefix key for the resource in the DSS, can be
     *                          null if dss is null
     * @return - A list of available resources, at that time, other test runs may
     *         have acquired the same resources
     * @throws InsufficientResourcesAvailableException - If unable to generate the
     *                                                 return minimum
     */
    @NotNull
    List<String> obtainResources(@NotNull List<String> resourceStrings, List<String> rejectedResources,
            int returnMinimum, IDynamicStatusStoreService dss, String keyPrefix)
            throws InsufficientResourcesAvailableException;

    /**
     * <p>
     * Generate and return a list of resource that are available for use by the
     * manager
     * </p>
     * 
     * <p>
     * Convenience method for obtainResources(resourceStrings, rejectedResources,
     * returnMinimum, returnConsecutive, null, null)
     * </p>
     * 
     * @param resourceStrings   - A list of resource regex string used to generate
     *                          the available resource pool to select from
     * @param rejectedResources - A list of resources to be excluded from selection
     * @param returnMinimum     - The minimum number of resource names to generate
     * @param returnConsecutive - The number of consecutive resource names to return
     *                          (must be a modular of return minimum)
     * @return - A list of available resources, at that time, other test runs may
     *         have acquired the same resources
     * @throws InsufficientResourcesAvailableException - If unable to generate the
     *                                                 return minimum
     */
    @NotNull
    List<String> obtainResources(@NotNull List<String> resourceStrings, List<String> rejectedResources,
            int returnMinimum, int returnConsecutive) throws InsufficientResourcesAvailableException;

    /**
     * <p>
     * Generate and return a list of resources that available for use by the
     * manager. If the dss and a keyPrefix is provided, then this routine will check
     * the if the key exists in the dss, if it does, the resource is deemed in use
     * </p>
     * 
     * @param resourceStrings   - A list of resource regex string used to generate
     *                          the available resource pool to select from
     * @param rejectedResources - A list of resources to be excluded from selection
     * @param returnMinimum     - The minimum number of resource names to generate
     * @param returnConsecutive - The number of consecutive resource names to return
     *                          (must be a modular of return minimum)
     * @param dss               - The Dynamic Status Store to check if the resource
     *                          exists, can be null
     * @param keyPrefix         - The prefix key for the resource in the DSS, can be
     *                          null if dss is null
     * @return - A list of available resources, at that time, other test runs may
     *         have acquired the same resources
     * @throws InsufficientResourcesAvailableException - If unable to generate the
     *                                                 return minimum
     */
    @NotNull
    List<String> obtainResources(@NotNull List<String> resourceStrings, List<String> rejectedResources,
            int returnMinimum, int returnConsecutive, IDynamicStatusStoreService dss, String keyPrefix)
            throws InsufficientResourcesAvailableException;
}
