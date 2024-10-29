/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.zos.IZosImage;
import dev.galasa.zossecurity.IZosCicsClassSet;
import dev.galasa.zossecurity.IZosCicsProfile;
import dev.galasa.zossecurity.IZosProfile;
import dev.galasa.zossecurity.ZosSecurityManagerException;
import dev.galasa.zossecurity.datatypes.RACFAccessType;
import dev.galasa.zossecurity.datatypes.ZosCicsClassResource;
import dev.galasa.zossecurity.datatypes.ZosCicsClassType;
import dev.galasa.zossecurity.internal.ZosSecurityImpl;
import dev.galasa.zossecurity.internal.ZosSecurityImpl.ResourceType;

public class ZosCicsClassSetImpl implements IZosCicsClassSet {

	private final ZosSecurityImpl zosSecurity;
	private static final Log logger = LogFactory.getLog(ZosCicsClassSetImpl.class);

	private final String name;
	private final IZosImage image;
	private final String sysplexId;
	private final String runName;

	private final Map<String, String> zosSecurityServerQueryParams = new HashMap<String, String>();


	public ZosCicsClassSetImpl(ZosSecurityImpl zosSecurity, String name) throws ZosSecurityManagerException {
		this.zosSecurity = zosSecurity;
		this.name = name;
		this.image = zosSecurity.getZosImage();
		this.sysplexId = image.getSysplexID();
		this.runName = zosSecurity.getRunName();

		zosSecurityServerQueryParams.put("runid", zosSecurity.getRunName());
	}
	
	public ZosCicsClassSetImpl(ZosSecurityImpl zosSecurity, String name, String sysplexId, String runName) {
		this.zosSecurity = zosSecurity;
		this.name = name;
		this.image = null;
		this.sysplexId = sysplexId;
		this.runName = runName;
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
		ArrayList<IZosProfile> profiles = new ArrayList<IZosProfile>();

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
	public IZosCicsProfile defineMemberProfile(ZosCicsClassResource classType, String profileName, RACFAccessType uacc) throws ZosSecurityManagerException {
		return defineMemberProfile(classType, profileName, uacc, true);
	}

	@Override
	public IZosCicsProfile defineMemberProfile(ZosCicsClassResource classType, String profileName, RACFAccessType uacc, boolean refresh) throws ZosSecurityManagerException {
		String className = classType.getClassName(this.name, ZosCicsClassType.MEMBER);
		ZosCicsProfileImpl profile = new ZosCicsProfileImpl(
				zosSecurity,
				className,
				className,
				profileName,
				image,
				classType, 
				ZosCicsClassType.MEMBER);

		zosSecurity.dssRegister(ResourceType.ZOS_PROFILE.getName(), className + "/" + profileName);
		
		profile.createProfileInRACF(null, uacc, true, refresh);
		zosSecurity.addClassToBeRefreshed(image.getSysplexID(), profile.getClassName());
		if (refresh) {
			zosSecurity.refreshClasses(image.getSysplexID());
		}

		logger.debug("zOS security profile '" + profile.getClassProfileName() + "' was created");
		return profile;
	}

	@Override
	public IZosProfile defineGroupProfile(ZosCicsClassResource classType, String profileName, RACFAccessType uacc,	List<String> members) throws ZosSecurityManagerException {
		return defineGroupProfile(classType, profileName, uacc, members, true);
	}

	@Override
	public IZosProfile defineGroupProfile(ZosCicsClassResource classType, String profileName, RACFAccessType uacc, List<String> members, boolean refresh) throws ZosSecurityManagerException {
		ZosCicsProfileImpl profile = new ZosCicsProfileImpl(zosSecurity, classType.getClassName(this.name, ZosCicsClassType.GROUPING), classType.getClassName(this.name, ZosCicsClassType.MEMBER), profileName, image, classType, ZosCicsClassType.GROUPING);
		
		profile.createProfileInRACF(null, uacc, false, refresh);
		
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
		profile.delete(true);
	}

	public static ZosCicsClassSetImpl allocateClassset(ZosSecurityImpl zosSecurity) throws ZosSecurityManagerException {
//	TODO - see also ZosUseridImpl#allocateUserId(ZosSecurityImpl zosSecurity)
//		Random random = new Random();
//
//		int minimumFree = 0;
//		if (preallocate) {
//			minimumFree = CicsClassSetMinimumFree.get(zosSecurity.getZosImage());
//		}
		
		String cicsClassSetName = zosSecurity.getCicsClassSetFromPool();
		
		ZosCicsClassSetImpl cicsClassSet = new ZosCicsClassSetImpl(zosSecurity, cicsClassSetName);

		logger.debug("CICS Class Set '" + cicsClassSet.getName() + "' was allocated to this run");

		return cicsClassSet;
	}
	
	@Override
	public String getSecprfx() throws ZosSecurityManagerException {
		throw new ZosSecurityManagerException("SECPRFX is only applicable for Shared CICS Classsets");
	}

	@Override
	public String toString() {
		return "[zOS Security CICS Class Set] " + getName();
	}

	public void delete() throws ZosSecurityManagerException {
		try {
			this.zosSecurity.dssUnregister(ResourceType.ZOS_CICS_CLASS_SET.getName(), getName(), sysplexId, runName);
		} catch (ZosSecurityManagerException e) {
			throw e;
//		} catch (Exception e) {
//			throw new ZosSecurityManagerException("RDELETE of " + getName() + " failed", e);
		}
	}
}
