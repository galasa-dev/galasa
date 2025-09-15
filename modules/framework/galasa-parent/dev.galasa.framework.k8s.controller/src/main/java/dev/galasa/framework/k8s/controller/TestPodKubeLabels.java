/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller;

/**
 * An enum of custom labels that are added to the Kubernetes metadata of test pods
 */
public enum TestPodKubeLabels {

    /**
     * Test pods are marked with a kube label of this, with a value holding the test run name. eg: U643
     */
    GALASA_RUN("galasa-run"),

    /**
     * Test pods have this kube label to identify which Galasa service the test pods belong to
     */
    GALASA_SERVICE_NAME("galasa-service-name"),

    /**
     * Test pods have a kube label with a value being the name of the engine controller
     * the pods were launched using
     */
    ENGINE_CONTROLLER("galasa-engine-controller");
    ;

    private String label;

    private TestPodKubeLabels(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
