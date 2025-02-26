/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.spi;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.imstm.IImsSystem;

public interface IImsSystemProvisioner {

    IImsSystem provision(@NotNull String imsTag, @NotNull String imageTag, @NotNull List<Annotation> annotations) throws ManagerException;

    void imsProvisionGenerate() throws ManagerException, ResourceUnavailableException;

    void imsProvisionBuild() throws ManagerException, ResourceUnavailableException;

    void imsProvisionStart() throws ManagerException, ResourceUnavailableException;

    void imsProvisionStop();

    void imsProvisionDiscard();

}
