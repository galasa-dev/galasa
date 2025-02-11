/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.internal.dse;

import static org.mockito.Mockito.mockConstruction;

import java.nio.charset.Charset;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.ICredentials;
import dev.galasa.imstm.ImstmManagerException;
import dev.galasa.imstm.internal.ImstmManagerImpl;
import dev.galasa.imstm.internal.properties.DseApplid;
import dev.galasa.imstm.spi.IImsSystemProvisioned;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.spi.IZosManagerSpi;

@RunWith(MockitoJUnitRunner.class)
public class TestDseProvisioningImpl {
    
    private static DseProvisioningImpl dpi;
    @Mock
    private static ImstmManagerImpl manager;
    @Mock
    private static IZosManagerSpi zosManager;

    private static String TAG = "tag";
    private static String TAG2 = "tag2";
    private static String APPLID = "APPLID";
    private static IZosImage zosImage = new IZosImage() {
        public String getImageID() {return "IMAGE";}
        public String getSysname() {return null;}
        public String getVtamLogonString(String applid) {return null;}
        public String getSysplexID() {return null;}
        public String getClusterID() {return null;}
        public Charset getCodePage() {return null;}
        public String getDefaultHostname() throws ZosManagerException {return null;}
        public ICredentials getDefaultCredentials() throws ZosManagerException {return null;}
        public IIpHost getIpHost() {return null;}
        public String getHome() throws ZosManagerException {return null;}
        public String getRunTemporaryUNIXPath() throws ZosManagerException {return null;}
        public String getJavaHome() throws ZosManagerException {return null;}
        public String getLibertyInstallDir() throws ZosManagerException {return null;}
        public String getZosConnectInstallDir() throws ZosManagerException {return null;}
    };

    @Before
    public void setup() throws Exception{
        Mockito.when(manager.getProvisionType()).thenReturn("DSE");
        Mockito.when(manager.getZosManager()).thenReturn(zosManager);
        Mockito.when(zosManager.getImageForTag(TAG2)).thenReturn(zosImage);
    }

    @Test
    public void testProvisionNotEnabled() throws Exception {
        Mockito.when(manager.getProvisionType()).thenReturn("NOT_DSE");
        dpi = new DseProvisioningImpl(manager);
        Assert.assertNull("Unexpected value returned from DseProvisioningImpl.provision()", dpi.provision(TAG, TAG2, null));
    }
    
    @Test
    public void testProvisionNoApplid() throws Exception {
        try (MockedStatic<DseApplid> dseApplid = Mockito.mockStatic(DseApplid.class)) {
            dseApplid.when(() -> DseApplid.get(TAG)).thenReturn(null);
            dpi = new DseProvisioningImpl(manager);
            Assert.assertNull("Unexpected value returned from DseProvisioningImpl.provision()", dpi.provision(TAG, TAG2, null));
        }
    }

    @Test
    public void testProvisionNoZosImage() throws Exception {
        Mockito.when(zosManager.getImageForTag(TAG2)).thenThrow(new ZosManagerException());
        try (MockedStatic<DseApplid> dseApplid = Mockito.mockStatic(DseApplid.class)) {
            dseApplid.when(() -> DseApplid.get(TAG)).thenReturn("APPLID");
            dpi = new DseProvisioningImpl(manager);
            String expectedMessage = "Unable to locate zOS Image tagged " + TAG2;
            ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
        	    dpi.provision(TAG, TAG2, null);
            });
            Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
        }
    }

    @Test
    public void testProvision() throws Exception {
        try (MockedStatic<DseApplid> dseApplid = Mockito.mockStatic(DseApplid.class)) {
            dseApplid.when(() -> DseApplid.get(TAG)).thenReturn(APPLID);
            dpi = new DseProvisioningImpl(manager);
            try (MockedConstruction<DseImsImpl> mc = mockConstruction(DseImsImpl.class, (dseIms, context) -> {
                Assert.assertEquals("Provisioned IMS system has the wrong ImstmManager", manager, (ImstmManagerImpl) context.arguments().get(0));
                Assert.assertEquals("Provisioned IMS system has the wrong tag", TAG, (String) context.arguments().get(1));
                Assert.assertEquals("Provisioned IMS system has the wrong zOS image", zosImage, (IZosImage) context.arguments().get(2));
                Assert.assertEquals("Provisioned IMS system has the wrong applid", APPLID, (String) context.arguments().get(3));

                Mockito.when(dseIms.getZosImage()).thenReturn(zosImage);
            })) {
                IImsSystemProvisioned ims = dpi.provision(TAG, TAG2, null);
                Assert.assertEquals("Wrong IMS system returned", mc.constructed().get(0), ims);
            }
        }
    }

    @Test
    public void testProvisionMixed() throws Exception {
        Mockito.when(manager.getProvisionType()).thenReturn("MIXED");
        try (MockedStatic<DseApplid> dseApplid = Mockito.mockStatic(DseApplid.class)) {
            dseApplid.when(() -> DseApplid.get(TAG)).thenReturn(APPLID);
            dpi = new DseProvisioningImpl(manager);
            try (MockedConstruction<DseImsImpl> mc = mockConstruction(DseImsImpl.class, (dseIms, context) -> {
                Assert.assertEquals("Provisioned IMS system has the wrong ImstmManager", manager, (ImstmManagerImpl) context.arguments().get(0));
                Assert.assertEquals("Provisioned IMS system has the wrong tag", TAG, (String) context.arguments().get(1));
                Assert.assertEquals("Provisioned IMS system has the wrong zOS image", zosImage, (IZosImage) context.arguments().get(2));
                Assert.assertEquals("Provisioned IMS system has the wrong applid", APPLID, (String) context.arguments().get(3));

                Mockito.when(dseIms.getZosImage()).thenReturn(zosImage);
            })) {
                IImsSystemProvisioned ims = dpi.provision(TAG, TAG2, null);
                Assert.assertEquals("Wrong IMS system returned", mc.constructed().get(0), ims);
            }
        }
    }
}
