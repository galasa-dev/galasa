/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa;

import java.util.Properties;

public interface ICredentials {
    Properties toProperties(String credentialsId);
}
