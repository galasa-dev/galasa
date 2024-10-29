/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.jmeter;

import dev.galasa.ManagerException;

public class JMeterManagerException extends ManagerException {
       private static final long serialVersionUID = 1L;

       public JMeterManagerException() {
       }

       public JMeterManagerException(String message) {
               super(message);
       }

       public JMeterManagerException(Throwable cause) {
               super(cause);
       }

       public JMeterManagerException(String message, Throwable cause) {
               super(message, cause);
       }

       public JMeterManagerException(String message, Throwable cause, boolean enableSuppression,
                       boolean writableStackTrace) {
               super(message, cause, enableSuppression, writableStackTrace);
       }

}