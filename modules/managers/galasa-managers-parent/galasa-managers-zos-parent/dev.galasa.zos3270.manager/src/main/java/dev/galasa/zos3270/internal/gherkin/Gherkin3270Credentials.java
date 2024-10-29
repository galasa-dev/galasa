/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.gherkin;

import java.util.List;
import java.util.Map;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsername;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.IGherkinExecutable;
import dev.galasa.framework.spi.IStatementOwner;
import dev.galasa.framework.spi.language.gherkin.ExecutionMethod;
import dev.galasa.framework.spi.language.gherkin.GherkinKeyword;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TextNotFoundException;
import dev.galasa.zos3270.Zos3270Exception;
import dev.galasa.zos3270.Zos3270ManagerException;
import dev.galasa.zos3270.internal.Zos3270ManagerImpl;
import dev.galasa.zos3270.spi.Zos3270TerminalImpl;

public class Gherkin3270Credentials  implements IStatementOwner {

    private final Gherkin3270Coordinator gerkinCoordinator;
    private final Zos3270ManagerImpl     manager;

    public Gherkin3270Credentials(Gherkin3270Coordinator gerkinCoordinator, Zos3270ManagerImpl manager) {
        this.gerkinCoordinator = gerkinCoordinator;
        this.manager = manager;
    }

    @ExecutionMethod(keyword = GherkinKeyword.AND, regex = "type credentials( \\w+)? username on terminal( \\w+)?")
    public void typeUsername(IGherkinExecutable executable, Map<String,Object> testVariables) throws Zos3270ManagerException, Zos3270Exception, TextNotFoundException, TerminalInterruptedException {
        List<String> groups = executable.getRegexGroups();  

        String terminalId = Gherkin3270Coordinator.defaultTerminaId(groups.get(1));
        String credentialsId = AbstractManager.nulled(groups.get(0));

        Zos3270TerminalImpl terminal = this.gerkinCoordinator.getTerminal(terminalId);
        if (terminal == null ) {
            throw new Zos3270ManagerException("Unable to get terminal "+terminalId);
        }
        if (!terminal.isConnected()) {
            throw new Zos3270ManagerException("Terminal '" + terminalId + "' is not connected");
        }

        ICredentials credentials = getCredentials(credentialsId, terminalId);

        String username = null;

        if (credentials instanceof ICredentialsUsername) {
            username = ((ICredentialsUsername)credentials).getUsername();
        } else {
            throw new Zos3270ManagerException("Unrecognised credentials type " + credentials.getClass().getName());
        }

        terminal.type(username);

    }
    
    
    @ExecutionMethod(keyword = GherkinKeyword.AND, regex = "type credentials( \\w+)? password on terminal( \\w+)?")
    public void typePassword(IGherkinExecutable executable, Map<String,Object> testVariables) throws Zos3270ManagerException, Zos3270Exception, TextNotFoundException, TerminalInterruptedException {
        List<String> groups = executable.getRegexGroups();  

        String terminalId = Gherkin3270Coordinator.defaultTerminaId(groups.get(1));
        String credentialsId = AbstractManager.nulled(groups.get(0));

        Zos3270TerminalImpl terminal = this.gerkinCoordinator.getTerminal(terminalId);
        if (terminal == null ) {
            throw new Zos3270ManagerException("Unable to get terminal "+terminalId);
        }
        if (!terminal.isConnected()) {
            throw new Zos3270ManagerException("Terminal '" + terminalId + "' is not connected");
        }

        ICredentials credentials = getCredentials(credentialsId, terminalId);

        String password = null;

        if (credentials instanceof ICredentialsUsernamePassword) {
            password = ((ICredentialsUsernamePassword)credentials).getPassword();
        } else {
            throw new Zos3270ManagerException("Unrecognised credentials type " + credentials.getClass().getName());
        }
        
        this.manager.getFramework().getConfidentialTextService().registerText(password, "Password for credentials");

        terminal.type(password);

    }
    
    
    private ICredentials getCredentials(String credentialsId, String terminalId) throws Zos3270ManagerException {
        try {
            if (credentialsId == null) { // use default zos credentials
                String imageTag = this.gerkinCoordinator.getImageTagForTerminal(terminalId);
                IZosImage image = this.manager.getZosManager().getImageForTag(imageTag);

                return image.getDefaultCredentials();
            } else {
                ICredentials credentials = this.manager.getFramework().getCredentialsService().getCredentials(credentialsId);
                if (credentials == null) {
                    throw new Zos3270Exception("Missing credentials for id '" + credentialsId + "'");
                }
                return credentials;
            }
        } catch(Exception e) {
            throw new Zos3270ManagerException("Unable to retrieve credentials", e);
        }
        
    }

}
