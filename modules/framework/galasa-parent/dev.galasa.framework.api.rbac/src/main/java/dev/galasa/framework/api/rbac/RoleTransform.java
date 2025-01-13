/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.rbac;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dev.galasa.framework.api.beans.generated.RBACRole;
import dev.galasa.framework.api.beans.generated.RBACRoleData;
import dev.galasa.framework.api.beans.generated.RBACRoleMetadata;
import dev.galasa.framework.api.common.resources.GalasaResourceValidator;
import dev.galasa.framework.spi.rbac.Role;

/**
 * Converts an internal role structure into an external RBACRole bean.
 */
public class RoleTransform {

    public RBACRole createRoleBean(Role role, String url ) {
        RBACRole roleBean = new RBACRole();

        roleBean.setApiVersion(GalasaResourceValidator.DEFAULT_API_VERSION);
        roleBean.setkind("GalasaRole");

        RBACRoleMetadata metadata = createRoleMetadata(role,url);

        roleBean.setmetadata(metadata);

        RBACRoleData data = new RBACRoleData();
        roleBean.setdata(data);

        List<String> actionIds = role.getActionIds();
        String[] actionIdsArray = new String[actionIds.size()];


        int index = 0;
        for( String actionId: actionIds ) {
            actionIdsArray[index] = actionId;
            index+=1;
        }

        data.setactions(actionIdsArray);
        return roleBean;
    }

    private RBACRoleMetadata createRoleMetadata(Role role, String url) {
        RBACRoleMetadata metadata = new RBACRoleMetadata();
        metadata.setdescription(role.getDescription());
        metadata.setid(role.getId());
        metadata.setname(role.getName());
        metadata.seturl(url);
        return metadata;
    }

    public List<RBACRole> createRolesBeans(Collection<Role> roles, String baseUrl) {
        List<RBACRole> rolesBeans = new ArrayList<RBACRole>();
        for( Role role : roles ) {

            String url ;
            if (baseUrl.endsWith("/")) {
                url = baseUrl+role.getId();
            } else {
                url = baseUrl+"/"+role.getId();
            }

            RBACRole roleBean = createRoleBean(role, url);
            rolesBeans.add(roleBean);
        }
        return rolesBeans;
    }
    
}
