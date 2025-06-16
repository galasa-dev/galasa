/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.properties;

import java.nio.charset.StandardCharsets;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos3270.Zos3270ManagerException;

/**
 * The 3270 client device name to connect to.
 *
 * The property takes the following format:
 * zos3270.image.IMAGEID.device.name=LU1
 *
 * If no device name is given, a null value will be returned.
 * 
 * As per https://www.rfc-editor.org/rfc/rfc2355, device names are case-insensitive 7-bit ASCII strings
 * that must not exceed 8 bytes.
 */
public class TerminalDeviceName extends CpsProperties {

    private static final int MAX_DEVICE_NAME_LENGTH = 8;

    private static boolean isDeviceNameValid(String deviceName) {
        boolean isValid = false;
        if (deviceName != null && !deviceName.isBlank()) {
            isValid = deviceName.length() <= MAX_DEVICE_NAME_LENGTH && StandardCharsets.US_ASCII.newEncoder().canEncode(deviceName);
        }
        return isValid;
    }

    public static String get(IZosImage image) throws Zos3270ManagerException {
        try {
            String deviceName = getStringNulled(Zos3270PropertiesSingleton.cps(), "image", "device.name", image.getImageID());
            if (!isDeviceNameValid(deviceName)) {
                throw new Zos3270ManagerException("Empty or invalid device name provided. Device name must not exceed 8 characters and must only include 7-bit ASCII characters.");
            }
            return deviceName;
        } catch (ConfigurationPropertyStoreException e) {
            throw new Zos3270ManagerException("Failed to get a value for the terminal device name from the CPS", e);
        }
    }
}
