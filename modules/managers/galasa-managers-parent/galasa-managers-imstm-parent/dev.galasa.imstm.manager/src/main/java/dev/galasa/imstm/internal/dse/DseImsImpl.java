/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.internal.dse;

import dev.galasa.imstm.ImstmManagerException;
import dev.galasa.imstm.internal.ImstmManagerImpl;
import dev.galasa.imstm.internal.properties.DseVersion;
import dev.galasa.imstm.spi.BaseImsImpl;
import dev.galasa.ProductVersion;
import dev.galasa.zos.IZosImage;

public class DseImsImpl extends BaseImsImpl {

    private ProductVersion version;

    public DseImsImpl(ImstmManagerImpl imstmManager, String imsTag, IZosImage image, String applid)
            throws ImstmManagerException {
        super(imstmManager, imsTag, image, applid);
    }

    @Override
    public ProductVersion getVersion() throws ImstmManagerException {
        if (this.version == null) {
            this.version = DseVersion.get(this.getTag());
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

    @Override
    public void submitRuntimeJcl() throws ImstmManagerException {
        throw new ImstmManagerException("Unable to submit DSE IMS TM systems");
    }

    @Override
    public boolean hasRegionStarted() throws ImstmManagerException {
        throw new ImstmManagerException("Unable to check DSE IMS TM systems has started");
    }
}
