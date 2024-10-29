/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.zos.IZosImage;
import dev.galasa.zossecurity.IZosCicsProfile;
import dev.galasa.zossecurity.ZosSecurityManagerException;
import dev.galasa.zossecurity.datatypes.RACFAccessType;
import dev.galasa.zossecurity.datatypes.ZosCicsClassResource;
import dev.galasa.zossecurity.datatypes.ZosCicsClassType;
import dev.galasa.zossecurity.internal.ZosSecurityImpl;
import dev.galasa.zossecurity.internal.ZosSecurityImpl.ResourceType;

public class ZosCicsProfileImpl extends ZosProfileImpl implements IZosCicsProfile {
	
	private final ZosCicsClassResource classResource;
	private final ZosCicsClassType     classType;
	private final ZosSecurityImpl zosSecurity;
	
	private static final Log logger = LogFactory.getLog(ZosCicsProfileImpl.class);
	
	protected ZosCicsProfileImpl(ZosSecurityImpl zosSecurity, String className, String refreshClassName, String profileName, IZosImage image, ZosCicsClassResource classResource, ZosCicsClassType classType) {
		super(zosSecurity, className, refreshClassName, profileName, image);
		
		this.zosSecurity = zosSecurity;
		this.classResource = classResource;
		this.classType = classType;
	}

	@Override
	public void free() throws ZosSecurityManagerException {
		zosSecurity.dssFree(ResourceType.ZOS_PROFILE.getName(), getClassProfileName());
		logger.debug("zOS CICS Profile '" + getClassProfileName() + "' was freed");
	}
	
	@Override
	public ZosCicsClassResource getClassResource() {
		return this.classResource;
	}

	@Override
	public ZosCicsClassType getClassType() {
		return this.classType;
	}
	
	protected static ZosCicsProfileImpl createProfile(ZosSecurityImpl zosSecurity, String className, String refreshClassName, String profileName, IZosImage image, ZosCicsClassResource classResource, ZosCicsClassType classType, RACFAccessType uacc, boolean refresh) throws ZosSecurityManagerException {
		if (className == null || className.trim().isEmpty()) {
			throw new ZosSecurityManagerException("The class name is missing");
		}
		if (profileName == null || profileName.trim().isEmpty()) {
			throw new ZosSecurityManagerException("The profile name is missing");
		}
		if (image == null) {
			throw new ZosSecurityManagerException("The lpar is missing");
		}
		
		className = className.trim();
		profileName = profileName.trim();

		ZosCicsProfileImpl profile = new ZosCicsProfileImpl(zosSecurity, className, refreshClassName, profileName, image, classResource, classType);
		zosSecurity.dssRegister(ResourceType.ZOS_PROFILE.getName(), className + "/" + profileName);

		profile.createProfileInRACF(null, uacc, true, refresh);

		return profile;
	}

	@Override
	public String toString() {
		return "[zOS Security CICS Profile] " + getName();
	}
}
