/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosconsole.oeconsol.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsername;
import dev.galasa.zosconsole.IZosConsoleCommand;
import dev.galasa.zosconsole.ZosConsoleException;
import dev.galasa.zosunixcommand.IZosUNIXCommand;
import dev.galasa.zosunixcommand.ZosUNIXCommandAuthFailException;
import dev.galasa.zosunixcommand.ZosUNIXCommandException;

/**
 * Implementation of {@link IZosConsoleCommand} using oeconsol
 *
 */
public class OeconsolZosConsoleCommandImpl implements IZosConsoleCommand {
	private static Log logger = LogFactory.getLog(OeconsolZosConsoleCommandImpl.class);
    
    private IZosUNIXCommand zosUnixCommand;
	private String oeconsolPath;
	private String imageId;
    
    private String command;
    private String commandImmediateResponse;
	private String consoleName;
	private ICredentials credentials;

    public OeconsolZosConsoleCommandImpl(IZosUNIXCommand zosUnixCommand, String oeconsolPath, String imageId, String command, String consoleName, ICredentials credentials) {
        this.zosUnixCommand = zosUnixCommand;
        this.oeconsolPath = oeconsolPath;
        this.imageId = imageId;
        this.command = command;
        this.consoleName = consoleName;
        this.credentials = credentials;
    }

	public IZosConsoleCommand issueCommand() throws ZosConsoleException {
    	try {
    		String consoleCommand = "Issuing command '" + command + "' on image '" + this.imageId + "'";
			if (credentials == null) {
    			this.commandImmediateResponse = this.zosUnixCommand.issueCommand(buildCommand(this.command));
    			logger.debug(consoleCommand + " using default credentials");
    		} else {
    			logger.debug(consoleCommand  + " with console name '" + this.consoleName + "' using credentials for user name '" + ((ICredentialsUsername) credentials).getUsername() + "'");
    			this.commandImmediateResponse = this.zosUnixCommand.issueCommand(buildCommand(this.command), credentials);
    		}
		} catch (ZosUNIXCommandAuthFailException e) {
			throw new ZosConsoleException("Unable to issue console command '" + this.command + "'" + " - user not authenticated", e);
		} catch (ZosUNIXCommandException e) {
			throw new ZosConsoleException("Unable to issue console command '" + this.command + "'", e);
		}
        return this;
    }

    @Override
    public String getResponse() throws ZosConsoleException {
        return this.commandImmediateResponse;
    }

    @Override
    public String requestResponse() throws ZosConsoleException {
    	throw new ZosConsoleException("oeconsol does not provide support for delayed response");
    }

    @Override
    public String getCommand() {
        return this.command;
    }

    protected String buildCommand(String command) {
        StringBuilder builtCommand = new StringBuilder();
        builtCommand.append(this.oeconsolPath);
        builtCommand.append(" \'");
        builtCommand.append(command.replaceAll("\\\'", "\\\'\\\"\\\'\\\"\\\'"));
        builtCommand.append("\'");
        return builtCommand.toString();
    }
}
