/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity;

import jakarta.xml.bind.DatatypeConverter;

/**
 * Object representing a Kerberos Token
 * 
 *  
 *
 */
public class KerberosToken {

	private final byte[] bytes;

	/**
	 * Construct with the kerberos token in byte[] form
	 * 
	 * @param bytes
	 */
	public KerberosToken(byte[] bytes) {
		this.bytes = bytes;
	}

	/**
	 * 
	 * @return - token in byte[] form
	 */
	public byte[] getBytes() {
		return bytes;
	}

	/**
	 * 
	 * @return - token in base64 encoded form
	 */
	public String getBase64EncodedString() {
		return DatatypeConverter.printBase64Binary(getBytes());
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		String encodedString = getBase64EncodedString();
		int i = 0;
		while (encodedString.length() > i + 80) {
			sb.append(encodedString.substring(i, (i = (i + 80))) + "\n");
		}

		sb.append(encodedString.substring(i));

		return sb.toString();
	}
}
