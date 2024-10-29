/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.artifact;

import dev.galasa.artifact.ISkeletonProcessor.SkeletonType;

/**
 * This is a very simple manager which provides access to objects to assist in
 * retrieving artifacts/resources from the 'resources' directory in your test
 * bundle, and manipulating those resources
 * 
 *  
 *
 */
public interface IArtifactManager {

    /**
     * Return an {@link IBundleResources} object to assist in retrieving artifacts
     * from within a bundles 'resources' directory.
     * 
     * @param owningClass - any class within the bundle to be accessed, simply used
     *                    as a reference to get hold of the correct bundle
     * @return {@link IBundleResources}
     */
    IBundleResources getBundleResources(Class<?> owningClass);

    /**
     * Return an {@link ISkeletonProcessor} object to perform substitutions on
     * skeleton files
     * 
     * @return {@link ISkeletonProcessor}
     */
    ISkeletonProcessor getSkeletonProcessor();

    /**
     * Return an {@link ISkeletonProcessor} object to perform substitutions on
     * skeleton files Select Type using {@link SkeletonType}
     * 
     * @param skeletonType
     * @return {@link ISkeletonProcessor}
     * @throws SkeletonProcessorException
     */
    ISkeletonProcessor getSkeletonProcessor(int skeletonType) throws SkeletonProcessorException;

}
