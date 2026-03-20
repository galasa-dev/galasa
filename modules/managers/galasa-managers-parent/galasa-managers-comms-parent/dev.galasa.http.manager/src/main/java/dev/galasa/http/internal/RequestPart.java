/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.http.internal;

import org.apache.hc.client5.http.entity.mime.ContentBody;

public interface RequestPart {

    ContentBody getBody();

    String getType();
}
