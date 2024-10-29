/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts;

public class CemtManagerException extends CicstsManagerException {

   private static final long serialVersionUID = 1L;
   
   public CemtManagerException() {}
   
   public CemtManagerException(String message) {
        super(message);
   }
   
   public CemtManagerException(Throwable cause) {
      super(cause);
   }
   
   public CemtManagerException(String message, Throwable cause) {
      super(message, cause);
   }
   
   public CemtManagerException(String message, Throwable cause, boolean enableSuppression,
         boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
   }
   
   
}
