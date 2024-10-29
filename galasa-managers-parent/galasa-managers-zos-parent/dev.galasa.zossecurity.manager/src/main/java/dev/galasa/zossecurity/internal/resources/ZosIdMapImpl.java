/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.resources;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;

import dev.galasa.zos.IZosImage;
import dev.galasa.zossecurity.IZosIdMap;
import dev.galasa.zossecurity.ZosSecurityManagerException;
import dev.galasa.zossecurity.internal.ZosSecurityImpl;
import dev.galasa.zossecurity.internal.ZosSecurityImpl.HttpMethod;
import dev.galasa.zossecurity.internal.ZosSecurityImpl.ResourceType;
import dev.galasa.zossecurity.internal.resources.RacfOutputProcessing.COMMAND;

public class ZosIdMapImpl implements IZosIdMap {

	private final ZosSecurityImpl zosSecurity;
	private final String userid;
	private final String label;
	private final String distributedID;
	private final String registry;
	private final String sysplexId;
	private final String runName;

	private final Map<String, String> zosSecurityServerQueryParams = new HashMap<String, String>();

	private static final Log logger = LogFactory.getLog(ZosIdMapImpl.class);

	public ZosIdMapImpl(ZosSecurityImpl zosSecurity, String userid, String label, String distributedID, String registry, IZosImage image) {
		this.zosSecurity = zosSecurity;
		this.userid = userid;
		this.label = label;
		this.distributedID = distributedID;
		this.registry = registry;
		this.sysplexId = image.getSysplexID();
		this.runName = zosSecurity.getRunName();

		zosSecurityServerQueryParams.put("runid", zosSecurity.getRunName());
	}

	public ZosIdMapImpl(ZosSecurityImpl zosSecurity, String userid, String label, String sysplexId, String runName) {
		this.zosSecurity = zosSecurity;
		this.userid = userid;
		this.label = label;
		this.distributedID = null;
		this.registry = null;
		this.sysplexId = sysplexId;
		this.runName = runName;

		zosSecurityServerQueryParams.put("runid", zosSecurity.getRunName());
	}

	@Override
	public void free() throws ZosSecurityManagerException {
		zosSecurity.dssFree(ResourceType.ZOS_ID_MAP.getName(), getUseridLabel());
		logger.debug("zOS IDMAP '" + getUseridLabel() + "' was freed");
	}

	@Override
	public String toString() {
		return "[zOS Security ID Map] " + getUseridLabel();
	}

	public String getUseridLabel() {
		return this.userid + "/" + this.label;
	}

	@Override
	public String getUserid() {
		return this.userid;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

	@Override
	public String getDistributedID() {
		return this.distributedID;
	}

	@Override
	public String getRegistry() {
		return this.registry;
	}

	@Override
	public void delete() throws ZosSecurityManagerException {
		try {
			JsonObject response = zosSecurity.clientRequest(sysplexId, HttpMethod.DELETE, "/api/idmap/" + getUseridLabel(), zosSecurityServerQueryParams, null);
			RacfOutputProcessing.analyseOutput(response, COMMAND.RACMAP_DELMAP, getUseridLabel(), zosSecurity.isOutputReporting());
			
			zosSecurity.addClassToBeRefreshed(sysplexId, "IDIDMAP");
			zosSecurity.refreshClasses(sysplexId);
		} catch (ZosSecurityManagerException e) {
			throw new ZosSecurityManagerException("RACMAP DELMAP of " + getUseridLabel() + " failed", e);
		}
		zosSecurity.dssUnregister(ResourceType.ZOS_ID_MAP.getName(), getUseridLabel(), sysplexId, runName);
	}
	
	public static IZosIdMap createIdMap(ZosSecurityImpl zosSecurity, IZosImage image, String userid, String label, String distributedID, String registry) throws ZosSecurityManagerException {
		if (userid == null ) {
			throw new ZosSecurityManagerException("The userid is missing");
		}
		if (label == null || label.trim().isEmpty()) {
			throw new ZosSecurityManagerException("The label is missing");
		}
		if (distributedID == null || distributedID.trim().isEmpty()) {
			throw new ZosSecurityManagerException("The distributedID is missing");
		}
		if (registry == null || registry.trim().isEmpty()) {
			throw new ZosSecurityManagerException("The registry is missing");
		}
		if (image == null) {
			throw new ZosSecurityManagerException("The sysplexId is missing");
		}

		label = label.trim();
		distributedID = distributedID.trim();
		registry = registry.trim();

		ZosIdMapImpl idmap = new ZosIdMapImpl(zosSecurity, userid, label, distributedID, registry, image);
		zosSecurity.dssRegister(ResourceType.ZOS_ID_MAP.getName(), userid + "/" + label);

		idmap.createIdMapInRACF();
		
		zosSecurity.addClassToBeRefreshed(image.getSysplexID(), "IDIDMAP");
		zosSecurity.refreshClasses(image.getSysplexID());


		return idmap;
	}
	private void createIdMapInRACF() throws ZosSecurityManagerException {
		StringBuilder command = new StringBuilder();
		
		command.append("USERDIDFILTER(NAME('");
		command.append(this.distributedID);
		command.append("')) ");

		command.append("REGISTRY(NAME('");
		command.append(this.registry);
		command.append("')) ");		
		
		try {
			JsonObject jsonBody = new JsonObject();
			jsonBody.addProperty("parameters", command.toString());

			JsonObject response = zosSecurity.clientRequest(sysplexId, HttpMethod.POST, "/api/idmap/" + getUseridLabel(), zosSecurityServerQueryParams, jsonBody);
			RacfOutputProcessing.analyseOutput(response, COMMAND.RACMAP_MAP, getUseridLabel(), zosSecurity.isOutputReporting());

			if (zosSecurity.isResourceReporting()) {
				String listidmap = list();
				if (!zosSecurity.isOutputReporting()) {
					logger.debug("Updated RACMAP of " + getUseridLabel() + "' \n" + listidmap);
				}
			}
		} catch (ZosSecurityManagerException e) {
			throw e;
		} catch (Exception e) {
			throw new ZosSecurityManagerException("RACMAP MAP of " + getUseridLabel() + " failed", e);
		}
	}
	
	private String list() throws ZosSecurityManagerException {
		try {
			JsonObject response = zosSecurity.clientRequest(sysplexId, HttpMethod.GET, "/api/idmap/" + getUseridLabel(), zosSecurityServerQueryParams, null);
			JsonObject jsonResponse = RacfOutputProcessing.analyseOutput(response, COMMAND.RACMAP_LISTMAP, getUseridLabel(), zosSecurity.isOutputReporting());
			return jsonResponse.get("output").getAsString();	
		} catch (ZosSecurityManagerException e) {
			throw new ZosSecurityManagerException("RACMAP LISTMAP of " + getUseridLabel() + " failed", e);
		}
	}

}
