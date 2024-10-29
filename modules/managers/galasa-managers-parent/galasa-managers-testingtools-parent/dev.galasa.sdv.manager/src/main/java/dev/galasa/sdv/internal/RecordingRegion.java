/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.sdv.internal;

import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.sdv.ISdvUser;
import dev.galasa.sdv.SdvManagerException;
import java.util.ArrayList;
import java.util.List;

/**
  * This class contains all required information to run SDV
  * for a single CICS Region under test.
  *
  * <p>Each region has it own maintenance terminal to allow the
  * creation, and tear down of required resources, and contains a
  * list of all SDV Users being recorded on that region.
  *
  */
class RecordingRegion {

    private ICicsTerminal maintenanceTerminal;
    private List<ISdvUser> recordingUsers = new ArrayList<>();

    public RecordingRegion(ICicsTerminal maintenanceTerminal) {
        this.maintenanceTerminal = maintenanceTerminal;
    }

    public ICicsTerminal getMaintenanceTerminal() {
        return this.maintenanceTerminal;
    }

    public Boolean addUserToRecord(ISdvUser user) throws SdvManagerException {

        // Check if user already exists in the list. This would mean
        // the user had incorrectly been provisioned to a CICS region
        // more than one by the pool code. In which case, this would
        // be a bug
        for (ISdvUser ru : recordingUsers) {
            if (user.getUsername().equals(ru.getUsername())) {
                throw new SdvManagerException("User '" + user.getUsername()
                        + "' has been allocated to more than one region in the test."
                        + " Please report this to the Galasa project.");
            }
        }

        recordingUsers.add(user);
        return true;
    }

    public List<ISdvUser> getRecordingUsers() {
        return this.recordingUsers;
    }

}
