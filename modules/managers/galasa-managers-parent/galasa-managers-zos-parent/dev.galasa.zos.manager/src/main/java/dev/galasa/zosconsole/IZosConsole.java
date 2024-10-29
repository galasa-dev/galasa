/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosconsole;

import javax.validation.constraints.NotNull;

/**
 * Provides the test code access to the zOS Console via the zOS Manager 
 *
 */
public interface IZosConsole {
    
    /**
     * Issue a command to the zOS Console using the default named console
     * 
     * @param command The console command
     * @return {@link IZosConsoleCommand} A representation of the console response
     * @throws ZosConsoleException 
     */
    @NotNull
    public IZosConsoleCommand issueCommand(@NotNull String command) throws ZosConsoleException;
    
    /**
     * Issue a command to the zOS Console using a named console
     * 
     * @param command The console command
     * @param consoleName The name of the EMCS console that is used to issue the command 
     * @return {@link IZosConsoleCommand} A representation of the console response
     * @throws ZosConsoleException 
     */
    @NotNull
    public IZosConsoleCommand issueCommand(@NotNull String command, String consoleName) throws ZosConsoleException;

}
