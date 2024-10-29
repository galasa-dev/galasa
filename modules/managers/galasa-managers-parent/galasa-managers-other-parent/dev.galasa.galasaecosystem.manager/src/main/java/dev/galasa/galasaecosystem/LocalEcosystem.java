/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.galasaecosystem;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * Local Ecosystem
 * 
 * @galasa.annotation
 * 
 * @galasa.description The <code>{@literal @}LocalEcosystem</code> annotation requests the Galasa Ecosystem Manager to provision
 * a local running environment on a Linux or Windows instance.   The Local running environment will 
 * use the FPF file configuration and not run any of the servers by default.
 * 
 * @galasa.examples 
 * <code>{@literal @}LocaEcosystem(linuxImageTag="PRIMARY")<br>
 * public ILocalEcosystem ecosystem;<br>
 * <br>
 * {@literal @}LocalEcosystem(windowsImageTag="PRIMARY")<br>
 * public ILocalEcosystem ecosystem;<br>
 * </code>
 * 
 * @galasa.extra
 * The <code>ILocalEcosystem</code> interface gives the test access FPF services and the ability to run tests from the commandline.
 * The Manager will pre-configure the CPS, DSS and CREDS before the test begins.<br>
 * <br>
 * The test must provide a {@literal @}LocalNamespace ILocalNamespace annotation, as this is where the Ecosystem is
 * provisioned in.
 * <br>
 * The annotation must provide either a Windows or Linux image tag, but not both and must provide a {@literal @}JavaInstallation tag.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@ValidAnnotatedFields({ ILocalEcosystem.class })
@GalasaEcosystemManagerField
public @interface LocalEcosystem {

    /**
     * The <code>ecosystemNamespaceTag</code> is used to identify the Ecosystem to other Managers or Shared Environments.  If a test is using multiple 
     * Ecosystems, each separate Ecosystem must have a unique tag.  If two Ecosystems use the same tag, they refer to the 
     * same Ecosystem.
     */
    public String ecosystemTag() default "PRIMARY";
    
    /**
     * The <code>linuxImageTag</code> identifies which tagged Linux image is to be used to deploy the Galasa Ecosystem into.
     */
    public String linuxImageTag() default "";
    
    /**
     * The <code>windowsImageTag</code> identifies which tagged Windows image is to be used to deploy the Galasa Ecosystem into.
     */
    public String windowsImageTag() default "";
    
    /**
     * The <code>javaInstallationTag</code> to which Java installation on the image is to be used to run the Galasa tests and services.
     */
    public String javaInstallationTag() default "PRIMARY";

    /**
     * The <code>addDefaultZosImage</code> add the zOS image to the ecosystem as part of the default cluster
     */
    public String addDefaultZosImage() default "";
    
    public IsolationInstallation isolationInstallation() default IsolationInstallation.None;
    
    public boolean startSimPlatform() default false;
}