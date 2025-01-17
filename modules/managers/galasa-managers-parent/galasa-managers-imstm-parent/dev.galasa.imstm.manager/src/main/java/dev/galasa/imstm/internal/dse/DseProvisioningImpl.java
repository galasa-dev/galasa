/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.internal.dse;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.imstm.ImstmManagerException;
import dev.galasa.imstm.internal.ImstmManagerImpl;
import dev.galasa.imstm.internal.properties.DseApplid;
import dev.galasa.imstm.spi.IImsSystemProvisioned;
import dev.galasa.imstm.spi.IImsSystemProvisioner;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;

public class DseProvisioningImpl implements IImsSystemProvisioner {
    private static final Log logger = LogFactory.getLog(DseProvisioningImpl.class);

    private final ImstmManagerImpl imstmManager;

    private HashMap<String, DseImsImpl> dseImsSystems = new HashMap<>();

    private final boolean enabled;

    public DseProvisioningImpl(ImstmManagerImpl imstmManager) {
        this.imstmManager = imstmManager;

        String provisionType = this.imstmManager.getProvisionType();
        switch (provisionType) {
            case "DSE":
            case "MIXED":
                this.enabled = true;
                break;
            default:
                this.enabled = false;
        }
    }
    
    @Override
    public void imsProvisionGenerate() throws ManagerException, ResourceUnavailableException {
    }


    @Override
    public IImsSystemProvisioned provision(@NotNull String imsTag, @NotNull String imageTag,
            @NotNull List<Annotation> annotations) throws ManagerException {
        if (!this.enabled) {
            return null;
        }
        
        String applid = DseApplid.get(imsTag);
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


        DseImsImpl imsSystem = new DseImsImpl(this.imstmManager, imsTag, zosImage, applid);

        logger.info("Provisioned DSE " + imsSystem.toString() + " on zOS Image " + imsSystem.getZosImage().getImageID() + " for tag '" + imsSystem.getTag());

        return imsSystem;
    }

    @NotNull
    public List<IImsSystemProvisioned> getSystems() {
        ArrayList<IImsSystemProvisioned> systems = new ArrayList<>();
        systems.addAll(dseImsSystems.values());
        return systems;
    }

    public IImsSystemProvisioned getTaggedSystem(String tag) {
        DseImsImpl system = this.dseImsSystems.get(tag);

        if (system != null) {
            logger.info("Provisioned DSE " + system + " for tag " + tag);
        }

        return system;
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
