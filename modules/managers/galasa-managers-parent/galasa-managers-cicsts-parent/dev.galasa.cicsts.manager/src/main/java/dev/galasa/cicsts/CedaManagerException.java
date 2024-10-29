/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts;

public class CedaManagerException extends CicstsManagerException {
   
   private static final long serialVersionUID = 1L;
   
   public CedaManagerException() {}
   
   public CedaManagerException(String message) {
        super(message);
   }
   
   public CedaManagerException(Throwable cause) {
      super(cause);
   }
   
   public CedaManagerException(String message, Throwable cause) {
      super(message, cause);
   }
   
   public CedaManagerException(String message, Throwable cause, boolean enableSuppression,
         boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
   }
   
   
   
}
