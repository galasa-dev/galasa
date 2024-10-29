/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.spi;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.ResourceUnavailableException;

public interface ICicsRegionProvisioner {

    ICicsRegionProvisioned provision(@NotNull String cicsTag, @NotNull String imageTag, @NotNull List<Annotation> annotations) throws ManagerException;

    void cicsProvisionGenerate() throws ManagerException, ResourceUnavailableException;

    void cicsProvisionBuild() throws ManagerException, ResourceUnavailableException;

    void cicsProvisionStart() throws ManagerException, ResourceUnavailableException;

    void cicsProvisionStop();

    void cicsProvisionDiscard();

}
