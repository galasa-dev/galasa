/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.http.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.http.IHttpClient;
import dev.galasa.http.IHttpManager;

public interface IHttpManagerSpi extends IHttpManager {

    @NotNull
    IHttpClient newHttpClient();

    @NotNull
    IHttpClient newHttpClient(int timeout);

}
