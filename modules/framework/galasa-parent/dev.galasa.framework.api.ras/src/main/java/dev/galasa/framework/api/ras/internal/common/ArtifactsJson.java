/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonArray;

import dev.galasa.framework.api.ras.internal.routes.RunArtifactsRoute;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.utils.GalasaGson;

public class ArtifactsJson implements IRunRootArtifact {

    private RunArtifactsRoute runsRoute;
    static final GalasaGson gson = new GalasaGson();

    public ArtifactsJson(RunArtifactsRoute runsRoute) {
        this.runsRoute = runsRoute;
    }

    @Override
    public byte[] getContent(IRunResult run) throws ResultArchiveStoreException, IOException {
        JsonArray artifactsJson = runsRoute.getArtifacts(run);
        return gson.toJson(artifactsJson).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public String getPathName() {
        return "/artifacts.json";
    }
}
