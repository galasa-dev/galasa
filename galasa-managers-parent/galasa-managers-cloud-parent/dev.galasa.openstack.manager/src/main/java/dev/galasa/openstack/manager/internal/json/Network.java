/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.openstack.manager.internal.json;

import com.google.gson.annotations.SerializedName;

public class Network {

    public String  id;             // NOSONAR
    public String  name;           // NOSONAR

    @SerializedName("router:external")
    public boolean route_external; // NOSONAR

}
