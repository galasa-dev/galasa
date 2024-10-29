/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Dictionary;

import org.osgi.framework.FrameworkUtil;

public class FrameworkVersion {

    public static String getBundleVersion() {
        String version = "UNKNOWN";

        Dictionary<String, String> headers = FrameworkUtil.getBundle(FrameworkVersion.class).getHeaders();
        if (headers != null) {
            String bundleVersion = headers.get("Bundle-Version");
            if (bundleVersion != null) {
                version = bundleVersion;
            }
        }

        return version;
    }

    public static String getBundleBuild() {
        String build = "UNKNOWN";
        Dictionary<String, String> headers = FrameworkUtil.getBundle(FrameworkVersion.class).getHeaders();
        if (headers != null) {
            String bndLastModified = headers.get("Bnd-LastModified");
            if (bndLastModified != null) {
                Instant time = Instant.ofEpochMilli(Long.parseLong(bndLastModified));
                ZonedDateTime zdt = ZonedDateTime.ofInstant(time, ZoneId.systemDefault());
                build = zdt.format(DateTimeFormatter.RFC_1123_DATE_TIME);
            }
        }

        return build;
    }

}
