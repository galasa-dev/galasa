/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.sdv;

import dev.galasa.cicsts.ICicsTerminal;

/**
 * This class provides the interface for
 * SDVUserImpl.
 *
 */
public interface ISdvUser {

    String getCredentialsTag();

    String getUsername();

    String getPassword();

    String getRole();

    String getCicsTag();

    String getSrrId();

    Boolean isRecording();

    void setSrrId(String srrId);

    void setNotRecording();

    void logIntoTerminal(ICicsTerminal terminal) throws SdvManagerException;

}
