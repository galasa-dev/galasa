/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

public interface IBeanValidator<T> {

    /**
     * Checks that the provided bean is valid according to its validation rules. The provided bean should come from the
     * body of a request that has been serialised into an object according to the OpenAPI specification.
     * 
     * @param bean an instance of the API bean to validate
     * @throws InternalServletException if the bean is not valid
     */
    void validate(T bean) throws InternalServletException;
}
