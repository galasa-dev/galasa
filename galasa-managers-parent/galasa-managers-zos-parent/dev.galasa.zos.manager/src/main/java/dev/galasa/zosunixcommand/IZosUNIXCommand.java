/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosunixcommand;

import javax.validation.constraints.NotNull;

import dev.galasa.ICredentials;

/**
 * Provides the test code access to zOS UNIX Commands via the zOS Manager 
 *
 */
public interface IZosUNIXCommand {
    
    /**
     * Issue a zOS UNIX command 
     * 
     * @param command The command
     * @return the UNIX command response
     * @throws ZosUNIXCommandException 
     */
    public String issueCommand(@NotNull String command) throws ZosUNIXCommandException;
    
    /**
     * Issue a zOS UNIX command with a timeout 
     * 
     * @param command The command
     * @param timeout time (in milliseconds) to wait with no new output appearing before timing out
     * @return the UNIX command response
     * @throws ZosUNIXCommandException 
     */
    public String issueCommand(@NotNull String command, long timeout) throws ZosUNIXCommandException;
    
    /**
     * Issue a zOS UNIX command with specified credentials
     * 
     * @param command The command
     * @param credentials the credentials
     * @return the UNIX command response
     * @throws ZosUNIXCommandException 
     */
    public String issueCommand(@NotNull String command, ICredentials credentials) throws ZosUNIXCommandException;
    
    /**
     * Issue a zOS UNIX command with specified credentials and timeout 
     * 
     * @param command The command
     * @param timeout time (in milliseconds) to wait with no new output appearing before timing out
     * @param credentials the credentials
     * @return the UNIX command response
     * @throws ZosUNIXCommandException 
     */
    public String issueCommand(@NotNull String command, long timeout, ICredentials credentials) throws ZosUNIXCommandException;

}
