/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.internal.dse;

import dev.galasa.imstm.IImsSystem;
import dev.galasa.imstm.ImstmManagerException;
import dev.galasa.imstm.internal.ImstmManagerImpl;
import dev.galasa.imstm.internal.ImstmProperties;
import dev.galasa.imstm.spi.IImstmManagerSpi;
import dev.galasa.ProductVersion;
import dev.galasa.zos.IZosImage;

public class DseImsImpl implements IImsSystem {

    private ProductVersion version;
    protected final IImstmManagerSpi imstmManager;
    private final ImstmProperties properties;
    private final String imsTag;
    private final String applid;
    private final IZosImage zosImage;

    public DseImsImpl(ImstmManagerImpl imstmManager, ImstmProperties properties, String imsTag, IZosImage image, String applid) {
        this.imstmManager = imstmManager;
        this.properties = properties;
        this.imsTag = imsTag;
        this.applid = applid;
        this.zosImage = image;
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
    public ProductVersion getVersion() throws ImstmManagerException {
        if (this.version == null) {
            this.version = properties.getDseVersion(this.getTag());
        }

        return this.version;
    }

	@Override
    public boolean isProvisionStart() {
        return true;  // DSE systems are assumed to be started before the test runs
    }

    @Override
    public void startup() throws ImstmManagerException {
        throw new ImstmManagerException("Unable to startup DSE IMS TM systems");
        
    }

    @Override
    public void shutdown() throws ImstmManagerException {
        throw new ImstmManagerException("Unable to shutdown DSE IMS TM systems");
    }
}
