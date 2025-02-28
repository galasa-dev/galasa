/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.internal.dse;

import static org.mockito.Mockito.mockConstruction;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.imstm.ImstmManagerException;
import dev.galasa.imstm.internal.ImstmManagerImpl;
import dev.galasa.imstm.internal.ImstmProperties;
import dev.galasa.imstm.IImsSystem;
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
    @Mock
    private static IZosImage zosImage;
    @Mock
    private static ImstmProperties properties;

    @Before
    public void setup() throws Exception{
        Mockito.when(manager.getProvisionType()).thenReturn("DSE");
        Mockito.when(manager.getZosManager()).thenReturn(zosManager);
        Mockito.when(zosManager.getImageForTag(TAG2)).thenReturn(zosImage);
        Mockito.when(zosImage.getImageID()).thenReturn("IMAGE");
    }

    @Test
    public void testProvisionNotEnabled() throws Exception {
        Mockito.when(manager.getProvisionType()).thenReturn("NOT_DSE");
        dpi = new DseProvisioningImpl(manager, properties);
        Assert.assertNull("Unexpected value returned from DseProvisioningImpl.provision()", dpi.provision(TAG, TAG2, null));
    }
    
    @Test
    public void testProvisionNoApplid() throws Exception {
        Mockito.when(properties.getDseApplid(TAG)).thenReturn(null);
        dpi = new DseProvisioningImpl(manager, properties);
        Assert.assertNull("Unexpected value returned from DseProvisioningImpl.provision()", dpi.provision(TAG, TAG2, null));
}

    @Test
    public void testProvisionNoZosImage() throws Exception {
        Mockito.when(zosManager.getImageForTag(TAG2)).thenThrow(new ZosManagerException());
        Mockito.when(properties.getDseApplid(TAG)).thenReturn("APPLID");
        dpi = new DseProvisioningImpl(manager, properties);
        String expectedMessage = "Unable to locate zOS Image tagged " + TAG2;
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
            dpi.provision(TAG, TAG2, null);
        });
        Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
}

    @Test
    public void testProvision() throws Exception {
        Mockito.when(properties.getDseApplid(TAG)).thenReturn("APPLID");
        dpi = new DseProvisioningImpl(manager, properties);
        try (MockedConstruction<DseImsImpl> mc = mockConstruction(DseImsImpl.class, (dseIms, context) -> {
            Assert.assertEquals("Provisioned IMS system has the wrong ImstmManager", manager, (ImstmManagerImpl) context.arguments().get(0));
            Assert.assertEquals("Provisioned IMS system has the wrong ImstmProperties", properties, (ImstmProperties) context.arguments().get(1));
            Assert.assertEquals("Provisioned IMS system has the wrong tag", TAG, (String) context.arguments().get(2));
            Assert.assertEquals("Provisioned IMS system has the wrong zOS image", zosImage, (IZosImage) context.arguments().get(3));
            Assert.assertEquals("Provisioned IMS system has the wrong applid", APPLID, (String) context.arguments().get(4));

            Mockito.when(dseIms.getZosImage()).thenReturn(zosImage);
        })) {
            IImsSystem ims = dpi.provision(TAG, TAG2, null);
            Assert.assertEquals("Wrong IMS system returned", mc.constructed().get(0), ims);
        }
}

    @Test
    public void testProvisionMixed() throws Exception {
        Mockito.when(manager.getProvisionType()).thenReturn("MIXED");
        Mockito.when(properties.getDseApplid(TAG)).thenReturn("APPLID");
        dpi = new DseProvisioningImpl(manager, properties);
        try (MockedConstruction<DseImsImpl> mc = mockConstruction(DseImsImpl.class, (dseIms, context) -> {
            Assert.assertEquals("Provisioned IMS system has the wrong ImstmManager", manager, (ImstmManagerImpl) context.arguments().get(0));
            Assert.assertEquals("Provisioned IMS system has the wrong ImstmProperties", properties, (ImstmProperties) context.arguments().get(1));
            Assert.assertEquals("Provisioned IMS system has the wrong tag", TAG, (String) context.arguments().get(2));
            Assert.assertEquals("Provisioned IMS system has the wrong zOS image", zosImage, (IZosImage) context.arguments().get(3));
            Assert.assertEquals("Provisioned IMS system has the wrong applid", APPLID, (String) context.arguments().get(4));

            Mockito.when(dseIms.getZosImage()).thenReturn(zosImage);
        })) {
            IImsSystem ims = dpi.provision(TAG, TAG2, null);
            Assert.assertEquals("Wrong IMS system returned", mc.constructed().get(0), ims);
        }
}
}
