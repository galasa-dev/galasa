/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.auth.couchdb.internal;

import java.util.Collection;
import java.util.List;

import javax.validation.constraints.NotNull;

import java.util.ArrayList;
import static dev.galasa.extensions.common.Errors.*;

import dev.galasa.auth.couchdb.internal.beans.FrontEndClient;
import dev.galasa.auth.couchdb.internal.beans.UserDoc;
import dev.galasa.framework.spi.auth.AuthStoreException;
import dev.galasa.framework.spi.auth.IFrontEndClient;
import dev.galasa.framework.spi.auth.IUser;
import dev.galasa.framework.spi.utils.GalasaGson;

/**
 * A wrapper around the UserDoc bean which gets used when talking to couchdb,
 * but one which implements the IUser interface.
 */
public class UserImpl implements IUser {

    private UserDoc userDocBean ;

    UserImpl() {
        this.userDocBean = new UserDoc();
    }

    UserImpl(UserDoc userDocBean) {
        this.userDocBean = userDocBean ;
    }

    public UserImpl(IUser user) {

        // We don't trust that the users' clients which are passed in are going to serialise
        // to the correct json format. So convert them into our bean which will.
        List<FrontEndClient> trustedClients = new ArrayList<FrontEndClient>();

        Collection<IFrontEndClient> clientsIn = user.getClients();
        if( clientsIn != null) {
            for( IFrontEndClient untrustedClient : clientsIn) {
                trustedClients.add( new FrontEndClient(untrustedClient));
            }
        }

        this.userDocBean = new UserDoc( user.getLoginId() , trustedClients, user.getRoleId());
        this.userDocBean.setVersion( user.getVersion() );
        this.userDocBean.setUserNumber( user.getUserNumber() );
    }

    public String toJson( GalasaGson gson) {
        return gson.toJson(userDocBean);
    }

    @Override
    public @NotNull String getRoleId() {
        String roleId = this.userDocBean.getRoleId();

        // Make sure that a default roleId is returned if none is actually set.
        if (roleId == null || roleId.trim().equals("")) {
            roleId = IUser.DEFAULT_ROLE_ID_WHEN_MISSING ;
        }
        return roleId ;
    }

    public void setRoleId(String newRoleId) {
        this.userDocBean.setRoleId(newRoleId);
    }

    @Override
    public String getUserNumber() {
        return this.userDocBean.getUserNumber();
    }

    public void setUserNumber(String newUserNumber) {
        this.userDocBean.setUserNumber(newUserNumber);
    }

    @Override
    public String getVersion() {
        return this.userDocBean.getVersion();
    }

    public void setVersion(String newVersion) {
        this.userDocBean.setVersion(newVersion);
    }

    @Override
    public String getLoginId() {
        return this.userDocBean.getLoginId();
    }

    public void setLoginId(String newLoginId) {
        this.userDocBean.setLoginId(newLoginId);
    }

    @Override
    public Collection<IFrontEndClient> getClients() {
        List<IFrontEndClient> results = new ArrayList<>();
        for ( FrontEndClient beanClient : userDocBean.getClients()) {
            results.add(beanClient);
        }
        return results;
    }

    @Override
    public IFrontEndClient getClient(String clientName) {
        IFrontEndClient match = null; 
        if (clientName != null) {
            for (FrontEndClient frontEndClient : userDocBean.getClients()) {
                if(clientName.equals(frontEndClient.getClientName())){
                    match = frontEndClient;
                    break;
                }
            }
        }
        return match;
    }

    @Override
    public void addClient(IFrontEndClient clientInput) {
        // We don't trust that the interface passed in will serialize to the correct json format that couchdb uses.
        // So create our own bean and copy the contents over.
        FrontEndClient frontEndClient = new FrontEndClient(clientInput);
        userDocBean.getClients().add(frontEndClient);
    }


    public void validate() throws AuthStoreException {
        if (getUserNumber() == null) {
            String errorMessage = ERROR_FAILED_TO_UPDATE_USER_DOCUMENT_INPUT_INVALID_NULL_USER_NUMBER.getMessage();
            throw new AuthStoreException(errorMessage);
        } 

        if (getVersion() == null) {
            String errorMessage = ERROR_FAILED_TO_UPDATE_USER_DOCUMENT_INPUT_INVALID_NULL_USER_VERSION.getMessage();
            throw new AuthStoreException(errorMessage);
        } 
    }

}