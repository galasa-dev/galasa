/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.rbac.internal.routes;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.Test;

import dev.galasa.framework.api.beans.generated.RBACAction;
import dev.galasa.framework.api.beans.generated.RBACActionMetadata;
import dev.galasa.framework.mocks.MockAction;
import dev.galasa.framework.spi.rbac.Action;

public class ActionsTransformTest {

    @Test
    public void testTransformGivesRBACActionOk() {
        String actionId = "myActionId";
        String actionName = "myActionName";
        String actionDescription = "myActionDescription";
        Action action = new MockAction(actionId, actionName, actionDescription);

        String baseUrl = "https://myHost/api/actions";
        ActionTransform xform = new ActionTransform();
        
        RBACAction bean = xform.createActionBean(action,baseUrl);
        assertThat(bean.getApiVersion()).isEqualTo("galasa-dev/v1alpha1");
        assertThat(bean.getkind()).isEqualTo("GalasaAction");

        RBACActionMetadata metadata = bean.getmetadata();
        assertThat(metadata).isNotNull();
        assertThat(metadata.getdescription()).isEqualTo(actionDescription);
        assertThat(metadata.getid()).isEqualTo(actionId);
        assertThat(metadata.getname()).isEqualTo(actionName);
    }

    @Test
    public void testTransformActionsGivesListOfActionMetadataBeans() {
        String actionId = "myActionId";
        String actionName = "myActionName";
        String actionDescription = "myActionDescription";
        Action action = new MockAction(actionId, actionName, actionDescription);

        String action1Id = "myAction1Id";
        String action1Name = "myAction1Name";
        String action1Description = "myAction1Description";
        Action action1 = new MockAction(action1Id, action1Name, action1Description);

        List<Action> actions = List.of(action, action1);

        String baseUrl = "https://myHost/api/actions";
        ActionTransform xform = new ActionTransform();

        List<RBACActionMetadata> beans = xform.creteaActionsSummary(actions,baseUrl);
        assertThat(beans).hasSize(2);

        assertThat(beans.get(0).getid()).isEqualTo(actionId);
        assertThat(beans.get(0).getdescription()).isEqualTo(actionDescription);
        assertThat(beans.get(0).getname()).isEqualTo(actionName);
        assertThat(beans.get(0).geturl()).isEqualTo(baseUrl+"/"+action.getId());

        assertThat(beans.get(1).getid()).isEqualTo(action1Id);
        assertThat(beans.get(1).getdescription()).isEqualTo(action1Description);
        assertThat(beans.get(1).getname()).isEqualTo(action1Name);
        assertThat(beans.get(1).geturl()).isEqualTo(baseUrl+"/"+action1.getId());
    }
}