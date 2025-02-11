/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.internal.dse;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.ProductVersion;
import dev.galasa.imstm.ImstmManagerException;
import dev.galasa.imstm.internal.properties.DseVersion;

@RunWith(MockitoJUnitRunner.class)
public class TestDseImsImpl {
    
    private DseImsImpl dii;

    private static String TAG = "tag";
    private static ProductVersion VERSION = ProductVersion.v(1).r(2).m(3);
    private static ProductVersion DIFFERENT_VERSION = ProductVersion.v(2).r(3).m(4);

    @Test
    public void testVersion() throws Exception {
        try (MockedStatic<DseVersion> dseVersion = Mockito.mockStatic(DseVersion.class)) {
            dseVersion.when(() -> DseVersion.get(TAG)).thenReturn(VERSION);
            dii = new DseImsImpl(null, TAG, null, null);
            Assert.assertEquals("Unexpected value returned from DseImsImpl.getVersion()", VERSION, dii.getVersion());
            
            // Subsequent gets come from a saved copy
            dseVersion.when(() -> DseVersion.get(TAG)).thenReturn(DIFFERENT_VERSION);
            Assert.assertEquals("Unexpected value returned from DseImsImpl.getVersion()", VERSION, dii.getVersion());
        }
    }

    @Test
    public void testIsProvisionStart() throws Exception {
        dii = new DseImsImpl(null, null, null, null);
        Assert.assertTrue("Unexpected value returned from DseImsImpl.isProvisionStart()", dii.isProvisionStart());
    }

    @Test
    public void testStartup() throws Exception {
        dii = new DseImsImpl(null, null, null, null);
        String expectedMessage = "Unable to startup DSE IMS TM systems";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
        	dii.startup();
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testShutdown() throws Exception {
        dii = new DseImsImpl(null, null, null, null);
        String expectedMessage = "Unable to shutdown DSE IMS TM systems";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
        	dii.shutdown();
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testSubmitRuntimeJcl() throws Exception {
        dii = new DseImsImpl(null, null, null, null);
        String expectedMessage = "Unable to submit DSE IMS TM systems";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
        	dii.submitRuntimeJcl();
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testHasRegionStarted() throws Exception {
        dii = new DseImsImpl(null, null, null, null);
        String expectedMessage = "Unable to check DSE IMS TM systems has started";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
        	dii.hasRegionStarted();
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
}
