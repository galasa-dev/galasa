/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.internal.dse;

import java.lang.annotation.Annotation;
import java.text.MessageFormat;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.imstm.ImstmManagerException;
import dev.galasa.imstm.internal.ImstmManagerImpl;
import dev.galasa.imstm.internal.ImstmProperties;
import dev.galasa.imstm.IImsSystem;
import dev.galasa.imstm.spi.IImsSystemProvisioner;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;

public class DseProvisioningImpl implements IImsSystemProvisioner {
    private static final Log logger = LogFactory.getLog(DseProvisioningImpl.class);

    private final ImstmManagerImpl imstmManager;
    private final ImstmProperties properties;

    private final boolean isEnabled;

    public DseProvisioningImpl(ImstmManagerImpl imstmManager, ImstmProperties properties) {
        this.imstmManager = imstmManager;
        this.properties = properties;

        String provisionType = this.imstmManager.getProvisionType();
        switch (provisionType) {
            case "DSE":
            case "MIXED":
                this.isEnabled = true;
                break;
            default:
                this.isEnabled = false;
        }
    }
    
    @Override
    public void imsProvisionGenerate() throws ManagerException, ResourceUnavailableException {
    }


    @Override
    public IImsSystem provision(@NotNull String imsTag, @NotNull String imageTag,
            @NotNull List<Annotation> annotations) throws ManagerException {
        if (!this.isEnabled) {
            return null;
        }
        
        String applid = properties.getDseApplid(imsTag);
        if (applid == null) {
            logger.warn("Unable to get APPLID for IMS system tagged " + imsTag);
            return null;
        }
        
        IZosImage zosImage = null;
        try {
            zosImage = imstmManager.getZosManager().getImageForTag(imageTag);
        } catch (ZosManagerException e) {
            throw new ImstmManagerException("Unable to locate zOS Image tagged " + imageTag, e);
        }


        DseImsImpl imsSystem = new DseImsImpl(this.imstmManager, properties, imsTag, zosImage, applid);

        logger.info(MessageFormat.format("Provisioned DSE {0} on zOS Image {1} for tag ''{2}''", 
                                            imsSystem.toString(), 
                                            imsSystem.getZosImage().getImageID(), 
                                            imsSystem.getTag()));

        return imsSystem;
    }

    @Override
    public void imsProvisionBuild() throws ManagerException, ResourceUnavailableException {
    }

    @Override
    public void imsProvisionStart() throws ManagerException, ResourceUnavailableException {
    }

    @Override
    public void imsProvisionStop() {
    }

    @Override
    public void imsProvisionDiscard() {
    }


}
