/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.framework.api.authentication.internal.beans;

import com.google.gson.annotations.SerializedName;

/**
 * A bean class representing a JSON Web Key (JWK) retrieved from Dex.
 * A JWK contains the following fields:
 * {
 *   "use": "sig",
 *   "kty": "RSA",
 *   "kid": "123abc",
 *   "alg": "RS256",
 *   "n": "abcdefg",
 *   "e": "xyz"
 * }
 */
public class JsonWebKey {

    @SerializedName("use")
    private String use;

    @SerializedName("kty")
    private String keyType;

    @SerializedName("kid")
    private String keyId;

    @SerializedName("alg")
    private String algorithm;

    @SerializedName("n")
    private String rsaModulus;

    @SerializedName("e")
    private String rsaExponent;

    public String getUse() {
        return use;
    }

    public String getKeyType() {
        return keyType;
    }

    public String getKeyId() {
        return keyId;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getRsaModulus() {
        return rsaModulus;
    }

    public String getRsaExponent() {
        return rsaExponent;
    }
}
