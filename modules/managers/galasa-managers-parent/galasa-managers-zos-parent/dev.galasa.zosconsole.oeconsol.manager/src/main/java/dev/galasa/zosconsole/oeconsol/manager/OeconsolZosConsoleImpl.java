/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosconsole.oeconsol.manager;

import dev.galasa.ICredentials;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosconsole.IZosConsole;
import dev.galasa.zosconsole.IZosConsoleCommand;
import dev.galasa.zosconsole.ZosConsoleException;
import dev.galasa.zosconsole.ZosConsoleManagerException;
import dev.galasa.zosconsole.oeconsol.manager.internal.properties.OeconsolPath;
import dev.galasa.zosunixcommand.IZosUNIXCommand;

/**
 * Implementation of {@link IZosConsole} using zOS/MF
 *
 */
public class OeconsolZosConsoleImpl implements IZosConsole {
	
    private OeconsolZosConsoleManagerImpl oeconsolZosConsoleManager;
    private IZosImage image;
	private String oeconsolPath;
    
    public OeconsolZosConsoleImpl(OeconsolZosConsoleManagerImpl oeconsolZosConsoleManager, IZosImage image) throws ZosConsoleManagerException {
        this.oeconsolZosConsoleManager = oeconsolZosConsoleManager;
        this.image = image;
        this.oeconsolPath = OeconsolPath.get(image.getImageID());
    }

	@Override
    public IZosConsoleCommand issueCommand(String command) throws ZosConsoleException {
        return issueCommand(command, null);
    }

    @Override
    public IZosConsoleCommand issueCommand(String command, String consoleName) throws ZosConsoleException {
        IZosUNIXCommand unixCommand = (IZosUNIXCommand) oeconsolZosConsoleManager.getZosUNIXCommand(this.image);
        ICredentials credentials = null;
        if (consoleName != null) {
        	credentials = oeconsolZosConsoleManager.getCredentials(consoleName, this.image);
        	if (credentials == null) {
        		throw new ZosConsoleException("Unable to get user credentials for console name " + consoleName);
        	}
        }
		OeconsolZosConsoleCommandImpl zosConsoleCommand = new OeconsolZosConsoleCommandImpl(unixCommand, this.oeconsolPath, this.image.getImageID(), command, consoleName, credentials);

    	return zosConsoleCommand.issueCommand();
    }
    
    @Override
    public String toString() {
    	return this.image.getImageID();
    }
}
