/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.sdv.internal;

import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.sdv.ISdvUser;
import dev.galasa.sdv.SdvManagerException;
import dev.galasa.zos3270.Zos3270Exception;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class implements the ISdvUser interface.
 *
 * <p>It provides all implementation for methods called on the
 * ISdvUser interface.
 *
 */
public class SdvUserImpl implements ISdvUser {

    private static final Log LOG = LogFactory.getLog(SdvUserImpl.class);

    private String credentialsTag;
    private String role;
    private String cicsTag;
    private ICredentialsUsernamePassword credentials;
    private String srrId; // When null, no recording is taking place
    private Boolean recording;

    /**
     * The SdvUserImpl constructor.
     *
     * @param credentialsTag - The string tag for the credential in the secure Galasa properties.
     * @param credentials - ICredentialsUsernamePassword object containing the actual credentials.
     * @param cicsTag - The string tag for the CICS region the user will run on.
     * @param role - The role name the user belongs to.
     */
    public SdvUserImpl(String credentialsTag, ICredentialsUsernamePassword credentials,
            String cicsTag, String role) {

        this.credentialsTag = credentialsTag;
        this.credentials = credentials;
        this.cicsTag = cicsTag;
        this.srrId = null;
        this.recording = false;
        this.role = role;
    }

    @Override
    public String getCredentialsTag() {
        return this.credentialsTag;
    }

    @Override
    public String getUsername() {
        return this.credentials.getUsername();
    }

    @Override
    public String getPassword() {
        return this.credentials.getPassword();
    }

    @Override
    public String getRole() {
        return this.role;
    }

    @Override
    public String getCicsTag() {
        return this.cicsTag;
    }

    @Override
    public void logIntoTerminal(ICicsTerminal terminal) throws SdvManagerException {
        try {
            // Are we already on CESL/N? If not go to it
            long timeout = 0;
            if (!terminal.searchText("Signon to CICS", timeout)) {
                terminal.clear().wfk();

                terminal.type("CESL").enter().wfk();
            }

            terminal.waitForTextInField(new String[] {"Userid"},
                    new String[] {"Security is not active"});
            terminal.type(this.credentials.getUsername());
            terminal.positionCursorToFieldContaining("Password");
            terminal.tab();
            terminal.type(this.credentials.getPassword());
            terminal.enter().wfk();

            waitForLoggedOnText(terminal);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Logged into CICS TS as user: " + this.credentials.getUsername());
            }

            terminal.clear().wfk();
        } catch (CicstsManagerException | Zos3270Exception e) {
            throw new SdvManagerException(
                "Could not log in via CESL on CICS region "
                + terminal.getCicsRegion().getApplid(),
                e
            );
        }

    }

    @Override
    public String getSrrId() {
        return this.srrId;
    }

    @Override
    public Boolean isRecording() {
        return this.recording;
    }

    @Override
    public void setSrrId(String srrId) {
        this.srrId = srrId;
        this.recording = true;
    }

    @Override
    public void setNotRecording() {
        this.recording = false;
    }

    private void waitForLoggedOnText(ICicsTerminal cicsTerminal) throws CicstsManagerException {

        String[] pass = {"Sign-on is complete"};
        String[] fail = {"Your password has expired. Please type your new password.",
                         "Invalid credentials entered", "userid has been revoked"};

        try {
            cicsTerminal.waitForTextInField(pass, fail);
        } catch (Exception e) {
            throw new CicstsManagerException(
                    "Unable to wait for the initial CICS screen, looking for '"
                    + String.join("', '", pass)
                    + "' on CICS Region "
                    + cicsTerminal.getCicsRegion().getApplid(),
                    e);
        }
    }

}
