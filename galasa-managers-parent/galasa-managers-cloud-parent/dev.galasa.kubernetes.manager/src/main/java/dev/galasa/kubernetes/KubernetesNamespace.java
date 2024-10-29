/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.kubernetes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;
import dev.galasa.kubernetes.internal.KubernetesManagerField;

/**
 * Kubernetes Namespace
 * 
 * @galasa.annotation
 * 
 * @galasa.description The <code>{@literal @}KubernetesNamespace</code> annotation requests the Kubernetes Manager to allocate a namespace
 * on the infrastructure Kubernetes clusters.  The test can request as many namespaces as required so long as they 
 * can be supported simultaneously by the Kubernetes Manager configuration.
 * 
 * @galasa.examples 
 * <code>{@literal @}KubernetesNamespace<br>
 * public IKubernetesNamesapce namespace;<br>
 * </code>
 * 
 * @galasa.extra
 * The <code>IKubernetesNamespace</code> interface gives the test access to create and manage resources on the Kubernetes cluster. 
 * See {@link KubernetesNamespace} and {@link IKubernetesNamespace} to find out more.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@ValidAnnotatedFields({ IKubernetesNamespace.class })
@KubernetesManagerField
public @interface KubernetesNamespace {

    /**
     * The <code>kubernetesNamespaceTag</code> identifies the Kubernetes names to other Managers or Shared Environments.  If a test is using multiple 
     * Kubernetes namespace, each separate Kubernetes namespace must have a unique tag.  If more than one Kubernetes namespace use the same tag, they will refer to the 
     * same Kubernetes namespace.
     */
    public String kubernetesNamespaceTag() default "PRIMARY";
}