/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.artifact;

import java.io.InputStream;
import java.util.Map;

public interface ISkeletonProcessor {

    public class SkeletonType {

        public static final int PLUSPLUS = 0;
        public static final int VELOCITY = 1;
    }

    InputStream processSkeleton(InputStream skeleton, Map<String, Object> parameters) throws SkeletonProcessorException;

}
