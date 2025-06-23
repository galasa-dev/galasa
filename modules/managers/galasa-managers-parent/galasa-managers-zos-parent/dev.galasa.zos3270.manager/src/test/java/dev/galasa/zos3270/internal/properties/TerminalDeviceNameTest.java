/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.properties;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos3270.Zos3270ManagerException;
import dev.galasa.zos3270.mocks.MockConfigurationPropertyStoreService;
import dev.galasa.zos3270.mocks.MockZosImage;

public class TerminalDeviceNameTest {

    @Test
    public void testCanGetValidDeviceName() throws Exception {
        // Given...
        String imageId = "MYZOSIMAGE";
        String deviceNameInCps = "MYDEVICE";

        String propertyKey = "image." + imageId + ".device.name";

        IZosImage zosImage = new MockZosImage(imageId);

        Map<String, String> cpsProps = new HashMap<>();
        cpsProps.put(propertyKey, deviceNameInCps);

        IConfigurationPropertyStoreService mockCps = new MockConfigurationPropertyStoreService(cpsProps);
        Zos3270PropertiesSingleton singletonInstance = new Zos3270PropertiesSingleton();
        singletonInstance.activate();
        Zos3270PropertiesSingleton.setCps(mockCps);

        // When...
        String deviceNameGotBack = TerminalDeviceName.get(zosImage);

        // Then...
        assertThat(deviceNameGotBack).isEqualTo(deviceNameInCps);
    }

    @Test
    public void testGetDeviceNameEmptyThrowsError() throws Exception {
        // Given...
        String imageId = "MYZOSIMAGE";
        String deviceNameInCps = "    ";

        String propertyKey = "image." + imageId + ".device.name";

        IZosImage zosImage = new MockZosImage(imageId);

        Map<String, String> cpsProps = new HashMap<>();
        cpsProps.put(propertyKey, deviceNameInCps);

        IConfigurationPropertyStoreService mockCps = new MockConfigurationPropertyStoreService(cpsProps);
        Zos3270PropertiesSingleton singletonInstance = new Zos3270PropertiesSingleton();
        singletonInstance.activate();
        Zos3270PropertiesSingleton.setCps(mockCps);

        // When...
        Zos3270ManagerException thrown = catchThrowableOfType(() -> {
            TerminalDeviceName.get(zosImage);
        }, Zos3270ManagerException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown).hasMessageContaining("Empty or invalid device name provided");
    }

    @Test
    public void testGetDeviceNameTooLongThrowsError() throws Exception {
        // Given...
        String imageId = "MYZOSIMAGE";
        String deviceNameInCps = "thisdevicenameistoolong";

        String propertyKey = "image." + imageId + ".device.name";

        IZosImage zosImage = new MockZosImage(imageId);

        Map<String, String> cpsProps = new HashMap<>();
        cpsProps.put(propertyKey, deviceNameInCps);

        IConfigurationPropertyStoreService mockCps = new MockConfigurationPropertyStoreService(cpsProps);
        Zos3270PropertiesSingleton singletonInstance = new Zos3270PropertiesSingleton();
        singletonInstance.activate();
        Zos3270PropertiesSingleton.setCps(mockCps);

        // When...
        Zos3270ManagerException thrown = catchThrowableOfType(() -> {
            TerminalDeviceName.get(zosImage);
        }, Zos3270ManagerException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown).hasMessageContaining("Empty or invalid device name provided");
    }

    @Test
    public void testGetDeviceNameNot7BitAsciiThrowsError() throws Exception {
        // Given...
        String imageId = "MYZOSIMAGE";

        // 7-bit US ASCII code points range from 0 to 127, so any character outside
        // this range is invalid.
        int invalidCodePoint = 128;
        char invalidChar = (char) invalidCodePoint;
        String deviceNameInCps = String.valueOf(invalidChar);

        String propertyKey = "image." + imageId + ".device.name";

        IZosImage zosImage = new MockZosImage(imageId);

        Map<String, String> cpsProps = new HashMap<>();
        cpsProps.put(propertyKey, deviceNameInCps);

        IConfigurationPropertyStoreService mockCps = new MockConfigurationPropertyStoreService(cpsProps);
        Zos3270PropertiesSingleton singletonInstance = new Zos3270PropertiesSingleton();
        singletonInstance.activate();
        Zos3270PropertiesSingleton.setCps(mockCps);

        // When...
        Zos3270ManagerException thrown = catchThrowableOfType(() -> {
            TerminalDeviceName.get(zosImage);
        }, Zos3270ManagerException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown).hasMessageContaining("Empty or invalid device name provided");
    }

    @Test
    public void testGetDeviceNameWithValid7BitAsciiOk() throws Exception {
        // Given...
        String imageId = "MYZOSIMAGE";

        // 7-bit US ASCII code points range from 0 to 127, so any character outside
        // this range is invalid.
        int codePoint = 127;
        char validChar = (char) codePoint;
        String deviceNameInCps = String.valueOf(validChar);

        String propertyKey = "image." + imageId + ".device.name";

        IZosImage zosImage = new MockZosImage(imageId);

        Map<String, String> cpsProps = new HashMap<>();
        cpsProps.put(propertyKey, deviceNameInCps);

        IConfigurationPropertyStoreService mockCps = new MockConfigurationPropertyStoreService(cpsProps);
        Zos3270PropertiesSingleton singletonInstance = new Zos3270PropertiesSingleton();
        singletonInstance.activate();
        Zos3270PropertiesSingleton.setCps(mockCps);

        // When...
        String deviceNameGotBack = TerminalDeviceName.get(zosImage);

        // Then...
        assertThat(deviceNameGotBack).isEqualTo(deviceNameInCps);
    }
}
