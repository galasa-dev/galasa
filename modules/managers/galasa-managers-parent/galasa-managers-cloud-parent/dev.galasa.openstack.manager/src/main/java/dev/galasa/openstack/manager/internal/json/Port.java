/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.openstack.manager.internal.json;

import java.util.List;

public class Port {

    public String              id;             // NOSONAR
    public String              device_id;      // NOSONAR

    public List<DnsAssignment> dns_assignment; // NOSONAR

}
