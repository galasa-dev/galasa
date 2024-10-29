/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.internal.properties.DefaultLogonGmText;
import dev.galasa.cicsts.internal.properties.DefaultLogonInitialText;
import dev.galasa.cicsts.spi.ICicsRegionLogonProvider;
import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.zos3270.Zos3270Exception;

public class CicstsDefaultLogonProvider implements ICicsRegionLogonProvider {

    private static final Log logger = LogFactory.getLog(CicstsDefaultLogonProvider.class);
    private final ICredentialsService cs;
    private final IConfidentialTextService cts;

    private final String initialText;
    private final String gmText;

    public CicstsDefaultLogonProvider(IFramework framework) throws CicstsManagerException {

        try {
            this.cs = framework.getCredentialsService();
        } catch (CredentialsException e) {
            throw new CicstsManagerException("Could not obtain the Credentials service.", e);
        }

        this.cts = framework.getConfidentialTextService();

        try {
            initialText = DefaultLogonInitialText.get();
            gmText = DefaultLogonGmText.get();
        } catch (CicstsManagerException e) {
            throw new CicstsManagerException("Problem retrieving logon text for the default logon provider", e);
        }
    }

    @Override
    public boolean logonToCicsRegion(ICicsTerminal cicsTerminal) throws CicstsManagerException {

        try {
            if (!cicsTerminal.isConnected()) {
                cicsTerminal.connect();
            }

            // Ensure we can type something first
            cicsTerminal.waitForKeyboard();

            // Check we are at the right screen
            if (initialText != null) {
                checkForInitialText(cicsTerminal);
            }

            cicsTerminal.type("LOGON APPLID(" + cicsTerminal.getCicsRegion().getApplid() + ")").enter().wfk();

            waitForGmText(cicsTerminal);

            logger.debug("Logged onto " + cicsTerminal.getCicsRegion());

            // If loginCredentialsTag is provided, attempt to sign-in
            // via CESL
            if (!cicsTerminal.getLoginCredentialsTag().isEmpty()) {
                ICredentialsUsernamePassword creds = (ICredentialsUsernamePassword)this.cs.getCredentials(cicsTerminal.getLoginCredentialsTag());
                cts.registerText(creds.getPassword(), "Password for credential tag: " + cicsTerminal.getLoginCredentialsTag());

                // Are we already on CESL/N? If not go to it
                long timeout = 0;
                if (!cicsTerminal.searchText("Signon to CICS", timeout)) {
                    cicsTerminal.clear().wfk();

                    cicsTerminal.type("CESL").enter().wfk();
                }

                cicsTerminal.waitForTextInField(new String[]{"Userid"}, new String[]{"Security is not active"});
                cicsTerminal.type(creds.getUsername());
                cicsTerminal.positionCursorToFieldContaining("Password");
                cicsTerminal.tab();
                cicsTerminal.type(creds.getPassword());
                cicsTerminal.enter().wfk();

                waitForLoggedOnText(cicsTerminal);
                logger.debug("Logged into CICS TS as user: " + creds.getUsername());
            }

            cicsTerminal.clear().wfk();

        } catch (Zos3270Exception | CredentialsException e) {
            throw new CicstsManagerException("Problem logging onto the CICS region");
        }

        return true;
    }

    private void checkForInitialText(ICicsTerminal cicsTerminal) throws CicstsManagerException {
        try {
            cicsTerminal.waitForTextInField(initialText);
        } catch (Exception e) {
            throw new CicstsManagerException(
                    "Unable to logon to CICS, initial screen does not contain '" + initialText + "'");
        }
    }

    private void waitForGmText(ICicsTerminal cicsTerminal) throws CicstsManagerException {
        try {
            cicsTerminal.waitForTextInField(gmText);
        } catch (Exception e) {
            throw new CicstsManagerException("Unable to wait for the initial CICS screen, looking for '" + gmText + "'",
                    e);
        }
    }

    private void waitForLoggedOnText(ICicsTerminal cicsTerminal) throws CicstsManagerException {

        String[] pass = { "Sign-on is complete" };
        String[] fail = { 
            "Your password has expired. Please type your new password.",
            "Invalid credentials entered",
            "userid has been revoked"
        };

        try {
            cicsTerminal.waitForTextInField(pass, fail);
        } catch (Exception e) {
            throw new CicstsManagerException("Unable to wait for the initial CICS screen, looking for '" + String.join("', '", pass) + "'",
                e);
        }
    }

}
