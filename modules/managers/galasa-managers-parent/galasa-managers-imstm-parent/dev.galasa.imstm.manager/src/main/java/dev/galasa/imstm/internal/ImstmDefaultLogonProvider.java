/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ICredentialsUsername;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.imstm.ImstmManagerException;
import dev.galasa.imstm.IImsSystem;
import dev.galasa.imstm.IImsTerminal;
import dev.galasa.imstm.spi.IImsSystemLogonProvider;
import dev.galasa.zos.IZosImage;
import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.zos3270.Zos3270Exception;

public class ImstmDefaultLogonProvider implements IImsSystemLogonProvider {

    private static final Log logger = LogFactory.getLog(ImstmDefaultLogonProvider.class);
    private final ICredentialsService cs;
    private final IConfidentialTextService cts;

    private static final String SIGNON_CHALLENGE = "DFS3649A";
    private static final String[] SIGNON_SUCCESSFUL = { "DFS3650I" };
    // Any signon failure will give us DFS3649A again, with REJECTED and a reason code.
    // However, we can't search for DFS3649A as it's already on the screen.
    private static final String[] SIGNON_FAILED = { "REJECTED" };  

    public ImstmDefaultLogonProvider(IFramework framework) throws ImstmManagerException {

        try {
            this.cs = framework.getCredentialsService();
        } catch (CredentialsException e) {
            throw new ImstmManagerException("Could not obtain the Credentials service.", e);
        }

        this.cts = framework.getConfidentialTextService();
    }

    @Override
    public boolean logonToImsSystem(IImsTerminal imsTerminal) throws ImstmManagerException {

        try {
            if (!imsTerminal.isConnected()) {
                imsTerminal.connect();
            }

            // Ensure we can type something first
            imsTerminal.waitForKeyboard();

            IImsSystem system = imsTerminal.getImsSystem();
            IZosImage zos = system.getZosImage();

            // Check we are at the right screen
            String initialText = zos.getLogonInitialText();
            if (initialText != null) {
                checkForInitialText(imsTerminal, initialText);
            }

            imsTerminal.type(system.getZosImage().getVtamLogonString(system.getApplid())).enter().wfk();

            waitForSignonScreen(imsTerminal);
            logger.debug("Logged onto " + system);

            if (imsTerminal.getLoginCredentialsTag().isEmpty()) {
                throw new ImstmManagerException("No login credentials provided");
            } else {
                ICredentialsUsername creds = (ICredentialsUsername) this.cs.getCredentials(imsTerminal.getLoginCredentialsTag());

                imsTerminal.positionCursorToFieldContaining("USERID:");
                imsTerminal.tab();
                imsTerminal.type(creds.getUsername());
                if (creds instanceof ICredentialsUsernamePassword) {
                    String pw = ((ICredentialsUsernamePassword) creds).getPassword();
                    cts.registerText(pw, "Password for credential tag: " + imsTerminal.getLoginCredentialsTag());
                    imsTerminal.positionCursorToFieldContaining("PASSWORD:");
                    imsTerminal.tab();
                    imsTerminal.type(pw);
                    }
                imsTerminal.enter().wfk();

                waitForSignedOnText(imsTerminal);
                logger.debug("Logged into IMS TM as user: " + creds.getUsername());
            }

            imsTerminal.clear().wfk();

        } catch (Zos3270Exception | CredentialsException e) {
            throw new ImstmManagerException("Problem logging onto the IMS system", e);
        }

        return true;
    }

    private void checkForInitialText(IImsTerminal imsTerminal, String initialText) throws ImstmManagerException {
        try {
            imsTerminal.waitForTextInField(initialText);
        } catch (Exception e) {
            throw new ImstmManagerException(
                    "Unable to logon to IMS, initial screen does not contain '" + initialText + "'");
        }
    }

    private void waitForSignonScreen(IImsTerminal imsTerminal) throws ImstmManagerException {
        try {
            imsTerminal.waitForTextInField(SIGNON_CHALLENGE);
        } catch (Exception e) {
            throw new ImstmManagerException("Unable to wait for the initial IMS screen, looking for '" + SIGNON_CHALLENGE + "'",
                    e);
        }
    }

    private void waitForSignedOnText(IImsTerminal imsTerminal) throws ImstmManagerException {
        try {
            imsTerminal.waitForTextInField(SIGNON_SUCCESSFUL, SIGNON_FAILED);
        } catch (Exception e) {
            throw new ImstmManagerException("Unable to sign on, looking for '" + String.join("', '", SIGNON_SUCCESSFUL) + "'",
                e);
        }
    }

}
