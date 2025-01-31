/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.users.internal.routes;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import dev.galasa.framework.spi.auth.IInternalAuthToken;
import dev.galasa.framework.api.beans.generated.UserData;
import dev.galasa.framework.api.beans.generated.UserUpdateData;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.auth.spi.IAuthService;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.auth.AuthStoreException;
import dev.galasa.framework.spi.auth.IUser;
import dev.galasa.framework.spi.rbac.BuiltInAction;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.rbac.RBACService;

/**
 * Handles REST calls directed at a specific user record.
 * 
 * The user record is addressed using it's user-id (a.k.a: user-number)
 * 
 * The URL takes the form of ../users/{user-number}
 * 
 * THe user number can then be used to look up this specific user in the auth store.
 */
public class UserRoute extends AbstractUsersRoute {

    // Regex to match endpoint /users/{userDocumentId}
    protected static final String path = "\\/([a-zA-Z0-9\\-\\_]+)\\/?" ;

    protected Pattern pathPattern;

    private BeanTransformer beanTransformer ;

    private UserUpdateRequestValidator updateRequestValidator = new UserUpdateRequestValidator();

    public UserRoute(ResponseBuilder responseBuilder, Environment env,
            IAuthService authService, RBACService rbacService) {
        super(responseBuilder, path, authService , env, rbacService);
        this.pathPattern = getPathRegex();
        this.beanTransformer = new BeanTransformer(baseServletUrl, rbacService);
    }

    @Override
    public HttpServletResponse handleDeleteRequest(
        String pathInfo,
        HttpRequestContext requestContext,
        HttpServletResponse response
    ) throws FrameworkException {

        logger.info("handleDeleteRequest() entered");
        HttpServletRequest request = requestContext.getRequest();
        String requestingUserLoginId = requestContext.getUsername();

        IUser user = getUser(pathInfo);

        deleteUser(user, requestingUserLoginId);

        logger.info("handleDeleteRequest() exiting");
        return getResponseBuilder().buildResponse(request, response, HttpServletResponse.SC_NO_CONTENT);
    }

    @Override
    public HttpServletResponse handlePutRequest(
        String pathInfo,
        HttpRequestContext requestContext,
        HttpServletResponse response
    ) throws FrameworkException, IOException {

        validateActionPermitted(BuiltInAction.USER_EDIT_OTHER, requestContext.getUsername());
        logger.info("handlePutRequest() entered");

        HttpServletRequest request = requestContext.getRequest();

        IUser originalUser = getUser(pathInfo);

        checkRequestHasContent(request);

        // Parse the Java payload
        UserUpdateData updatePayload = parseRequestBody(request, UserUpdateData.class);

        updateRequestValidator.validateUpdateRequest(updatePayload);

        IUser updatedUser = updateUser(originalUser, updatePayload);

        UserData updatedUserBean = beanTransformer.convertUserToUserBean(updatedUser);

        String payloadContent = gson.toJson(updatedUserBean);

        HttpServletResponse filledResponse = getResponseBuilder().buildResponse(
            request, response, "application/json", payloadContent, HttpServletResponse.SC_OK); 

        logger.info("handlePutRequest() exiting");
        return filledResponse;
    }

    @Override
    public HttpServletResponse handleGetRequest(
        String pathInfo,
        QueryParameters queryParams,
        HttpRequestContext requestContext,
        HttpServletResponse response
    ) throws FrameworkException, IOException {

        logger.info("handleGetRequest() entered");
        HttpServletRequest request = requestContext.getRequest();

        IUser userRecordFound = getUser(pathInfo);

        UserData userBean = beanTransformer.convertUserToUserBean(userRecordFound);

        String payloadContent = gson.toJson(userBean);

        HttpServletResponse filledResponse = getResponseBuilder().buildResponse(
            request, response, "application/json", payloadContent, HttpServletResponse.SC_OK); 

        logger.info("handleGetRequest() exiting");
        return filledResponse;
    }


    private IUser updateUser(IUser user , UserUpdateData updatePayload) throws AuthStoreException, InternalServletException, RBACException{

        boolean isStoreUpdateRequired = false ;

        // Only apply the update to the user if it's different to the original values.
        String desiredRoleId = updatePayload.getrole();
        if (desiredRoleId != null ) {
            if (! desiredRoleId.equals(user.getRoleId() )) {
                user.setRoleId(desiredRoleId);
                isStoreUpdateRequired = true;
            }
        }

        if (isStoreUpdateRequired) {
            authStoreService.updateUser(user);
            rbacService.invalidateUser(user.getLoginId());
        }

        return user;
    }


    void checkRequestorHasPermissionToDeleteUserRecord( String loginIdToBeDeleted, String loginIdOfRequestor ) throws InternalServletException {
        if (!loginIdToBeDeleted.equals(loginIdOfRequestor)) {
            // The user is trying to delete someone else's user record.
            // This is only allowed if you have permissions.
            validateActionPermitted(BuiltInAction.USER_EDIT_OTHER , loginIdOfRequestor);
        } else {
            // The user is trying to delete their own record. This is never allowed.
            // Enforcing this makes it less likely that the last admin on the sysyem will delete themselves.
            ServletError error = new ServletError(GAL5088_FORBIDDEN_USER_DELETE_THEMSELVES);
            throw new InternalServletException(error, HttpServletResponse.SC_FORBIDDEN);
        }
    }
    
    private void deleteUser(IUser user, String requestingUserLoginId ) throws InternalServletException{

        try {
            String loginId = user.getLoginId();

            checkRequestorHasPermissionToDeleteUserRecord(loginId, requestingUserLoginId);

            //Need to delete access tokens of a user if we delete the user
            List<IInternalAuthToken> tokens = authStoreService.getTokensByLoginId(loginId);
            for (IInternalAuthToken token : tokens) {
                authService.revokeToken(token.getTokenId(),requestingUserLoginId);
            }

            authStoreService.deleteUser(user);
            rbacService.invalidateUser(loginId);

            logger.info("The user with the given loginId was deleted OK");

        } catch (AuthStoreException | RBACException e) {
            ServletError error = new ServletError(GAL5084_FAILED_TO_DELETE_USER);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    private IUser getUser(String pathInfo) throws InternalServletException, AuthStoreException {
        String userNumber = getUserNumber(pathInfo);
        IUser user = authStoreService.getUser(userNumber);

        if (user == null) {
            ServletError error = new ServletError(GAL5083_ERROR_USER_NOT_FOUND);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }

        logger.info("A user with the given loginId was found OK");

        return user;
    }

    private String getUserNumber(String urlPath) throws InternalServletException {
        UsersUrlParameterExtractor parser = new UsersUrlParameterExtractor(pathPattern);
        return parser.getUserNumber(urlPath);
    }
}
