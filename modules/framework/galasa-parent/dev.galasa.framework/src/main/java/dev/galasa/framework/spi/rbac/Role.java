/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.rbac;

import java.util.List;

public interface Role {
    
    String getName();

    String getId();

    List<String> getActionIds();

    String getDescription();

}
