/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts;

public class CemtException extends CemtManagerException {
   private static final long serialVersionUID = 1L;
   
   public CemtException() {
   }

   public CemtException(String message) {
       super(message);
   }

   public CemtException(Throwable cause) {
       super(cause);
   }

   public CemtException(String message, Throwable cause) {
       super(message, cause);
   }

   public CemtException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
       super(message, cause, enableSuppression, writableStackTrace);
   }
}
