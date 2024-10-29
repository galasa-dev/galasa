/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.resources;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;

import dev.galasa.zos.IZosImage;
import dev.galasa.zossecurity.IZosPreDefinedProfile;
import dev.galasa.zossecurity.IZosUserid;
import dev.galasa.zossecurity.ZosSecurityManagerException;
import dev.galasa.zossecurity.datatypes.RACFAccessType;
import dev.galasa.zossecurity.internal.ZosSecurityImpl;
import dev.galasa.zossecurity.internal.ZosSecurityImpl.HttpMethod;
import dev.galasa.zossecurity.internal.ZosSecurityImpl.ResourceType;
import dev.galasa.zossecurity.internal.resources.RacfOutputProcessing.COMMAND;

public class ZosPreDefinedProfileImpl implements IZosPreDefinedProfile {

	private final ZosSecurityImpl zosSecurity;
	private final IZosImage image;
	private final String classname;
	private final String profile;

	private static final Log logger = LogFactory.getLog(ZosPreDefinedProfileImpl.class);

	private final HashSet<String> predefinedUserids = new HashSet<String>();

	private final HashMap<String, ZosPredefinedProfilePermitImpl> newUserids = new HashMap<String, ZosPredefinedProfilePermitImpl>();

	private final Map<String, String> zosSecurityServerQueryParams = new HashMap<String, String>();

	private final Pattern patternAuthTitle  = Pattern.compile("\\QUSER      ACCESS\\E");
	private final Pattern patternAuthTitle2 = Pattern.compile("\\Q----      ------\\E");
	private final Pattern patternAuthUser   = Pattern.compile("(\\w+)\\s+\\w+");


	public ZosPreDefinedProfileImpl(ZosSecurityImpl zosSecurity, IZosImage image, String classname, String profile) throws ZosSecurityManagerException {
		this.zosSecurity = zosSecurity;
		this.image = image;
		this.classname = classname;
		this.profile = profile;

		zosSecurityServerQueryParams.put("runid", zosSecurity.getRunName());

		retrievePredefinedUserids();
	}

	private void retrievePredefinedUserids() throws ZosSecurityManagerException {
		try {
			String output = list();

			boolean foundTitle = false;
			boolean foundTitle2 = false;
			BufferedReader br = new BufferedReader(new StringReader(output));
			String line = null;
			while((line = br.readLine()) != null) {
				line = line.trim();
				if (!foundTitle) {
					Matcher matcher = patternAuthTitle.matcher(line);
					if (matcher.matches()) {
						foundTitle = true;
					}
				} else if (!foundTitle2) {
					Matcher matcher = patternAuthTitle2.matcher(line);
					if (matcher.matches()) {
						foundTitle2 = true;
					}
				} else {
					if (line.isEmpty()) {
						break;
					}
					Matcher matcher = patternAuthUser.matcher(line);
					if (matcher.find()) {
						String userid = matcher.group(1);
						predefinedUserids.add(userid);
					} else {
						break;
					}
				}
			}
			br.close();
		} catch (Exception e) {
			throw new ZosSecurityManagerException("Extract of userids for predefined profile failed", e);
		}
	}

	@Override
	public String getClassName() {
		return this.classname;
	}

	@Override
	public String getName() {
		return this.profile;
	}

	@Override
	public void setAccess(IZosUserid userid, RACFAccessType access) throws ZosSecurityManagerException {
		setAccess(userid, access, true);
	}

	@Override
	public void setAccess(IZosUserid userid, RACFAccessType access, boolean refresh)
			throws ZosSecurityManagerException {
		permitUserid(userid.getUserid(), access, refresh);
	}
	
	protected String getClassProfileNames() {
		return classname + "/" + profile;
	}

	public void permitUserid(String userid,
			RACFAccessType access,
			boolean refresh) throws ZosSecurityManagerException {

		if (userid == null || userid.trim().isEmpty()) {
			throw new ZosSecurityManagerException("The userid name is missing");
		}
		if (image == null) {
			throw new ZosSecurityManagerException("The image is missing");
		}

		userid = userid.trim();
		
		ZosPredefinedProfilePermitImpl permit = new ZosPredefinedProfilePermitImpl(zosSecurity, classname, profile, userid);
		newUserids.put(userid, permit);
		
		zosSecurity.dssRegister(ResourceType.ZOS_PRE_DEFINED_PROFILE_PERMIT.getName(), permit.getName());

		if (access == null) {
			access = RACFAccessType.NONE;
		}
		
		StringBuilder command = new StringBuilder();

		command.append("ID(");
		command.append(userid);
		command.append(") ACCESS(");
		command.append(access.toString());
		command.append(") ");

		try {
			JsonObject jsonBody = new JsonObject();
			jsonBody.addProperty("parameters", command.toString());

			JsonObject response = zosSecurity.clientRequest(image.getSysplexID(), HttpMethod.PUT, "/api/profile/" + getClassProfileNames() + "/permit", zosSecurityServerQueryParams, jsonBody);
			RacfOutputProcessing.analyseOutput(response, COMMAND.PERMIT, getClassProfileNames(), zosSecurity.isOutputReporting());

			zosSecurity.addClassToBeRefreshed(image.getSysplexID(), this.classname);
			if (refresh) {
				zosSecurity.refreshClasses(image.getSysplexID());
			}

			if (zosSecurity.isResourceReporting()) {
				String listProfile = list();
				if (!zosSecurity.isOutputReporting()) {
					logger.debug("Updated RLIST of " + getClassProfileNames() + "' \n" + listProfile);
				}
			}
		} catch (ZosSecurityManagerException e) {
			throw new ZosSecurityManagerException("PERMIT of " + getClassProfileNames() + " failed", e);
		}
	}

	public String list() throws ZosSecurityManagerException {
		try {
			HashMap<String, String> rlQueryParams = new HashMap<String, String>(zosSecurityServerQueryParams);
			rlQueryParams.put("authuser", "true");

			JsonObject response = zosSecurity.clientRequest(image.getSysplexID(), HttpMethod.GET, "/api/profile/" + getClassProfileNames(), rlQueryParams, null);
			JsonObject jsonResponse = RacfOutputProcessing.analyseOutput(response, COMMAND.RLIST, getClassProfileNames(), zosSecurity.isOutputReporting());
			return jsonResponse.get("output").getAsString();	
		} catch (ZosSecurityManagerException e) {
			throw new ZosSecurityManagerException("RLIST of " + getClassProfileNames() + " failed", e);
		}
	}

	@Override
	public String toString() {
		return "[zOS Security Pre-defined Profile] " + getName();
	}
}
