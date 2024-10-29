/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ipnetwork;

public interface ICommandShell {

    public String issueCommand(String command) throws IpNetworkManagerException;

    /**
     * Issue a command using SSH. Equivalent to {@link #issueCommand(String, boolean, long)}

     * 
     * @param command - command to issue
     * @param timeout - time (in milliseconds) to wait with no new output appearing
     *                before timing out
     * @return the output of the command (stdout and stderr)
     * @throws SSHException
     */
    public String issueCommand(String command, long timeout) throws IpNetworkManagerException;

    /**
     * Issue a command using SSH. Equivalent to
     * {@link #issueCommand(String, boolean, long)}
     * 
     * @param command  - command to issue
     * @param newShell - if true will start a new
     * @return the output of the command (stdout and stderr)
     * @throws SSHException
     */
    public String issueCommand(String command, boolean newShell) throws IpNetworkManagerException;

    /**
     * Issue a command using SSH
     * 
     * @param command  - command to issue
     * @param newShell - if true will start a new
     * @param timeout  - time (in milliseconds) to wait with no new output appearing
     *                 before timing out
     * @return the output of the command (stdout and stderr)
     * @throws SSHException
     */
    public String issueCommand(String command, boolean newShell, long timeout) throws IpNetworkManagerException;

//	public void changeUser(String userid, String password);

    public void connect() throws IpNetworkManagerException;

    public void disconnect() throws IpNetworkManagerException;

    public void restartShell() throws IpNetworkManagerException;

    /**
     * Define the right command used to change the shell prompt
     */
    public void setChangePromptCommand(String command);

    /**
     * Issue a command using SSH shell. 
     * 
     * Equivalent to
     * {@link #issueCommandToShell(String, boolean, long)} - not valid for
     * Rexec implementation - equivalent to
     * {@link #issueCommand(String, long)} for Telnet implementation
     * 
     * @param command - command to issue
     * @return the output of the command (stdout and stderr)
     * @throws IpNetworkManagerException
     */
    public String issueCommandToShell(String command) throws IpNetworkManagerException;

    /**
     * Issue a command using SSH shell. Equivalent to
     * {@link #issueCommandToShell(String, boolean, long)} - not valid for Rexec
     * implementation - equivalent to {@link #issueCommand(String, long)}
     * for Telnet implementation
     * 
     * @param command - command to issue - if true will start a new
     * @return the output of the command (stdout and stderr)
     * @throws IpNetworkManagerException
     */
    public String issueCommandToShell(String command, long timeout) throws IpNetworkManagerException;

    /**
     * Issue a command using SSH shell. Equivalent to
     * {@link #issueCommandToShell(String, boolean, long)} - not valid for
     * Rexec implementation - equivalent to
     * {@link #issueCommand(String, long)} for Telnet implementation
     * 
     * @param command  - command to issue
     * @param newShell - if true will start a new
     * @return the output of the command (stdout and stderr)
     * @throws IpNetworkManagerException
     */
    public String issueCommandToShell(String command, boolean newShell) throws IpNetworkManagerException;

    /**
     * Issue a command using SSH shell - not valid for Rexec implementation -
     * equivalent to {@link #issueCommand(String, long)} for Telnet
     * implementation
     * 
     * @param command  - command to issue
     * @param newShell - if true will start a new
     * @param timeout  - time (in milliseconds) to wait with no new output appearing
     *                 before timing out
     * @return the output of the command
     * @throws IpNetworkManagerException
     */
    public String issueCommandToShell(String command, boolean newShell, long timeout) throws IpNetworkManagerException;

    /**
     * Ask the shell to log the result strings for all shell commands
     * 
     * @param report whether the shell should log the results of the shell commands
     */
    public void reportResultStrings(boolean report);

    /**
     * Instruct the command shell to remove ANSI escape codes from the output of commands to the shell
     */
    public void setRemoveAnsiEscapeCodes(boolean remoteAnsiEscapeCodes);

}
