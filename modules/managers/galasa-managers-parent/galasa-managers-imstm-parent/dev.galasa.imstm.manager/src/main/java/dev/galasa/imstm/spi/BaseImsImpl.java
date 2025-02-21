/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.spi;

import dev.galasa.zos.IZosImage;

public abstract class BaseImsImpl implements IImsSystemProvisioned {

    protected final IImstmManagerSpi imstmManager;
    private final String imsTag;
    private final String applid;
    private final IZosImage zosImage;

    private int lastTerminalId;
    
    public BaseImsImpl(IImstmManagerSpi imstmManager, String imsTag, IZosImage zosImage, String applid) {
        this.imstmManager = imstmManager;
        this.imsTag = imsTag;
        this.applid = applid;
        this.zosImage = zosImage;
    }

    @Override
    public String getTag() {
        return this.imsTag;
    }

    @Override
    public String getApplid() {
        return this.applid;
    }

    @Override
    public IZosImage getZosImage() {
        return this.zosImage;
    }

    @Override
    public String toString() {
        return "IMS System[" + this.applid + "]";
    }

    @Override
    public String getNextTerminalId() {
        lastTerminalId++;
        return this.applid + "_" + Integer.toString(lastTerminalId);
    }
    
    protected  IImstmManagerSpi getImstmManager() {
        return this.imstmManager;
    }
    
}
