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
import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.users.UsersServlet;
import dev.galasa.framework.api.common.JwtWrapper;

import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.auth.AuthStoreException;
import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.auth.IUser;

public class UsersRoute extends BaseRoute {

    // Regex to match endpoint /users and /users/
    private static final String path = "\\/?";

    public static final String QUERY_PARAMETER_LOGIN_ID_VALUE_MYSELF = "me";

    private Environment env;
    private IAuthStoreService authStoreService;
    private BeanTransformer beanTransformer ;

    public UsersRoute(ResponseBuilder responseBuilder, Environment env,
            IAuthStoreService authStoreService) {
        super(responseBuilder, path);
        this.env = env;
        this.authStoreService = authStoreService;

        String baseServletUrl = env.getenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL);

        this.beanTransformer = new BeanTransformer(baseServletUrl);
    }

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, FrameworkException {

        logger.info("UserRoute: handleGetRequest() entered.");

        List<UserData> usersList = new ArrayList<>();
        String payloadContent = "[]";

        String loginId = queryParams.getSingleString(UsersServlet.QUERY_PARAM_LOGIN_ID, null);

        if (loginId != null) {
            usersList = getUserByLoginIdList(request, loginId);
        }
        else{
            Collection<IUser> users = authStoreService.getAllUsers();
            usersList = beanTransformer.convertAllUsersToUserBean(users);
        }

        if (!usersList.isEmpty()) {
            payloadContent = gson.toJson(usersList);
        }

        return getResponseBuilder().buildResponse(
                request, response, "application/json", payloadContent, HttpServletResponse.SC_OK);
    }

    private List<UserData> getUserByLoginIdList(HttpServletRequest request, String loginId)
            throws InternalServletException, AuthStoreException {

        List<UserData> usersList = new ArrayList<>();
        List<IUser> userDocs = new ArrayList<>();

        JwtWrapper jwtWrapper = new JwtWrapper(request, env);

        if (loginId.equals("me")) {
            loginId = jwtWrapper.getUsername();
        }

        IUser currentUser = authStoreService.getUserByLoginId(loginId);

        if (currentUser != null) {
            userDocs.add(currentUser);
            usersList = beanTransformer.convertAllUsersToUserBean(userDocs);
        }
    
        return usersList;
    }

    

}
