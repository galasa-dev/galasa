/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;

import dev.galasa.zos.IZosImage;
import dev.galasa.zossecurity.IZosProfile;
import dev.galasa.zossecurity.IZosUserid;
import dev.galasa.zossecurity.ProfileNotFoundException;
import dev.galasa.zossecurity.ZosSecurityManagerException;
import dev.galasa.zossecurity.datatypes.RACFAccessType;
import dev.galasa.zossecurity.internal.ZosSecurityImpl;
import dev.galasa.zossecurity.internal.ZosSecurityImpl.HttpMethod;
import dev.galasa.zossecurity.internal.ZosSecurityImpl.ResourceType;
import dev.galasa.zossecurity.internal.resources.RacfOutputProcessing.COMMAND;

public class ZosProfileImpl implements IZosProfile {

	private final ZosSecurityImpl zosSecurity;
	private final String className;
	private final String refreshClassname;
	private final String profileName;
	private final String sysplexId;
	private final String runName;

	private final Map<String, String> zosSecurityServerQueryParams = new HashMap<String, String>();

	private static final Log logger = LogFactory.getLog(ZosProfileImpl.class);

	protected ZosProfileImpl(ZosSecurityImpl zosSecurity, String className, String profileName, IZosImage image) {
		this.zosSecurity = zosSecurity;
		this.className = className;
		this.refreshClassname = className;
		this.profileName = profileName;
		this.sysplexId = image.getSysplexID();
		this.runName = zosSecurity.getRunName();

		zosSecurityServerQueryParams.put("runid", this.runName);
	}
	
	protected ZosProfileImpl(ZosSecurityImpl zosSecurity, String className, String refreshClassname, String profileName, IZosImage image) {
		this.zosSecurity = zosSecurity;
		this.className = className;
		this.refreshClassname = refreshClassname;
		this.profileName = profileName;
		this.sysplexId = image.getSysplexID();
		this.runName = zosSecurity.getRunName();

		zosSecurityServerQueryParams.put("runid", this.runName);
	}

	public ZosProfileImpl(ZosSecurityImpl zosSecurity, String className, String profileName, String sysplexId, String runName) {
		this.zosSecurity = zosSecurity;
		this.className = className;
		this.refreshClassname = className;
		this.profileName = profileName;
		this.sysplexId = sysplexId;
		this.runName = runName;

		zosSecurityServerQueryParams.put("runid", this.runName);
	}

	@Override
	public void free() throws ZosSecurityManagerException {
		zosSecurity.dssFree(ResourceType.ZOS_PROFILE.getName(), getClassProfileName());
		logger.debug("zOS Profile '" + getClassProfileName() + "' was freed");
	}

	@Override
	public String getClassName() {
		return this.className;
	}

	public String getRefreshClassName() {
		return this.refreshClassname;
	}

	@Override
	public String getName() {
		return this.profileName;
	}

	@Override
	public void alterUacc(RACFAccessType newUacc) throws ZosSecurityManagerException {
		alterUacc(newUacc, true);
	}

	@Override
	public void alterUacc(RACFAccessType newUacc, boolean refresh) throws ZosSecurityManagerException {
		StringBuilder command = new StringBuilder();

		if (newUacc == null) {
			newUacc = RACFAccessType.NONE;
		}

		command.append("UACC(");
		command.append(newUacc.toString());
		command.append(") ");

		try {
			JsonObject jsonBody = new JsonObject();
			jsonBody.addProperty("parameters", command.toString());

			JsonObject response = zosSecurity.clientRequest(sysplexId, HttpMethod.PUT, "/api/profile/" + getClassProfileName(), zosSecurityServerQueryParams, jsonBody);
			RacfOutputProcessing.analyseOutput(response, COMMAND.RALTER, getClassProfileName(), zosSecurity.isOutputReporting());

			zosSecurity.addClassToBeRefreshed(sysplexId, this.refreshClassname);
			if (refresh) {
				zosSecurity.refreshClasses(sysplexId);
			}

			if (zosSecurity.isResourceReporting()) {
				String listProfile = list();
				if (!zosSecurity.isOutputReporting()) {
					logger.debug("Updated RLIST of " + getClassProfileName() + "' \n" + listProfile);
				}
			}
		} catch (ZosSecurityManagerException e) {
			throw new ZosSecurityManagerException("RALTER of " + getClassProfileName() + " failed", e);
		}
	}

