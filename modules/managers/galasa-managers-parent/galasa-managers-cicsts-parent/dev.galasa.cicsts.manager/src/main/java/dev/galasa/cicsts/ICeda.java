/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts;

import javax.validation.constraints.NotNull;

public interface ICeda {
   
    public void createResource(@NotNull ICicsTerminal terminal, @NotNull String resourceType, 
            @NotNull String resourceName, @NotNull String groupName, String resourceParameters) throws CedaException;
      
      public void installGroup(@NotNull ICicsTerminal terminal, @NotNull String groupName) throws CedaException;
      
      public void installResource(@NotNull ICicsTerminal terminal, @NotNull String resourceType, 
            @NotNull String resourceName, @NotNull String cedaGroup) throws CedaException;
      
      public void deleteGroup(@NotNull ICicsTerminal terminal, @NotNull String groupName) throws CedaException;
      
      public void deleteResource(@NotNull ICicsTerminal terminal, @NotNull String resourceType, 
            @NotNull String resourceName, @NotNull String groupName) throws CedaException;

      public boolean resourceExists(@NotNull ICicsTerminal terminal, @NotNull String resourceType, @NotNull String resourceName, @NotNull String groupName) throws CedaException;

}
