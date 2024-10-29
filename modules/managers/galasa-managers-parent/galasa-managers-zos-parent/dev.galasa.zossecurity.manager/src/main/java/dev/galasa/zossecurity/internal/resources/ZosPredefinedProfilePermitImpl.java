/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.resources;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import dev.galasa.zossecurity.ZosSecurityManagerException;
import dev.galasa.zossecurity.internal.ZosSecurityImpl;
import dev.galasa.zossecurity.internal.ZosSecurityImpl.HttpMethod;
import dev.galasa.zossecurity.internal.ZosSecurityImpl.ResourceType;
import dev.galasa.zossecurity.internal.resources.RacfOutputProcessing.COMMAND;

public class ZosPredefinedProfilePermitImpl {
	
	private final ZosSecurityImpl zosSecurity;
	private final String classname;
	private final String profile;
	private final String userid;
	private final String sysplexId;
	private final String runName;
	
	private final Map<String, String> zosSecurityServerQueryParams = new HashMap<String, String>();
	
	protected ZosPredefinedProfilePermitImpl(ZosSecurityImpl zosSecurity, String classname, String profile, String userid) throws ZosSecurityManagerException {
		this.zosSecurity = zosSecurity;
		this.classname = classname;
		this.profile = profile;
		this.userid = userid;
		this.sysplexId = zosSecurity.getZosImage().getSysplexID();
		this.runName = zosSecurity.getRunName();
		
		zosSecurityServerQueryParams.put("runid", "CLEANUP");
	}
	
	public ZosPredefinedProfilePermitImpl(ZosSecurityImpl zosSecurity, String classname, String profile, String userid, String sysplexId, String runName) {
		this.zosSecurity = zosSecurity;
		this.classname = classname;
		this.profile = profile;
		this.userid = userid;
		this.sysplexId = sysplexId;
		this.runName = runName;
		
		zosSecurityServerQueryParams.put("runid", "CLEANUP");
	}

	protected String getName() {
		return classname + "/" + profile + "/" + userid;
	}

	@Override
	public String toString() {
		return "[zOS Security Pre-defined Profile Permit] " + getName();
	}
	
	private String getClassProfileName() {
		return classname + "/" + profile;
	}

	public void discard() throws ZosSecurityManagerException {		
		try {
			StringBuilder sb = new StringBuilder();

			sb.append("ID(");
			sb.append(userid);
			sb.append(") DELETE ");
			
			JsonObject jsonBody = new JsonObject();
			jsonBody.addProperty("parameters", sb.toString());
			
			JsonObject response = zosSecurity.clientRequest(sysplexId, HttpMethod.PUT, "/api/profile/" + getClassProfileName() + "/permit", zosSecurityServerQueryParams, jsonBody);
			RacfOutputProcessing.analyseOutput(response, COMMAND.RACDCERT_DELETE, getClassProfileName(), zosSecurity.isOutputReporting());
		} catch (ZosSecurityManagerException e) {
			throw new ZosSecurityManagerException("Failed to discard " + getName(), e);
		}
		zosSecurity.dssUnregister(ResourceType.ZOS_PRE_DEFINED_PROFILE_PERMIT.getName(), getName(), sysplexId, runName);
	}

}
