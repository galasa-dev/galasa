/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ipnetwork;

/**
 * zOS Command exception indicating an error in SSH
 * 
 *  
 *
 */
public class SSHException extends IpNetworkManagerException {
    private static final long serialVersionUID = 1L;

    public SSHException() {
    }

    public SSHException(String message) {
        super(message);
    }

    public SSHException(Throwable throwable) {
        super(throwable);
    }

    public SSHException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
