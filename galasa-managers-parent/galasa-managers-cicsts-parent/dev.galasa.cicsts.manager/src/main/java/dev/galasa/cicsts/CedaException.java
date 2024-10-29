/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts;

public class CedaException extends CedaManagerException {
   private static final long serialVersionUID = 1L;
   
   public CedaException() {
   }

   public CedaException(String message) {
       super(message);
   }

   public CedaException(Throwable cause) {
       super(cause);
   }

   public CedaException(String message, Throwable cause) {
       super(message, cause);
   }

   public CedaException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
       super(message, cause, enableSuppression, writableStackTrace);
   }

   
   
}
