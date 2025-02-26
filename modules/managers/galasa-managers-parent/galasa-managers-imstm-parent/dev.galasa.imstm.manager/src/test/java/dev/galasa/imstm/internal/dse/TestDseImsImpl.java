/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.internal.dse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.ProductVersion;
import dev.galasa.imstm.ImstmManagerException;
import dev.galasa.imstm.internal.ImstmManagerImpl;
import dev.galasa.imstm.internal.properties.DseVersion;
import dev.galasa.zos.IZosImage;

@RunWith(MockitoJUnitRunner.class)
public class TestDseImsImpl {
    
    private DseImsImpl ims;

    private static ProductVersion VERSION = ProductVersion.v(1).r(2).m(3);
    private static ProductVersion DIFFERENT_VERSION = ProductVersion.v(2).r(3).m(4);

    @Mock private ImstmManagerImpl imsManager;
    private static String TAG = "tag";
    @Mock private IZosImage zos;
    private static String APPLID = "APPLID";

    @Before
    public void setup() {
        ims = new DseImsImpl(imsManager, TAG, zos, APPLID);
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
    public void testVersion() throws Exception {
        try (MockedStatic<DseVersion> dseVersion = Mockito.mockStatic(DseVersion.class)) {
            dseVersion.when(() -> DseVersion.get(TAG)).thenReturn(VERSION);
            ims = new DseImsImpl(null, TAG, null, null);
            Assert.assertEquals("Unexpected value returned from DseImsImpl.getVersion()", VERSION, ims.getVersion());
            
            // Subsequent gets come from a saved copy
            dseVersion.when(() -> DseVersion.get(TAG)).thenReturn(DIFFERENT_VERSION);
            Assert.assertEquals("Unexpected value returned from DseImsImpl.getVersion()", VERSION, ims.getVersion());
        }
    }

    @Test
    public void testIsProvisionStart() throws Exception {
        ims = new DseImsImpl(null, null, null, null);
        Assert.assertTrue("Unexpected value returned from DseImsImpl.isProvisionStart()", ims.isProvisionStart());
    }

    @Test
    public void testStartup() throws Exception {
        ims = new DseImsImpl(null, null, null, null);
        String expectedMessage = "Unable to startup DSE IMS TM systems";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
        	ims.startup();
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testShutdown() throws Exception {
        ims = new DseImsImpl(null, null, null, null);
        String expectedMessage = "Unable to shutdown DSE IMS TM systems";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
        	ims.shutdown();
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
}
