/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.java.internal.properties;

import dev.galasa.CpuArchitecture;
import dev.galasa.OperatingSystem;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.java.JavaManagerException;
import dev.galasa.java.JavaType;
import dev.galasa.java.JavaVersion;

/**
 * Java archive download location
 * 
 * @galasa.cps.property
 * 
 * @galasa.name java.archive.type.operatingsystem.architecture.version.jvm.location
 * 
 * @galasa.description Indicate where to download specific versions of a Java Archive
 * 
 * @galasa.required Yes
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values a valid URL
 * 
 * @galasa.examples 
 * <code>java.archive.jdk.linux.x64.v8.hotspot.location=http://download/jdk.tgz</code>
 * 
 */
public class DownloadLocation extends CpsProperties {

    public static String get(JavaType javaType, 
            OperatingSystem operatingSystem,
            CpuArchitecture cpuArchitecture,
            JavaVersion     javaVersion,
            String          javaJvm) throws JavaManagerException {
        
        String propertyName = javaType.name() 
                + "." 
                + operatingSystem.name() 
                + "."
                + cpuArchitecture.name()
                + "."
                + javaVersion.name()
                + "."
                + javaJvm
                + ".location";
        
        try {
            return getStringNulled(JavaPropertiesSingleton.cps(), "archive", propertyName) ;
        } catch (ConfigurationPropertyStoreException e) {
            throw new JavaManagerException("Problem retrieving the java archive " + propertyName, e);
        }
    }
}