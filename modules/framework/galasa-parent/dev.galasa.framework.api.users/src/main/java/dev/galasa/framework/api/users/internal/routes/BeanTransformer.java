/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.users.internal.routes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.beans.generated.FrontEndClient;
import dev.galasa.framework.api.beans.generated.RBACRole;
import dev.galasa.framework.api.beans.generated.UserData;
import dev.galasa.framework.api.beans.generated.UserSynthetics;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.rbac.RoleTransform;
import dev.galasa.framework.spi.auth.IUser;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.rbac.Role;
import dev.galasa.framework.spi.auth.IFrontEndClient;
import static dev.galasa.framework.api.common.ServletErrorMessage.*;

public class BeanTransformer {

    private String baseUrl ;
    private RBACService rbacService;

    public BeanTransformer(String baseUrl, RBACService rbacService) {
        this.baseUrl = baseUrl;
        this.rbacService = rbacService;
    }

    public List<UserData> convertAllUsersToUserBean(Collection<IUser> users) throws InternalServletException {

        List<UserData> convertedUserList = new ArrayList<>();

        if (users != null) {
            for (IUser userIn : users) {
                UserData userOut = convertUserToUserBean(userIn);
                convertedUserList.add(userOut);
            }
        }
        
        return convertedUserList;
    }

    public UserData convertUserToUserBean(IUser userIn) throws InternalServletException {
        UserData userOut = new UserData();

        userOut.setLoginId(userIn.getLoginId());
        userOut.setid(userIn.getUserNumber());
        userOut.seturl(calculateUrl(userIn.getUserNumber()));
        
        List<FrontEndClient> clientsOutList = new ArrayList<FrontEndClient>();

        Collection<IFrontEndClient> clientsIn = userIn.getClients();
        if( clientsIn != null) {
            for (IFrontEndClient clientIn : clientsIn) {
                FrontEndClient clientOut = convertToFrontEndClient(clientIn);
                clientsOutList.add(clientOut);
            }
        }

        FrontEndClient[] clientsOut = new FrontEndClient[clientsOutList.size()];
        clientsOut = clientsOutList.toArray(clientsOut);
        userOut.setclients(clientsOut);

        String roleId = getRoleId(userIn);
        userOut.setrole(roleId);

        UserSynthetics synthetic = createSynthetic(roleId, this.baseUrl);
        userOut.setsynthetic(synthetic);

        return userOut ;
    }

    private String getRoleId( IUser userIn ) throws InternalServletException {
        String roleId ;
        
        if (rbacService.isOwner(userIn.getLoginId())) {
            try {
                Role ownerRole = rbacService.getRoleByName("owner");
                roleId = ownerRole.getId();
            } catch( RBACException ex) {
                ServletError error = new ServletError(GAL5124_ROLE_ID_NOT_FOUND_FOR_USER);
                throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            roleId = userIn.getRoleId();
            if (roleId==null || roleId.trim().equals("")) {
                roleId = IUser.DEFAULT_ROLE_ID_WHEN_MISSING;
            }
        }

        return roleId;
    }

    UserSynthetics createSynthetic(String roleId, String baseUrl) throws InternalServletException {

        Role role;
        try {
            role = rbacService.getRoleById(roleId);
        } catch( RBACException ex) {
            ServletError error = new ServletError(GAL5124_ROLE_ID_NOT_FOUND_FOR_USER);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        RoleTransform roleXForm = new RoleTransform();
        String url ;

        url = baseUrl;
        if (!baseUrl.endsWith("/")) {
            url = url + "/";
        }
        url = url+"rbac/roles/"+role.getId();
        
        RBACRole rbacRole = roleXForm.createRoleBean(role,url);

        UserSynthetics synthetics = new UserSynthetics();
        synthetics.setrole(rbacRole);
        return synthetics;
    }

    private String calculateUrl(String userNumber) {
        String url = baseUrl ;
        if(baseUrl != null && !baseUrl.endsWith("/")) {
            url += "/";
        }
        url += "users/" + userNumber ;
        return url ;
    }

    private FrontEndClient convertToFrontEndClient(IFrontEndClient clientIn) {
        FrontEndClient clientOut = new FrontEndClient();
        clientOut.setClientName(clientIn.getClientName());        
        clientOut.setLastLogin(clientIn.getLastLogin().toString()); 
        return clientOut ;
    }
}