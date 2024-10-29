/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import javax.validation.constraints.NotNull;

/**
 * Represents annotated fields that the AbstractManager finds on behalf of
 * Managers
 * 
 *  
 *
 */
public class AnnotatedField {

    /**
     * The annotated field
     */
    private final Field            field;
    /**
     * All the annotations related to the field
     */
    private final List<Annotation> annotations;

    /**
     * @param field       The annotated field
     * @param annotations All annotations on the field
     */
    public AnnotatedField(@NotNull Field field, @NotNull List<Annotation> annotations) {
        this.field = field;
        this.annotations = annotations;
    }

    /**
     * @return The annotated field
     */
    public Field getField() {
        return field;
    }

    /**
     * @return All annotations on the field
     */
    public List<Annotation> getAnnotations() {
        return annotations;
    }
}
