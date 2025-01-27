/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.auth;
import java.util.Collection;

import javax.validation.constraints.NotNull;


public interface IUser {

    // If the roleId field is missing, then we can default it to a value which 
    // is the roleId of the 'admin' role.
    // ie: Users with user records prior to the introduction of RBAC had admin rights,
    // so they should continue to have them.
    public static final String DEFAULT_ROLE_ID_WHEN_MISSING = "2";
    
    String getUserNumber(); 

    @NotNull String getRoleId();

    void setRoleId( @NotNull String newRoleId);

    String getVersion();

    String getLoginId();

    Collection<IFrontEndClient> getClients();

    IFrontEndClient getClient(String clientName);

    void addClient(IFrontEndClient client);

}
