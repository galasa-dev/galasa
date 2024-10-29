/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.http.internal;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * Client Authentication Trust Manager
 * 
 *  
 *
 */
public class ClientAuthTrustManager implements X509TrustManager {

    private final X509Certificate certificate;

    /**
     * Create trust manager with keystore and alias of the certificate to extract
     * 
     * @param keyStore
     * @param alias
     * @throws KeyStoreException
     */
    public ClientAuthTrustManager(KeyStore keyStore, String alias) throws KeyStoreException {
        certificate = (X509Certificate) keyStore.getCertificate(alias);
    }

    @Override
    public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException { // NOSONAR
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String arg1) throws CertificateException {

        if (!certificate.equals(chain[0])) {
            throw new CertificateException("Certificate does not match the root of the given chain");
        }

        if (!certificate.getSubjectDN().equals(chain[0].getSubjectDN())) {
            throw new CertificateException("Certificate subject does not match that of the root of the given chain");
        }

        if (!certificate.getPublicKey().equals(chain[0].getPublicKey())) {
            throw new CertificateException("Certificate public key does not match that of the root of the given chain");
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

}
