/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.resources;

import dev.galasa.zossecurity.IZosKerberosPrincipal;
import dev.galasa.zossecurity.IZosProfile;
import dev.galasa.zossecurity.IZosUserid;
import dev.galasa.zossecurity.ZosSecurityManagerException;
import dev.galasa.zossecurity.datatypes.RACFAccessType;
import dev.galasa.zossecurity.internal.ZosSecurityImpl;

public class ZosKerberosClientPrincipalImpl extends ZosKerberosPrincipalImpl {
	
	private IZosProfile clientProfile;

	protected ZosKerberosClientPrincipalImpl(ZosSecurityImpl zosSecurity, String clientPrincipalName, IZosUserid user, IZosKerberosPrincipal servicePrincipal, IZosProfile clientProfile) throws ZosSecurityManagerException {
		super(zosSecurity, clientPrincipalName, servicePrincipal.getRealm(), user, null);
		
		this.clientProfile = clientProfile;
	}

	public static IZosKerberosPrincipal createPrincipal(ZosSecurityImpl zosSecurity, IZosKerberosPrincipal servicePrincipal, IZosUserid clientUserid) throws ZosSecurityManagerException {
		
		ZosUseridImpl zosuser = (ZosUseridImpl) clientUserid;

		String principalName = generatePrincipalName(clientUserid);
		
		IZosProfile clientProfile = zosSecurity.createProfile("KERBLINK", principalName + "/" + servicePrincipal.getRealm(), RACFAccessType.NONE, false);
		clientProfile.setAccess(servicePrincipal.getUserid(), RACFAccessType.READ, true);
		
		ZosKerberosClientPrincipalImpl clientPrincipal = new ZosKerberosClientPrincipalImpl(zosSecurity, principalName, zosuser, servicePrincipal, clientProfile);
		
		zosuser.setKerbname(principalName);
		
		return clientPrincipal;
	}
	
	@Override
	public void free() throws ZosSecurityManagerException {
		super.free();
		
		clientProfile.free();
	}

	public static String generatePrincipalName(IZosUserid user) {
		return user.getUserid().toLowerCase() + "_client";
	}

	@Override
	public String toString() {
		return "[zOS Security Kerberos Client Principal] " + getResourceName();
	}
}
