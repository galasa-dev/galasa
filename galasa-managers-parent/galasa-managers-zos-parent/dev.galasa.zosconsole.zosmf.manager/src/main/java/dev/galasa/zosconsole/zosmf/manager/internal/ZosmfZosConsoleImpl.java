/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosconsole.zosmf.manager.internal;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosconsole.IZosConsole;
import dev.galasa.zosconsole.IZosConsoleCommand;
import dev.galasa.zosconsole.ZosConsoleException;
import dev.galasa.zosconsole.ZosConsoleManagerException;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.ZosmfManagerException;

/**
 * Implementation of {@link IZosConsole} using zOS/MF
 *
 */
public class ZosmfZosConsoleImpl implements IZosConsole {

    private List<ZosmfZosConsoleCommandImpl> zosConsoleCommands = new ArrayList<>();
    private IZosmfRestApiProcessor zosmfApiProcessor;
    private IZosImage image;
    
    public ZosmfZosConsoleImpl(IZosImage image, ZosmfZosConsoleManagerImpl zosConsoleManager) throws ZosConsoleException {
        this.image = image;
        try {
			this.zosmfApiProcessor = zosConsoleManager.getZosmfManager().newZosmfRestApiProcessor(image, zosConsoleManager.getZosManager().getZosConsolePropertyConsoleRestrictToImage(image.getImageID()));
		} catch (ZosmfManagerException | ZosConsoleManagerException e) {
			throw new ZosConsoleException(e);
		}
    }

    @Override
    public @NotNull IZosConsoleCommand issueCommand(@NotNull String command) throws ZosConsoleException {
        return issueCommand(command, null);
    }

    @Override
    public @NotNull IZosConsoleCommand issueCommand(@NotNull String command, String consoleName) throws ZosConsoleException {
        ZosmfZosConsoleCommandImpl zosConsoleCommand = new ZosmfZosConsoleCommandImpl(this.zosmfApiProcessor, command, consoleName(consoleName), this.image);
        this.zosConsoleCommands.add(zosConsoleCommand);
        
        return zosConsoleCommand.issueCommand();
    }

    protected String consoleName(String consoleName) throws ZosConsoleException {
        if (consoleName == null) {
            try {
                ICredentials creds = image.getDefaultCredentials();
                if (!(creds instanceof ICredentialsUsernamePassword)) {
                    throw new ZosConsoleException("Unable to get the run username for image "  + image.getImageID());
                }
                return ((ICredentialsUsernamePassword) creds).getUsername();
            } catch (ZosManagerException e) {
                throw new ZosConsoleException("Unable to get the run username for image "  + image.getImageID());
            }
        }
        if (consoleName.length() < 2 || consoleName.length() > 8) {
            throw new ZosConsoleException("Invalid console name \"" + consoleName + "\" must be between 2 and 8 characters long");
        }
        return consoleName;
    }
    
    @Override
    public String toString() {
    	return this.image.getImageID();
    }
}
