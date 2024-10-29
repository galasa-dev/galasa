/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosunixcommand.ssh.manager.internal;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsername;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.ipnetwork.IpNetworkManagerException;
import dev.galasa.ipnetwork.SSHAuthFailException;
import dev.galasa.ipnetwork.spi.IIpNetworkManagerSpi;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosunixcommand.IZosUNIXCommand;
import dev.galasa.zosunixcommand.ZosUNIXCommandAuthFailException;
import dev.galasa.zosunixcommand.ZosUNIXCommandException;

/**
 * Implementation of {@link IZosUNIXCommand} using ssh
 *
 */
public class ZosUNIXCommandImpl implements IZosUNIXCommand {

    private IIpNetworkManagerSpi ipNetworkManager;
	private IZosImage image;
	private ICredentials defaultCredentials;
	private IIpHost host;
    
    private static final Log logger = LogFactory.getLog(ZosUNIXCommandImpl.class);
    
    private static final String UNABLE_TO_ISSUE_ZOS_UNIX_COMMAND = "Unable to issue zOS UNIX Command";
    
    public ZosUNIXCommandImpl(IIpNetworkManagerSpi ipNetworkManager, IZosImage image) {
    	this.image = image;
        this.ipNetworkManager = ipNetworkManager;
        this.host = image.getIpHost();
    }

    @Override
    public String issueCommand(@NotNull String command) throws ZosUNIXCommandException {
        return issueCommand(command, getDefaultCredentials());
    }

    @Override
    public String issueCommand(@NotNull String command, long timeout) throws ZosUNIXCommandException {
        return issueCommand(command, timeout, getDefaultCredentials());
    }

	@Override
	public String issueCommand(@NotNull String command, ICredentials credentials) throws ZosUNIXCommandException {
        String commandResponse;
        try {
            logger.debug("About to issue command :" + command);
            commandResponse = getCommandShell(credentials).issueCommand(command);
            logger.debug("response :" + commandResponse);
        } catch (IpNetworkManagerException e) {
        	throw new ZosUNIXCommandException(UNABLE_TO_ISSUE_ZOS_UNIX_COMMAND, e);
        }
        return commandResponse;
	}

	@Override
	public String issueCommand(@NotNull String command, long timeout, ICredentials credentials) throws ZosUNIXCommandException {
	    String commandResponse;
	    try {
	        logger.debug("About to issue command :" + command);
	        commandResponse = getCommandShell(credentials).issueCommand(command, timeout);
	        logger.debug("response :" + commandResponse);
        } catch (IpNetworkManagerException e) {
        	throw new ZosUNIXCommandException(UNABLE_TO_ISSUE_ZOS_UNIX_COMMAND, e);
        }
	    return commandResponse;
	}

	protected ICredentials getDefaultCredentials() throws ZosUNIXCommandException {
		if (this.defaultCredentials == null) {
	        try {
	        	this.defaultCredentials = image.getDefaultCredentials();
	        } catch (ZosManagerException e) {
	            throw new ZosUNIXCommandException("Unable to get default credentials for image " + image.getImageID(), e);
	        }
		}
		return this.defaultCredentials;
	}

	protected ICommandShell getCommandShell(ICredentials credentials) throws ZosUNIXCommandException {
	    ICommandShell commandShell;
		try {
	        commandShell = ipNetworkManager.getCommandShell(host, credentials);
	        commandShell.reportResultStrings(true);
	        commandShell.connect();
	    } catch (SSHAuthFailException e) {
	    	throw new ZosUNIXCommandAuthFailException(UNABLE_TO_ISSUE_ZOS_UNIX_COMMAND + logAuthFail(credentials), e);
	    } catch (IpNetworkManagerException e) {
	    	throw new ZosUNIXCommandException(UNABLE_TO_ISSUE_ZOS_UNIX_COMMAND, e);
	    }
		return commandShell;
	}

	protected String logAuthFail(ICredentials credentials) {
		String authFail = " - authentication failed";
		String username = null;
		if (credentials instanceof ICredentialsUsername) {
			username = ((ICredentialsUsername) credentials).getUsername();
		}
		if (username != null) {
			authFail = " - user name '" + username + "' not authenticated";
		}
		return authFail;
	}
}
