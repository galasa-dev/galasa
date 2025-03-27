/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.framework.api.streams.internal.routes;

import dev.galasa.framework.api.common.ProtectedRoute;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.streams.IStreamsService;
import dev.galasa.framework.spi.streams.StreamsException;

public abstract class AbstractStreamsRoute extends ProtectedRoute {

    protected IStreamsService streamsService;

    public AbstractStreamsRoute(ResponseBuilder responseBuilder, String path,
            RBACService rbacService, IStreamsService streamsService) throws StreamsException {
        super(responseBuilder, path, rbacService);
        this.streamsService = streamsService;
    }

}
