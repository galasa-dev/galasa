/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker;

import java.net.HttpURLConnection;

/**
 * Docker Exec Resource Object that represents a command being executed on the
 * container.
 * <p>
 * 
 *   
 *
 */
public interface IDockerExec {

	/**
	 * Equivlant to waitForExec(10000);
	 * 
	 * @return - true if finished in time, false if not
	 * @throws DockerManagerException
	 */
	public boolean waitForExec() throws DockerManagerException;

	/**
	 * Wait for the command to finish.
	 * 
	 * @param timeout - timeout in milliseconds
	 * @return - true if finished in time, false if not
	 * @throws DockerManagerException
	 */
	public boolean waitForExec(long timeout) throws DockerManagerException;

	/**
	 * Has the command finished
	 * 
	 * @return true if finished, false if not
	 */
	public boolean isFinished();

	/**
	 * Returns the current/finished output of the command, will always return all the output
	 * 
	 * @return command output
	 */
	public String getCurrentOutput();

	/**
	 * The exitcode of the command, or -1 if the command has not completed
	 * 
	 * @return exit code
	 */
	public long getExitCode();

	public HttpURLConnection getConnection();
	
	
}