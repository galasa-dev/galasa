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
 * Kubernetes Ecosystem
 * 
 * @galasa.annotation
 * 
 * @galasa.description The <code>{@literal @}KubernetesEcosystem</code> annotation requests the Galasa Ecosystem Manager to provision
 * a Galasa Ecosystem within a Kubernetes Namespace.
 * 
 * @galasa.examples 
 * <code>{@literal @}KubernetesEcosystem<br>
 * public IKubernetesEcosystem ecosystem;<br>
 * <br>
 * {@literal @}KubernetesEcosystem(yamlDirectory="/k8syaml"<br>
 * public IKubernetesEcosystem ecosystem;<br>
 * </code>
 * 
 * @galasa.extra
 * The <code>IKubernetesEcosystem</code> interface gives the test access to the URLs of the services and API endpoints within the Ecosystem.
 * When the test runs all the services that are required by the test are known to be up and working.<br>
 * <br>
 * The test must provide a {@literal @}KubernetesNamespace IKubernetesNamespace annotation, as this is where the Ecosystem is
 * provisioned in.  In the future, Docker and Linux will be options.
 * <br>
 * The Galasa Ecosystem has its own stable versions of the Kubernetes yaml files that are needed to create the entire Ecosystem.  If you want to override those and
 * use your own yaml files, then use the yamlDirectory attribute.  If a resource is missing in the test's set, then the stable version is used. 
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@ValidAnnotatedFields({ IKubernetesEcosystem.class })
@GalasaEcosystemManagerField
public @interface KubernetesEcosystem {

    /**
     * The <code>ecosystemNamespaceTag</code> is used to identify the Ecosystem to other Managers or Shared Environments.  If a test is using multiple 
     * Ecosystems, each separate Ecosystem must have a unique tag.  If two Ecosystems use the same tag, they refer to the 
     * same Ecosystem.
     */
    public String ecosystemNamespaceTag() default "PRIMARY";
    
    /**
     * The <code>kubernetesNamespaceTag</code> identifies which tagged Kubernetes Namespace is to be used to deploy the Galasa Ecosystem into.
     */
    public String kubernetesNamespaceTag() default "PRIMARY";
    
    /**
     * The <code>yamlDirectory</code> points to a resource directory within the test bundle that contains a set of override yaml files to use when creating the 
     * ecosystem.  Each file must end with .yaml to be found.  If a directory or resource is not provided, the stable yaml files within the Galasa Ecosystem Manager will be used.
     */
    public String yamlDirectory() default "";
}