/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.zos.IZosImage;
import dev.galasa.zossecurity.IZosCicsClassSet;
import dev.galasa.zossecurity.IZosCicsProfile;
import dev.galasa.zossecurity.IZosProfile;
import dev.galasa.zossecurity.ZosSecurityManagerException;
import dev.galasa.zossecurity.datatypes.ZosCicsClassResource;
import dev.galasa.zossecurity.datatypes.ZosCicsClassType;
import dev.galasa.zossecurity.datatypes.RACFAccessType;
import dev.galasa.zossecurity.internal.ZosSecurityImpl;
import dev.galasa.zossecurity.internal.ZosSecurityImpl.ResourceType;

public class ZosCicsSharedClassSetImpl implements IZosCicsClassSet {
	
	private final ZosSecurityImpl zosSecurity;
	private static final Log logger = LogFactory.getLog(ZosCicsSharedClassSetImpl.class);

	private final String name;
	private final IZosImage image;
	
	private final String prefix;

	public ZosCicsSharedClassSetImpl(ZosSecurityImpl zosSecurity, String name) throws ZosSecurityManagerException {

		this.zosSecurity = zosSecurity;
		this.name = name;
		this.image = zosSecurity.getZosImage();
		this.prefix = zosSecurity.getRunName();
	}
	
	@Override
	public void free() throws ZosSecurityManagerException {
		zosSecurity.dssFree(ResourceType.ZOS_CICS_CLASS_SET.getName(), getName());
		logger.debug("zOS CICS Class Set '" + getName() + "' was freed");
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public IZosImage getZosImage() {
		return this.image;
	}

	@Override
	public HashMap<String, String> getSIT() {
		HashMap<String, String> sits = new HashMap<String, String>();
		for (ZosCicsClassResource resource : ZosCicsClassResource.values()) {
			sits.put(resource.getSIT(), getSIT(resource));
		}		
		sits.put("SECPRFX", prefix);		
		return sits;
	}

	@Override
	public HashMap<String, String> getSIT(int cicsRelease) {
		HashMap<String, String> sits = new HashMap<String, String>();
		for (ZosCicsClassResource resource : ZosCicsClassResource.values()) {
			if (resource.getMinCicsLevel() <= cicsRelease) {
				sits.put(resource.getSIT(), getSIT(resource));
			}
		}
		
		sits.put("SECPRFX", prefix);
		
		return sits;
	}

	@Override
	public String getSIT(ZosCicsClassResource type) {
		return name + type.getSuffix();
	}
	
	@Override
	public List<IZosProfile> allowAllAccess() throws ZosSecurityManagerException {
		return allowAllAccess(true);
	}

	@Override
	public List<IZosProfile> allowAllAccess(boolean refresh) throws ZosSecurityManagerException {
		List<IZosProfile> profiles = new ArrayList<IZosProfile>();

		int count = ZosCicsClassResource.values().length;
		for(ZosCicsClassResource resource : ZosCicsClassResource.values()) {
			count--;
			if (count <= 0 && refresh) {
				profiles.add(defineMemberProfile(resource, "*", RACFAccessType.ALTER, true));
			} else {
				profiles.add(defineMemberProfile(resource, "*", RACFAccessType.ALTER, false));
			}
		}
		return profiles;
	}

	@Override
	public IZosCicsProfile defineMemberProfile(ZosCicsClassResource classType, 
			String profileName,
			RACFAccessType uacc) throws ZosSecurityManagerException {
		return defineMemberProfile(classType, profileName, uacc, true);
	}

	@Override
	public IZosCicsProfile defineMemberProfile(ZosCicsClassResource classType, String profileName, RACFAccessType uacc, boolean refresh) throws ZosSecurityManagerException {		
		String actualProfileName = this.prefix + "." + profileName;
		
		ZosCicsProfileImpl profile = ZosCicsProfileImpl.createProfile(
				zosSecurity,
				classType.getClassName(this.name, ZosCicsClassType.MEMBER),
				classType.getClassName(this.name, ZosCicsClassType.MEMBER),
				actualProfileName,
				image,
				classType,
				ZosCicsClassType.MEMBER,
				uacc,
				refresh);
		
		if (profile == null) {
			throw new ZosSecurityManagerException("Profile " + classType.getClassName(this.name, ZosCicsClassType.MEMBER) + "/" + actualProfileName + " is already in use");
		}
		
		logger.debug("zOS security profile '" + profile.getClassProfileName() + "' was created");
		return profile;
	}

	@Override
	public IZosProfile defineGroupProfile(ZosCicsClassResource classType, String profileName, RACFAccessType uacc,
			List<String> members) throws ZosSecurityManagerException {
		return defineGroupProfile(classType, profileName, uacc, members, true);
	}

	@Override
	public IZosProfile defineGroupProfile(ZosCicsClassResource classType, String profileName, RACFAccessType uacc, List<String> members, boolean refresh) throws ZosSecurityManagerException {
		String actualProfileName = this.prefix + "." + profileName;
		
		ZosCicsProfileImpl profile = ZosCicsProfileImpl.createProfile(
				zosSecurity,
				classType.getClassName(this.name, ZosCicsClassType.GROUPING),
				classType.getClassName(this.name, ZosCicsClassType.MEMBER),
				actualProfileName,
				image,
				classType,
				ZosCicsClassType.GROUPING,
				uacc,
				false);
		
		if (profile == null) {
			throw new ZosSecurityManagerException("Profile " + classType.getClassName(this.name, ZosCicsClassType.GROUPING) + "/" + actualProfileName + " is already in use");
		}
		
		if (members != null) {
			for(String member : members) {
				profile.addMember(member, false, false);
			}
		}
		
		zosSecurity.addClassToBeRefreshed(image.getSysplexID(), profile.getRefreshClassName());
		if (refresh) {
			zosSecurity.refreshClasses(image.getSysplexID());
		}
		
		if (zosSecurity.isResourceReporting()) {
			String listProfile = profile.list();
			if (!zosSecurity.isOutputReporting()) {
				logger.debug("Updated RLIST of " + profile.getClassProfileName() + "' \n" + listProfile);
			}
		}

		logger.debug("zOS security profile '" + profile.getClassProfileName() + "' was created");
		
		return profile;
	}

	@Override
	public void deleteProfile(IZosCicsProfile profile) throws ZosSecurityManagerException {
		deleteProfile(profile, true);
	}

	@Override
	public void deleteProfile(IZosCicsProfile profile, boolean refresh) throws ZosSecurityManagerException {
		profile.delete(refresh);
	}

	public static IZosCicsClassSet allocateClassset(ZosSecurityImpl zosSecurity, IZosImage image) throws ZosSecurityManagerException {
		String cicsClassSetName = zosSecurity.getCicsClassSetFromPool();
		
		ZosCicsSharedClassSetImpl cicsClassSet = new ZosCicsSharedClassSetImpl(zosSecurity, cicsClassSetName);

		logger.debug("zOS Shared CICS Class Set '" + cicsClassSetName + "'  was allocated to this run");

		return cicsClassSet;
	}
	
	@Override
	public String getSecprfx() throws ZosSecurityManagerException {
		return this.prefix;
	}

	@Override
	public String toString() {
		return "[zOS Security CICS Shared Class Set] " + getName();
	}
}
