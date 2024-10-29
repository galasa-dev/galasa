/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.openstack.manager.internal.json;

import java.util.List;

public class Token {

    public String    expires_at; // NOSONAR

    public List<Api> catalog;    // NOSONAR

}
