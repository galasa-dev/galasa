/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.rbac.internal.routes;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.Test;

import dev.galasa.framework.api.beans.generated.*;
import dev.galasa.framework.api.rbac.RoleTransform;
import dev.galasa.framework.mocks.MockAction;
import dev.galasa.framework.spi.rbac.Action;
import dev.galasa.framework.spi.rbac.Role;
import dev.galasa.framework.mocks.MockRole;

public class RolesTransformTest {

    @Test
    public void testTransformGivesRBACRolesOk() {
        String actionId = "myActionId";
        String actionName = "myActionName";
        String actionDescription = "myActionDescription";
        Action action = new MockAction(actionId, actionName, actionDescription);

        String myRole1Name = "myRole1Name";
        String myRole1Id = "myRole1Id";
        String myRole1Description = "myRole1Description";


        Role role = new MockRole(myRole1Name, myRole1Id, myRole1Description, List.of(action.getId()) );

        String baseUrl = "https://myHost/api/roles";
        RoleTransform xform = new RoleTransform();
        
        RBACRole bean = xform.createRoleBean(role,baseUrl);
        
        assertThat(bean.getApiVersion()).isEqualTo("galasa-dev/v1alpha1");
        assertThat(bean.getkind()).isEqualTo("GalasaRole");

        RBACRoleMetadata metadata = bean.getmetadata();
        assertThat(metadata).isNotNull();
        assertThat(metadata.getdescription()).isEqualTo(myRole1Description);
        assertThat(metadata.getid()).isEqualTo(myRole1Id);
        assertThat(metadata.getname()).isEqualTo(myRole1Name);
    }
}