	@Override
	public void setAccess(IZosUserid userid, RACFAccessType access) throws ZosSecurityManagerException {
		setAccess(userid.getUserid(), access, true);
	}

	@Override
	public void setAccess(IZosUserid userid, RACFAccessType access, boolean refresh) throws ZosSecurityManagerException {
		setAccess(userid.getUserid(), access, refresh);
	}

	@Override
	public void setAccess(String userid, RACFAccessType access) throws ZosSecurityManagerException {
		setAccess(userid, access, true);
	}

	@Override
	public void setAccess(String userid, RACFAccessType access, boolean refresh) throws ZosSecurityManagerException {
		StringBuilder command = new StringBuilder();

		if (userid == null || userid.trim().isEmpty()) {
			throw new ZosSecurityManagerException("Userid is missing");
		}

		if (access == null) {
			access = RACFAccessType.NONE;
		}

		command.append("ID(");
		command.append(userid);
		command.append(") ACCESS(");
		command.append(access.toString());
		command.append(") ");
		
		try {
			JsonObject jsonBody = new JsonObject();
			jsonBody.addProperty("parameters", command.toString());

			JsonObject response = zosSecurity.clientRequest(sysplexId, HttpMethod.PUT, "/api/profile/" + getClassProfileName() + "/permit", zosSecurityServerQueryParams, jsonBody);
			RacfOutputProcessing.analyseOutput(response, COMMAND.PERMIT, getClassProfileName(), zosSecurity.isOutputReporting());

			zosSecurity.addClassToBeRefreshed(sysplexId, this.refreshClassname);
			if (refresh) {
				zosSecurity.refreshClasses(sysplexId);
			}

			if (zosSecurity.isResourceReporting()) {
				String listProfile = list();
				if (!zosSecurity.isOutputReporting()) {
					logger.debug("Updated RLIST of " + getClassProfileName() + "' \n" + listProfile);
				}
			}
		} catch (ZosSecurityManagerException e) {
			throw e;
		} catch (Exception e) {
			throw new ZosSecurityManagerException("PERMIT of " + getClassProfileName() + " failed", e);
		}
	}

	@Override
	public void addMember(String member) throws ZosSecurityManagerException {
		addMember(member, true);
	}

	@Override
	public void addMember(String member, boolean refresh) throws ZosSecurityManagerException {
		addMember(member, refresh, true);
	}

	protected void addMember(String member, boolean refresh, boolean reporting) throws ZosSecurityManagerException {
		StringBuilder command = new StringBuilder();

		if (member == null || member.trim().isEmpty()) {
			throw new ZosSecurityManagerException("Member is missing");
		}

		command.append("ADDMEM(");
		command.append(member.trim());
		command.append(") ");

		try {
			JsonObject jsonBody = new JsonObject();
			jsonBody.addProperty("parameters", command.toString());

			JsonObject response = zosSecurity.clientRequest(sysplexId, HttpMethod.PUT, "/api/profile/" + getClassProfileName(), zosSecurityServerQueryParams, jsonBody);
			RacfOutputProcessing.analyseOutput(response, COMMAND.RALTER, getClassProfileName(), zosSecurity.isOutputReporting());

			zosSecurity.addClassToBeRefreshed(sysplexId, this.refreshClassname);
			if (refresh) {
				zosSecurity.refreshClasses(sysplexId);
			}

			if (reporting) {
				if (zosSecurity.isResourceReporting()) {
					String listProfile = list();
					if (!zosSecurity.isOutputReporting()) {
						logger.debug("Updated RLIST of " + getClassProfileName() + "' \n" + listProfile);
					}
				}
			}
		} catch (ZosSecurityManagerException e) {
			throw e;
		} catch (Exception e) {
			throw new ZosSecurityManagerException("RALTER of " + getClassProfileName() + " failed", e);
		}
	}

	@Override
	public void delete() throws ZosSecurityManagerException {
		delete(true);
	}

