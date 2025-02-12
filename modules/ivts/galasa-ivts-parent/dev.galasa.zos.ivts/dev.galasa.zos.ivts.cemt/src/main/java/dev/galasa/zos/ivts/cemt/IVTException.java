/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.ivts.cemt;

public class IVTException extends Exception {
   
   private static final long serialVersionUID = 1L;

   public IVTException() {
   }

   public IVTException(String message) {
       super(message);
   }

   public IVTException(Throwable cause) {
       super(cause);
   }

   public IVTException(String message, Throwable cause) {
       super(message, cause);
   }

   public IVTException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
       super(message, cause, enableSuppression, writableStackTrace);
   }
   
}
