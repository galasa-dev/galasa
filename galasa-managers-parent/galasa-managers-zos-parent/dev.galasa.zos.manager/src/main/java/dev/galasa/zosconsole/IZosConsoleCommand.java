/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosconsole;

/**
 * Represents a zOS Console Command.<br><br> 
 * When the command is issued via {@link IZosConsole#issueCommand(String)} or {@link IZosConsole#issueCommand(String, String)}, the immediate response message, 
 * if available, together with a command response key, is received from the zOS Console.
 * The immediate response message is available via the {@link #getResponse()} method.<br> 
 * The {@link #requestResponse()} method uses the command response key to request any delayed response messages from the zOS Console associated with this command 
 * and returns all responses messages issued since the initial response or previous {@link #requestResponse()} method call.
 */
public interface IZosConsoleCommand {

    /**
     * Return the immediate response message from a console command
     * @return the response String
     * @throws ZosConsoleException
     */
    public String getResponse() throws ZosConsoleException;

    /**
     * Return the delayed response message from the current console command. May be called multiple times
     * @return the response string
     * @throws ZosConsoleException
     */
    public String requestResponse() throws ZosConsoleException;
    
    /**
     * Return the command
     * @return the command String
     */
    public String getCommand();

}
