/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.users.internal.routes;




import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dev.galasa.framework.api.beans.generated.FrontEndClient;
import dev.galasa.framework.api.beans.generated.UserData;

import dev.galasa.framework.spi.auth.IUser;
import dev.galasa.framework.spi.auth.IFrontEndClient;


public class BeanTransformer {

    private String baseUrl ;

    public BeanTransformer(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public List<UserData> convertAllUsersToUserBean(Collection<IUser> users) {

        List<UserData> convertedUserList = new ArrayList<>();

        if (users != null) {
            for (IUser userIn : users) {
                UserData userOut = convertUserToUserBean(userIn);
                convertedUserList.add(userOut);
            }
        }
        
        return convertedUserList;
    }

    public UserData convertUserToUserBean(IUser userIn) {
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

        return userOut ;
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