/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;


import com.google.gson.JsonObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.IDockerExec;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGson;

/**
 * DockerExecImpl. An object passed back used to monitor and control the exec process on a container.
 * 
 *   
 */
public class DockerExecImpl implements IDockerExec {

    private final IFramework                                framework;
    private final DockerManagerImpl                         dockerManager;
    private final DockerContainerImpl                       dockerContainer;
    private final DockerEngineImpl                          dockerEngine;
    private final List<String>                              commands;
    private final int                                       timeout;
    private final ExecThread                                execThread;
    private final String                                    id;
    
    private HttpURLConnection                               conn = null;
    private boolean                                         finished;
    private final StringBuffer                              outputBuffer = new StringBuffer();
    private long                                            exitCode = -1;
    private GalasaGson                                      gson = new GalasaGson();

    private static final Log                                logger = LogFactory.getLog(DockerExecImpl.class);

    /**
     * 
     * Creates the exec Json to be sent to docker engine.
     * 
     * @param framework
     * @param dockerManager
     * @param dockerContainer
     * @param timeout
     * @param commands
     * @throws DockerManagerException
     */
    public DockerExecImpl(IFramework framework, DockerManagerImpl dockerManager, DockerContainerImpl 
            dockerContainer, int timeout, String[] commands) throws DockerManagerException {
        this.framework                  = framework;
        this.dockerManager              = dockerManager;
        this.dockerContainer            = dockerContainer;
        this.timeout                    = timeout;
        this.commands                   = Arrays.asList(commands);
        this.dockerEngine               = dockerContainer.getDockerEngineImpl();

        try{
            ExecJson eJson = new ExecJson(false, true, true, true, this.commands);
            String json = gson.toJson(eJson);

            JsonObject cmd = gson.fromJson(json, JsonObject.class);

            JsonObject response = dockerEngine.sendExecCommands(dockerContainer.getContainerId(), cmd);
            if(response == null){
                throw new DockerManagerException("Did not receive a response from exec start for command");
            }

            // command ID not container ID
            id = response.get("Id").getAsString();
            if (id == null || id.trim().isEmpty()) {
                throw new DockerManagerException("Invalid response received from exec start for command - " + response.getAsString());
            }

            StringBuffer sb = new StringBuffer();
            for(String c : commands) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(c);
            }

            logger.info("Issuing command to Docker container '" + sb.toString() + "'");

            execThread = new ExecThread();
            execThread.start();
            logger.info("Command started");

        } catch(Exception e) {
            finished = true;
            throw new DockerManagerException("Failed to exec: ", e);
        }

    }

    /**
     * Standard wait with timeout for exec
     * 
     * @throws DockerManagerException
     */
    @Override
    public boolean waitForExec() throws DockerManagerException {
       return waitForExec(120000);
    }

    @Override
    public HttpURLConnection getConnection() {
        return conn;
    }

    /**
     * Wait for exec with specidied timeout
     * 
     * @param timeout
     * @throws DockerManagerException
     */
    @Override
    public boolean waitForExec(long timeout) throws DockerManagerException {
        long endTime = System.currentTimeMillis() + timeout;
        while(!finished && System.currentTimeMillis() < endTime) {
            try {
                Thread.sleep(100);
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new DockerManagerException("Wait for exec was interrupted", e);    
            }
        }
        return finished;
    }

    /**
     * Returns boolean if exec command is completed.
     */
    @Override
    public boolean isFinished() {
        return finished;
    }

    /**
     * Returns the last consoloe output line from the container.
     */
    @Override
    public String getCurrentOutput() {
        return outputBuffer.toString();
    }

    /**
     * Returns exitCdoe from the container.
     */
    @Override
    public long getExitCode() {
        return exitCode;
    }

    /**
     * A separate thread for the exec to the container to be performed in
     */
    private class ExecThread extends Thread {

        /**
         * Run the exec thread
         */
		@Override
		public void run() {
			InputStream is = null;
			OutputStream os = null;
			try {
				URL url = new URL(dockerEngine.getURI() + "/exec/" + id + "/start");
				conn = (HttpURLConnection) url.openConnection();

				conn.setConnectTimeout(timeout);
				conn.setReadTimeout(timeout);

				conn.setDoOutput(true);
				conn.setDoInput(true);

				conn.addRequestProperty("Content-Type", "application/json");

				conn.connect();

				os = conn.getOutputStream();
				os.write("{\"Detach\": false, \"Tty\": true}".getBytes());
				os.close();
				os = null;

				is = conn.getInputStream();
				byte[] buffer = new byte[1024];
				int len = 0;

				while((len = is.read(buffer)) >= 0) {
					String data = new String(buffer, 0, len);
					outputBuffer.append(data);
				}
				is.close();
				is = null;
				
				JsonObject status = dockerEngine.getExecInfo(id);
				String exitCodeObj = status.get("ExitCode").getAsString();
				if (exitCodeObj != null) {
					exitCode = Long.parseLong(exitCodeObj);
				}
                
				logger.debug("Command completed with exitcode " + exitCode);
				
				finished = true;
			} catch (Exception e) {
                logger.error("Failure during exec running", e);
                Thread.currentThread().interrupt();
			} finally {
                try{
					if (is!=null) {
						is.close();
					}
                    if (os!=null)  {
						os.close();
					}
                } catch (IOException e) {
                    logger.info("Failed to close stream, failing quietly: " + e);
                }
			}			
		}
    }
    
    /**
     * Expected json object for the docker engine API.
     */
    private class ExecJson {
        protected boolean attachStdIn;
        protected boolean attachStdOut;
        protected boolean attachStdErr;
        protected boolean tty;
        protected List<String> cmd;

        public ExecJson (boolean attachStdIn, boolean attachStdOut, boolean attachStdErr,
                 boolean tty, List<String> commands) {
            this.attachStdIn    = attachStdIn;
            this.attachStdOut   = attachStdOut;
            this.attachStdErr   = attachStdErr;
            this.tty            = tty;
            this.cmd            = commands;
        }
    }
}
