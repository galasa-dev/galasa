/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.teststructure.TestStructure;
import dev.galasa.framework.spi.utils.GalasaGson;

public class StructureJsonArtifact implements IRunRootArtifact {

    static final GalasaGson gson = new GalasaGson();

    @Override
    public byte[] getContent(IRunResult run) throws ResultArchiveStoreException, IOException {
        TestStructure testStructure = run.getTestStructure();
        if (testStructure != null) {
            return gson.toJson(testStructure).getBytes(StandardCharsets.UTF_8);
        }
        return null;
    }

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public String getPathName() {
        return "/structure.json";
    }
}