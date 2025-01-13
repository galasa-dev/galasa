/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.users.internal.routes;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import dev.galasa.framework.spi.auth.IInternalAuthToken;

import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.auth.spi.IAuthService;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.auth.AuthStoreException;
import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.auth.IUser;

public class UsersDeleteRoute extends BaseRoute{

    // Regex to match endpoint /users/{userDocumentId}
    protected static final String path = "\\/([a-zA-Z0-9\\-\\_]+)\\/?" ;

    private IAuthStoreService authStoreService;
    private IAuthService authService;

    private Pattern pathPattern;

    public UsersDeleteRoute(ResponseBuilder responseBuilder, Environment env,
            IAuthService authService) {
        super(responseBuilder, path);
        this.authService = authService;
        this.authStoreService = authService.getAuthStoreService();
        this.pathPattern = getPathRegex();
    }

    @Override
    public HttpServletResponse handleDeleteRequest(
        String pathInfo,
        QueryParameters queryParams,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws FrameworkException {

        logger.info("handleDeleteRequest() entered");

        String userNumber = extractUserNumberFromUrl(pathInfo);
        IUser user = authStoreService.getUser(userNumber);

        deleteUser(user);

        logger.info("handleDeleteRequest() exiting");
        return getResponseBuilder().buildResponse(request, response, HttpServletResponse.SC_NO_CONTENT);
    }

    private String extractUserNumberFromUrl(String pathInfo) throws InternalServletException{

        try {

            Matcher matcher = pathPattern.matcher(pathInfo);
            matcher.matches();

            String userNumber = matcher.group(1);
            return userNumber;

        } catch(Exception ex){
            ServletError error = new ServletError(GAL5085_FAILED_TO_GET_LOGIN_ID_FROM_URL);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND, ex);
        }

    }

    private void deleteUser(IUser user) throws AuthStoreException, InternalServletException{

        try {

            if (user == null) {
                ServletError error = new ServletError(GAL5083_ERROR_USER_NOT_FOUND);
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
            }

            String loginId = user.getLoginId();

            //Need to delete access tokens of a user if we delete the user
            List<IInternalAuthToken> tokens = authStoreService.getTokensByLoginId(loginId);
            for (IInternalAuthToken token : tokens) {
                authService.revokeToken(token.getTokenId());
            }

            logger.info("A user with the given loginId was found OK");
            authStoreService.deleteUser(user);
            logger.info("The user with the given loginId was deleted OK");

        } catch (AuthStoreException e) {
            ServletError error = new ServletError(GAL5084_FAILED_TO_DELETE_USER);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }
}
