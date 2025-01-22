/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.users.internal.routes;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import dev.galasa.framework.api.beans.generated.UserData;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.SupportedQueryParameterNames;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.users.UsersServlet;
import dev.galasa.framework.auth.spi.IAuthService;

import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.auth.AuthStoreException;
import dev.galasa.framework.spi.auth.IUser;
import dev.galasa.framework.spi.rbac.RBACService;

public class UsersRoute extends AbstractUsersRoute {

    // Regex to match endpoint /users and /users/
    private static final String path = "\\/?";

    public static final SupportedQueryParameterNames SUPPORTED_QUERY_PARAMETER_NAMES = new SupportedQueryParameterNames(
        UsersServlet.QUERY_PARAM_LOGIN_ID
    );

    private BeanTransformer beanTransformer ;

    public UsersRoute(ResponseBuilder responseBuilder, Environment env,
            IAuthService authService, RBACService rbacService) {
        super(responseBuilder,path, authService, env , rbacService );

        this.beanTransformer = new BeanTransformer(baseServletUrl, rbacService);
    }

    @Override
    public SupportedQueryParameterNames getSupportedQueryParameterNames() {
        return SUPPORTED_QUERY_PARAMETER_NAMES;
    }

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,
            HttpRequestContext requestContext, HttpServletResponse response)
            throws ServletException, IOException, FrameworkException {

        logger.info("UserRoute: handleGetRequest() entered.");
        HttpServletRequest request = requestContext.getRequest();

        List<UserData> usersList = new ArrayList<UserData>();
        String payloadContent ;

        String loginId = queryParams.getSingleString(UsersServlet.QUERY_PARAM_LOGIN_ID, null);

        if (loginId != null) {
            usersList = getUserByLoginIdList(requestContext.getUsername(), loginId);
        }
        else{
            Collection<IUser> users = authStoreService.getAllUsers();
            usersList = beanTransformer.convertAllUsersToUserBean(users);
        }

        payloadContent = gson.toJson(usersList);

        return getResponseBuilder().buildResponse(
                request, response, "application/json", payloadContent, HttpServletResponse.SC_OK);
    }

    private List<UserData> getUserByLoginIdList(String requestorUsername, String loginId)
            throws InternalServletException, AuthStoreException {

        List<UserData> usersList = new ArrayList<>();
        List<IUser> userDocs = new ArrayList<>();

        if (loginId.equals(UsersServlet.QUERY_PARAMETER_LOGIN_ID_VALUE_MYSELF)) {
            loginId = requestorUsername;
        }

        IUser currentUser = authStoreService.getUserByLoginId(loginId);

        if (currentUser != null) {
            userDocs.add(currentUser);
            usersList = beanTransformer.convertAllUsersToUserBean(userDocs);
        }
    
        return usersList;
    }

}
