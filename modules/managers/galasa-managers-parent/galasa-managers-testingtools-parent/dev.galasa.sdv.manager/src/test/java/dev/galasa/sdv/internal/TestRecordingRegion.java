/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.sdv.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;
import dev.galasa.sdv.ISdvUser;
import dev.galasa.sdv.SdvManagerException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestRecordingRegion {

    RecordingRegion rr;
    ICredentialsUsernamePassword credentials;

    @BeforeEach
    void beforeEach() throws CredentialsException  {
        rr = new RecordingRegion(null);

        credentials =
                new CredentialsUsernamePassword(null, "user1", "password1");
    }

    @Test
    void testAddingNewUserToRegion() throws SdvManagerException, CredentialsException {
        // No users to start
        List<ISdvUser> beforeList = rr.getRecordingUsers();
        assertThat(beforeList).isEmpty();

        // Add new user
        ISdvUser newUser = new SdvUserImpl("creds1", credentials, "cics1", "TELLER");
        rr.addUserToRecord(newUser);

        // Number of users increment, and user is the one created
        List<ISdvUser> afterList = rr.getRecordingUsers();
        assertThat(afterList).hasSize(1);
        assertThat(afterList.get(0)).isEqualTo(newUser);
    }

    @Test
    void testAddingExistingUserToRegion() throws SdvManagerException, CredentialsException {
        // Add existing user
        ISdvUser existingUser = new SdvUserImpl("creds1", credentials, "cics1", "TELLER");
        rr.addUserToRecord(existingUser);
        List<ISdvUser> beforeList = rr.getRecordingUsers();
        assertThat(beforeList).hasSize(1);
        assertThat(beforeList.get(0)).isEqualTo(existingUser);

        // Create new user with same user name
        ISdvUser newUser = new SdvUserImpl("creds2", credentials, "cics1", "ADMIN");
        Throwable exception = catchThrowableOfType(() -> rr.addUserToRecord(newUser),
            SdvManagerException.class);

        assertThat(exception.getMessage())
            .contains("User 'user1' has been allocated to more than one region in the test.");
    }
}
