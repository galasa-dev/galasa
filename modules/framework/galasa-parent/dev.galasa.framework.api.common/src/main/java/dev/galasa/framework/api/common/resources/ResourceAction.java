 /*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;
 
public enum ResourceAction {
    APPLY("apply"),
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete");

    private String actionLabel;

    private ResourceAction(String action) {
        this.actionLabel = action;
    }

    public static ResourceAction getFromString(String actionAsString) {
        ResourceAction match = null;
        for (ResourceAction action : values()) {
            if (action.toString().equalsIgnoreCase(actionAsString.trim())) {
                match = action;
                break;
            }
        }
        return match;
    }

    @Override
    public String toString() {
        return actionLabel;
    }
}