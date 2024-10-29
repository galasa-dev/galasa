/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zostsocommand;

import javax.validation.constraints.NotNull;

/**
 * Provides the test code access to zOS TSO Commands via the zOS Manager 
 *
 */
public interface IZosTSOCommand {
    
    /**
     * Issue a zOS TSO command
     * 
     * @param command The TSO command
     * @return the TSO command response
     * @throws ZosTSOCommandException 
     */
    public String issueCommand(@NotNull String command) throws ZosTSOCommandException;
    
    /**
     * Issue a zOS TSO command
     * 
     * @param command The TSO command
     * @param timeout time (in milliseconds) to wait with no new output appearing before timing out
     * @return the TSO command response
     * @throws ZosTSOCommandException 
     */
    public String issueCommand(@NotNull String command, long timeout) throws ZosTSOCommandException;
}
