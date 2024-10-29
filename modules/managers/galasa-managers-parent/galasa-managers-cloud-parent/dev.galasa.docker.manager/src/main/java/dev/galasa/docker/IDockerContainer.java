/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker;

import java.util.List;
import java.util.Map;
import java.io.InputStream;
import java.net.InetSocketAddress;

/**
 * Docker Container Resource Object
 * 
 * Provides a resource object representing a Docker Container on a Docker
 * Engine. The normal method of obtaining a Docker Container resource object is
 * by using a field:-<br>
 * <code>@DockerContainer(dockerContainerTag="a", image="galasadocker/ivp:1.0.0")<br>
 * private IDockerContainer containerA;</code><br>
 * 
 * The following are DSE Environment properties:-<br>
 * <code>docker.container.TAG.name=BOB</code> to define the name the container
 * is to use.<br>
 * <code>docker.container.TAG.leave.running=true</code> to inform the DSE that
 * the container is not to be killed at startup or stopped once the test is
 * complete.<br>
 * <code>docker.engine=http://localhost:2375</code> the Docker Server/Swarm the
 * container is to run on.<br>
 * <code>docker.registries=http://localhost:5000</code> a list of Docker
 * registries to search for an image<br>
 */
public interface IDockerContainer {
    /**
	 * Fetch the Resource Object representing the Docker Image of this container.
	 * 
	 * @return a {@link IDockerImage} for this container - never null
	 */
	public IDockerImage getDockerImage();

	/**
	 * Returns a map of all the exposed ports of the container and the real host ports
	 * they have been mapped to.   An exposed port can be mapped to more than one host port.
	 * The exposed port in the format used by docker, eg if tcp port 80 is exposed by the image, then 
	 * the port will be mapped to <code>"tcp/80"</code>.  The {@link InetSocketAddress} will contain the 
	 * ip address of the host.
	 * 
	 * @return a map of the exposed ports,  never null
	 * @throws DockerManagerException
	 */
	public Map<String, List<InetSocketAddress>> getExposedPorts() throws DockerManagerException;
	/**
	 * A convenience method to obtain the first socket of an exposed port.   Normally 
	 * an exposed port will only have one real socket, so this will usual way of obtaining the 
	 * socket of an exposed port. 
	 * @param exposedPort - the name of the exposed port - eg <code>"tcp/80"</code>
	 * @return {@link InetSocketAddress} of the first real port, or null if not exposed or not mapped
	 */
	public InetSocketAddress getFirstSocketForExposedPort(String exposedPort);
	/**
	 * A convenience method to obtain a random socket for an exposed port that has been mapped to 
	 * more than one host socket.  Similar to {@link #getFirstSocketForExposedPort(String)}.
	 * 
	 * @param exposedPort - the name of the exposed port - eg <code>"tcp/80"</code>
	 * @return {@link InetSocketAddress} of a random real port, or null if not exposed or not mapped
	 */
	public InetSocketAddress getRandomSocketForExposedPort(String exposedPort);

	/**
	 * Start the Docker Container.  The Docker Manager does not validate that the 
	 * container is down, so if up it will throw an exception with the remote api failure, likely to 
	 * be NOT MODIFIED.
	 * 
	 * @throws DockerManagerException
	 */
	public void start() throws DockerManagerException;

	/**
	 * Start the Docker Container with a provided galasa DockerContainerConfig. This will stop and remove
	 * any previous containers.
	 * 
	 * @param config
	 * @throws DockerManagerException
	 */
	public void startWithConfig(IDockerContainerConfig config) throws DockerManagerException;
	
	/**
	 * Stop the Docker Container.  The Docker Manager does not validate that the 
	 * container is up, so if down it will throw an exception with the remote api failure, likely to 
	 * be NOT MODIFIED.
	 * @throws DockerManagerException
	 */
	public void stop() throws DockerManagerException;
	
	/**
	 * 
	 * Equivalent to exec(defaultResourceTimeout, commands...);
	 * 
	 * @see {@link #exec(int, String...)}
	 * @param command - An array of command and its parameters 
	 * @return
	 * @throws DockerManagerException
	 */
	public IDockerExec exec(String... command) throws DockerManagerException;
	/**
	 * Issue a command to a running container.  Will return a {@link IDockerExec} resource object.
	 * The command will continue to execute in the background. 
	 * <br>Use:-<br>
	 * <code>container.exec("ls","-l","/var/log");</code>
	 * 
	 * @param timeout - A timeout in milliseconds for the command to send output
	 * @param command - An array of command and its parameters 
	 * @return {@link IDockerExec}
	 * @throws DockerManagerException
	 */
	public IDockerExec exec(int timeout, String... command) throws DockerManagerException;

	/**
	 * Retrieve the full STDOUT for the Docker Container 
	 * 
	 * @return Full Container STDOUT contents
	 * @throws DockerManagerException
	 */
	public String retrieveStdOut() throws DockerManagerException;

	/**
	 * Retrieve the full STDERR for the Docker Container 
	 * 
	 * @return Full Container STDERR contents
	 * @throws DockerManagerException
	 */
	public String retrieveStdErr() throws DockerManagerException;
	
	/**
	 * Checks with the docker engine to find the running state of this container.
	 * 
	 * @return boolean
	 * @throws DockerManagerException
	 */
	public boolean isRunning() throws DockerManagerException;
	
	/**
	 * Retireves the exit code from the container
	 * 
	 * @return long
	 * @throws DockerManagerException
	 */
	public long getExitCode() throws DockerManagerException;

	/**
	 * Stores a file onto the container. Path must be fully qualified including the name of the file
	 * on the container.
	 * 
	 * @param absolutePath
	 * @param file
	 * @throws DockerManagerException
	 */
	public void storeFile(String absolutePath, InputStream file) throws DockerManagerException;

	/**
	 * Retreievs a InputStream of a file on the container.
	 * 
	 * @param path
	 * @return
	 * @throws DockerManagerException
	 */
	public InputStream retrieveFile(String path) throws DockerManagerException;
	
	/**
	 * Retrieves a file from the container and passes the contents back as a string.
	 * 
	 * @param path
	 * @return String
	 * @throws DockerManagerException
	 */
	public String retrieveFileAsString(String path) throws DockerManagerException;

}