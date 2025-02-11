
/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.rbac;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.galasa.framework.internal.rbac.ActionImpl;

public enum BuiltInAction {
    GENERAL_API_ACCESS            (new ActionImpl("GENERAL_API_ACCESS", "General API access", "Able to access the REST API" )),
    USER_EDIT_OTHER               (new ActionImpl("USER_EDIT_OTHER", "Edit or delete a user other than you", "Edit or delete a user other than you, including role and access tokens")),
    SECRETS_GET_UNREDACTED_VALUES (new ActionImpl("SECRETS_GET_UNREDACTED_VALUES", "Get secret values", "Able to get unredacted secret values")),
    SECRETS_SET                   (new ActionImpl("SECRETS_SET", "Secrets set", "Able to set secrets")),
    SECRETS_DELETE                (new ActionImpl("SECRETS_DELETE", "Secrets delete", "Able to delete secrets")),
    CPS_PROPERTIES_SET            (new ActionImpl("CPS_PROPERTIES_SET", "CPS properties set", "Able to set CPS properties")),
    CPS_PROPERTIES_DELETE         (new ActionImpl("CPS_PROPERTIES_DELETE", "CPS properties delete", "Able to delete CPS properties")),
    RUNS_DELETE_OTHER_USERS       (new ActionImpl("RUNS_DELETE_OTHER_USERS", "Runs delete other users", "Able to delete runs submitted by other users"));

    private Action action;
    private static List<Action> allActions = Stream.of(values())
        .map(builtInAction -> builtInAction.getAction())
        .collect(Collectors.toList());

    private BuiltInAction(Action action) {
        this.action = action;
    }

    public Action getAction() {
        return this.action;
    }

    public static List<Action> getActions() {
        return allActions;
    }
}
