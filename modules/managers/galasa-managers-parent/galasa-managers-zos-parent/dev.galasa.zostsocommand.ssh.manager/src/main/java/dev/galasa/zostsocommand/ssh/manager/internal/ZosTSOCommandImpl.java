/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zostsocommand.ssh.manager.internal;

import javax.validation.constraints.NotNull;

import dev.galasa.zostsocommand.IZosTSOCommand;
import dev.galasa.zostsocommand.ZosTSOCommandException;
import dev.galasa.zosunixcommand.IZosUNIXCommand;
import dev.galasa.zosunixcommand.ZosUNIXCommandException;

/**
 * Implementation of {@link IZosTSOCommand} using ssh
 *
 */
public class ZosTSOCommandImpl implements IZosTSOCommand {

    private IZosUNIXCommand zosUnixCommand;
	private String tsocmdPath;
    
    public ZosTSOCommandImpl(IZosUNIXCommand zosUNIXCommand, String tsocmdPath) {
    	this.zosUnixCommand = zosUNIXCommand;
    	this.tsocmdPath = tsocmdPath;
    }

    @Override
    public String issueCommand(@NotNull String command) throws ZosTSOCommandException {
        String commandResponse;
        try {
            commandResponse = this.zosUnixCommand.issueCommand(buildCommand(command));
        } catch (ZosUNIXCommandException e) {
            throw new ZosTSOCommandException("Unable to issue zOS TSO Command", e);
        }
        return commandResponse;
    }

    @Override
    public String issueCommand(@NotNull String command, long timeout) throws ZosTSOCommandException {
        String commandResponse;
        try {
            commandResponse = this.zosUnixCommand.issueCommand(buildCommand(command), timeout);
        } catch (ZosUNIXCommandException e) {
            throw new ZosTSOCommandException("Unable to issue zOS TSO Command", e);
        }
        return commandResponse;
    }

    protected String buildCommand(String command) {
        StringBuilder builtCommand = new StringBuilder();
        builtCommand.append(tsocmdPath);
        builtCommand.append(" \'");
        builtCommand.append(command.replaceAll("\\\'", "\\\'\\\"\\\'\\\"\\\'"));
        builtCommand.append("\'");
        return builtCommand.toString();
    }
}
