/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.lang.annotation.Annotation;

/**
 * Something which can extract annotations from something can be an AnnotationExtractor
 */
public interface IAnnotationExtractor {    
    /**
    * @param <A> The type of testclass we are extracting something from. 
    * This will vary wildly based on what the users' test class is.
    * @param <B> The annotation type we want to extract.
    * Having this as a generic type allows this logic to be used for
    * any of the annotations we want to extract.
    * B must extend Annotation.
    * @param testClass The test class we want annotations extracted from.
    * @param annotationClass The annotation class we want to use.
    * @return An instance of the annotationClass, extracted from the test class.
    */
    public <A,B extends Annotation> B getAnnotation (Class<A> testClass, Class<B> annotationClass);
}
