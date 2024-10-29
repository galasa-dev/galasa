/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.datatypes;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(namespace="http://jaxb.zossec.jat.ibm.com")
@XmlType(namespace="http://jaxb.zossec.jat.ibm.com")
public class RACFAccess {

	@XmlElement(namespace="http://jaxb.zossec.jat.ibm.com")
	public String id;
	
	@XmlElement(namespace="http://jaxb.zossec.jat.ibm.com")
	public RACFAccessType access;
}