	@Override
	public void delete(boolean refresh) throws ZosSecurityManagerException {
		StringBuilder command = new StringBuilder();

		try {
			JsonObject jsonBody = new JsonObject();
			jsonBody.addProperty("parameters", command.toString());

			JsonObject response = zosSecurity.clientRequest(sysplexId, HttpMethod.DELETE, "/api/profile/" + getClassProfileName(), zosSecurityServerQueryParams, null);
			try {
				RacfOutputProcessing.analyseOutput(response, COMMAND.RDELETE, getClassProfileName(), zosSecurity.isOutputReporting());
			} catch (ProfileNotFoundException e) {
				// Continue
			}

			zosSecurity.addClassToBeRefreshed(sysplexId, this.refreshClassname);
			if (refresh) {
				zosSecurity.refreshClasses(sysplexId);
			}
			
			this.zosSecurity.dssUnregister(ResourceType.ZOS_PROFILE.getName(), getClassProfileName(), sysplexId, runName);
		} catch (ZosSecurityManagerException e) {
			throw e;
		} catch (Exception e) {
			throw new ZosSecurityManagerException("RDELETE of " + getClassProfileName() + " failed", e);
		}
	}

	public String list() throws ZosSecurityManagerException {
		try {
			HashMap<String, String> rlQueryParams = new HashMap<String, String>(zosSecurityServerQueryParams);
			rlQueryParams.put("authuser", "true");

			JsonObject response = zosSecurity.clientRequest(sysplexId, HttpMethod.GET, "/api/profile/" + getClassProfileName(), rlQueryParams, null);
			JsonObject jsonResponse = RacfOutputProcessing.analyseOutput(response, COMMAND.RLIST, getClassProfileName(), zosSecurity.isOutputReporting());
			return jsonResponse.get("output").getAsString();	
		} catch (ZosSecurityManagerException e) {
			throw new ZosSecurityManagerException("RLIST of " + getClassProfileName() + " failed", e);
		}
	}

	public static IZosProfile createProfile(ZosSecurityImpl zosSecurity, 
			IZosImage image, 
			String className, 
			String profileName, 
			Map<String, String> args,
			RACFAccessType uacc, 
			boolean refresh) throws ZosSecurityManagerException {

		if (className == null || className.trim().isEmpty()) {
			throw new ZosSecurityManagerException("The class name is missing");
		}
		if (profileName == null || profileName.trim().isEmpty()) {
			throw new ZosSecurityManagerException("The profile name is missing");
		}
		if (image == null) {
			throw new ZosSecurityManagerException("The image is missing");
		}

		className = className.trim();
		profileName = profileName.trim();

		ZosProfileImpl profile = new ZosProfileImpl(zosSecurity, className, profileName, image);
		zosSecurity.dssRegister(ResourceType.ZOS_PROFILE.getName(), className + "/" + profileName);

		profile.createProfileInRACF(args, uacc, true, refresh);
		return profile;
	}

	protected String getClassProfileName() {
		return this.className + "/" + this.profileName;
	}

	protected void createProfileInRACF(Map<String, String> args, RACFAccessType uacc, boolean reporting, boolean refresh) throws ZosSecurityManagerException {

		StringBuilder command = new StringBuilder();

		if (uacc != null) {
			command.append("UACC(");
			command.append(uacc.toString());
			command.append(") ");
		}

		if (args != null) {
			for(Entry<String, String> arg : args.entrySet()) {
				command.append(arg.getKey());
				command.append("(");
				command.append(arg.getValue());
				command.append(") ");
			}
		}

		try {
			JsonObject jsonBody = new JsonObject();
			jsonBody.addProperty("parameters", command.toString());

			JsonObject response = zosSecurity.clientRequest(sysplexId, HttpMethod.POST, "/api/profile/" + getClassProfileName(), zosSecurityServerQueryParams, jsonBody);
			RacfOutputProcessing.analyseOutput(response, COMMAND.RDEFINE, getClassProfileName(), zosSecurity.isOutputReporting());
			
			if (response.get("output").getAsString().contains("ICH10006I") && reporting && zosSecurity.isResourceReporting()) {
				refresh = true;
			}
			
			zosSecurity.addClassToBeRefreshed(sysplexId, refreshClassname);
			if (refresh) {
				zosSecurity.refreshClasses(sysplexId);
			}

			if (reporting) {
				if (zosSecurity.isResourceReporting()) {
					String listProfile = list();
					if (!zosSecurity.isOutputReporting()) {
						logger.debug("Updated RLIST of " + getClassProfileName() + "' \n" + listProfile);
					}
				}
			}	
		} catch (ZosSecurityManagerException e) {
			throw new ZosSecurityManagerException("RDEFINE of " + getClassProfileName() + " failed", e);
		}
	}

	@Override
	public String toString() {
		return "[zOS Security Profile] " + getClassProfileName();
	}
}
