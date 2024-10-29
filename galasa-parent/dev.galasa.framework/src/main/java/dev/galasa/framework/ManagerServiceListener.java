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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

public class ManagerServiceListener implements ServiceListener {
    
    private static final String UNKNOWN = "UNKNOWN";

    private static final Log logger = LogFactory.getLog(ManagerServiceListener.class);

    @Override
    public void serviceChanged(ServiceEvent event) {
        if (event.getType() == ServiceEvent.REGISTERED) {
            reportBundleVersion(event.getServiceReference().getBundle());
        }
    }

    private void reportBundleVersion(Bundle bundle) {
        logger.trace("Manager started : " + getBundleName(bundle));
        logger.trace("        version : " + bundle.getVersion());
        logger.trace("          build : " + getBundleBuild(bundle));
    }

    public String getBundleName(Bundle bundle) {
        String name = UNKNOWN;

        Dictionary<String, String> headers = bundle.getHeaders();
        if (headers != null) {
            String bundleName = headers.get("Bundle-Name");
            if (bundleName != null) {
                name = bundleName;
            }
        }

        return name;
    }

    public String getBundleVersion(Bundle bundle) {
        String version = UNKNOWN;

        Dictionary<String, String> headers = bundle.getHeaders();
        if (headers != null) {
            String bundleName = headers.get("Bundle-Version");
            if (bundleName != null) {
                version = bundleName;
            }
        }

        return version;
    }

    public String getBundleBuild(Bundle bundle) {
        String build = UNKNOWN;
        Dictionary<String, String> headers = bundle.getHeaders();
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
