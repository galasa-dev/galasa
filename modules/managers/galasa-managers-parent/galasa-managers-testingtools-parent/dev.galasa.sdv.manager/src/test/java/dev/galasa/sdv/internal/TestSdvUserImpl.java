/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.sdv.internal;

import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestSdvUserImpl {

    SdvUserImpl ru;

    private static final String TEST_SRR_ID = "1234";

    @BeforeEach
    void beforeEach() throws CredentialsException {
        ICredentialsUsernamePassword credentials =
                new CredentialsUsernamePassword(null, "user1", "password1");
        ru = new SdvUserImpl("CREDS1", credentials, "CICS1", "TELLER");
    }

    @Test
    void testGetCredentials() {
        Assertions.assertEquals("CREDS1", ru.getCredentialsTag());

        Assertions.assertEquals("user1", ru.getUsername());
        Assertions.assertEquals("password1", ru.getPassword());
    }

    @Test
    void testGetRole() {
        Assertions.assertEquals("TELLER", ru.getRole());
    }

    @Test
    void testGetSrrIdAndIsRecordingWhenNotSet() {
        Assertions.assertEquals(null, ru.getSrrId());
        Assertions.assertEquals(false, ru.isRecording());
    }

    @Test
    void testGetSrrIdAndIsRecordingWhenSet() {

        ru.setSrrId(TEST_SRR_ID);

        Assertions.assertEquals(TEST_SRR_ID, ru.getSrrId());
        Assertions.assertEquals(true, ru.isRecording());
    }

    @Test
    void testSetNotRecording() {

        ru.setSrrId(TEST_SRR_ID);
        ru.setNotRecording();

        Assertions.assertEquals(TEST_SRR_ID, ru.getSrrId());
        Assertions.assertEquals(false, ru.isRecording());
    }

    @Test
    void testgetCicsTag() {
        Assertions.assertEquals("CICS1", ru.getCicsTag());
    }
}
