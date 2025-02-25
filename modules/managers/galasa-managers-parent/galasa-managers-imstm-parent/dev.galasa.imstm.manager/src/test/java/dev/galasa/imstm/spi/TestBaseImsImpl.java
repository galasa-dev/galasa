/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.spi;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.Mock;

import dev.galasa.ProductVersion;
import dev.galasa.imstm.ImstmManagerException;
import dev.galasa.zos.IZosImage;

@RunWith(MockitoJUnitRunner.class)
public class TestBaseImsImpl {
    
    private class TestImsImpl extends BaseImsImpl {

        public TestImsImpl(IImstmManagerSpi imstmManager, String imsTag, IZosImage zosImage, String applid) {
            super(imstmManager, imsTag, zosImage, applid);
        }

        @Override
        public void submitRuntimeJcl() throws ImstmManagerException {
            throw new UnsupportedOperationException("Unimplemented method 'submitRuntimeJcl'");
        }

        @Override
        public boolean isSystemStarted() throws ImstmManagerException {
            throw new UnsupportedOperationException("Unimplemented method 'isSystemStarted'");
        }

        @Override
        public ProductVersion getVersion() throws ImstmManagerException {
            throw new UnsupportedOperationException("Unimplemented method 'getVersion'");
        }

        @Override
        public void startup() throws ImstmManagerException {
            throw new UnsupportedOperationException("Unimplemented method 'startup'");
        }

        @Override
        public void shutdown() throws ImstmManagerException {
            throw new UnsupportedOperationException("Unimplemented method 'shutdown'");
        }

        @Override
        public boolean isProvisionStart() {
            throw new UnsupportedOperationException("Unimplemented method 'isProvisionStart'");
        }
        
    }

    @Mock private IImstmManagerSpi imsManager;
    private static String TAG = "tag";
    @Mock private IZosImage zos;
    private static String APPLID = "APPLID";

    private BaseImsImpl ims;

    @Before
    public void setup() {
        ims = new TestImsImpl(imsManager, TAG, zos, APPLID);
    }

    @Test
    public void testGetTag() {
        Assert.assertEquals("Wrong tag returned by getTag()", TAG, ims.getTag());
    }

    @Test
    public void testGetApplid() {
        Assert.assertEquals("Wrong applid returned by getApplid()", APPLID, ims.getApplid());
    }

    @Test
    public void testGetZosImage() {
        Assert.assertEquals("Wrong z/OS image returned by getZosImage()", zos, ims.getZosImage());
    }

    @Test
    public void testToString() {
        Assert.assertEquals("Wrong description returned by toString()", "IMS System[" + APPLID + "]", ims.toString());
    }

    @Test
    public void testGetNextTerminalId() {
        Assert.assertEquals("Wrong terminal id returned by getNextTerminalId()", APPLID + "_1", ims.getNextTerminalId());
        Assert.assertEquals("Wrong terminal id returned by getNextTerminalId()", APPLID + "_2", ims.getNextTerminalId());
    }

    @Test
    public void testGetImstmManager() {
        Assert.assertEquals("Wrong IMS Manager returned by getZosImage()", imsManager, ims.getImstmManager());
    }
}
