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
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.ProductVersion;
import dev.galasa.imstm.ImstmManagerException;
import dev.galasa.imstm.internal.ImstmManagerImpl;
import dev.galasa.imstm.internal.ImstmProperties;
import dev.galasa.zos.IZosImage;

@RunWith(MockitoJUnitRunner.class)
public class TestDseImsImpl {
    
    private DseImsImpl ims;

    private static ProductVersion VERSION = ProductVersion.v(1).r(2).m(3);

    @Mock private ImstmManagerImpl imsManager;
    private static String TAG = "tag";
    @Mock private IZosImage zos;
    private static String APPLID = "APPLID";
    @Mock private ImstmProperties properties;

    @Before
    public void setup() throws Exception {
        ims = new DseImsImpl(imsManager, properties, TAG, zos, APPLID);
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
        ims = new DseImsImpl(null, properties, TAG, null, null);
        Mockito.when(properties.getDseVersion(TAG)).thenReturn(VERSION);
        Assert.assertEquals("Unexpected value returned from DseImsImpl.getVersion()", VERSION, ims.getVersion());
        
        // Subsequent gets come from a saved copy
        Mockito.verify(properties).getDseVersion(TAG);
        Assert.assertEquals("Unexpected value returned from DseImsImpl.getVersion()", VERSION, ims.getVersion());
        Mockito.verifyNoMoreInteractions(properties);
    }

    @Test
    public void testIsProvisionStart() throws Exception {
        ims = new DseImsImpl(null, null, null, null, null);
        Assert.assertTrue("Unexpected value returned from DseImsImpl.isProvisionStart()", ims.isProvisionStart());
    }

    @Test
    public void testStartup() throws Exception {
        ims = new DseImsImpl(null, null,null, null, null);
        String expectedMessage = "Unable to startup DSE IMS TM systems";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
        	ims.startup();
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testShutdown() throws Exception {
        ims = new DseImsImpl(null, null, null, null, null);
        String expectedMessage = "Unable to shutdown DSE IMS TM systems";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
        	ims.shutdown();
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
}
