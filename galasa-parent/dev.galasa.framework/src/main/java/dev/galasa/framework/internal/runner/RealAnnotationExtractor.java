/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.runner;

import java.lang.annotation.Annotation;

import dev.galasa.framework.IAnnotationExtractor;

public class RealAnnotationExtractor  implements IAnnotationExtractor {

    @Override
    public <A,B extends Annotation> B getAnnotation (Class<A> testClass, Class<B> annotationClass) {
        B result = testClass.getAnnotation(annotationClass);
        return result;
    }

}
