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
import dev.galasa.zossecurity.IZosCertificate;
import dev.galasa.zossecurity.IZosKeyring;
import dev.galasa.zossecurity.ZosSecurityManagerException;
import dev.galasa.zossecurity.datatypes.RACFCertificateType;
import dev.galasa.zossecurity.internal.ZosSecurityImpl;
import dev.galasa.zossecurity.internal.ZosSecurityImpl.HttpMethod;
import dev.galasa.zossecurity.internal.ZosSecurityImpl.ResourceType;
import dev.galasa.zossecurity.internal.resources.RacfOutputProcessing.COMMAND;

public class ZosKeyringImpl implements IZosKeyring {

	private final ZosSecurityImpl zosSecurity;
	private final String userid;
	private final String label;
	private final String sysplexId;
	private final String runName;

	private final Map<String, String> zosSecurityServerQueryParams = new HashMap<String, String>();

	private static final Log logger = LogFactory.getLog(ZosKeyringImpl.class);

	public ZosKeyringImpl(ZosSecurityImpl zosSecurity, String userid, String label, IZosImage image) {
		this.zosSecurity = zosSecurity;
		this.userid = userid;
		this.label = label;
		this.sysplexId = image.getSysplexID();
		this.runName = zosSecurity.getRunName();

		zosSecurityServerQueryParams.put("runid", this.runName);
	}

	public ZosKeyringImpl(ZosSecurityImpl zosSecurity, String userid, String label, String sysplexId, String runName) {
		this.zosSecurity = zosSecurity;
		this.userid = userid;
		this.label = label;
		this.sysplexId = sysplexId;
		this.runName = runName;

		zosSecurityServerQueryParams.put("runid", this.runName);
	}

	@Override
	public void free() throws ZosSecurityManagerException {
		zosSecurity.dssFree(ResourceType.ZOS_KEYRING.getName(), getUseridLabel());
		logger.debug("zOS Keyring '" + getUseridLabel() + "' was freed");
	}

	@Override
	public String toString() {
		return "[zOS Security Keyring] " + getUseridLabel();
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
	public void delete() throws ZosSecurityManagerException {
		try {
			JsonObject response = zosSecurity.clientRequest(sysplexId, HttpMethod.DELETE, "/api/keyring/" + getUseridLabel(), zosSecurityServerQueryParams, null);
			RacfOutputProcessing.analyseOutput(response, COMMAND.RACDCERT_DELRING, getUseridLabel(), zosSecurity.isOutputReporting());
		} catch (ZosSecurityManagerException e) {
			throw new ZosSecurityManagerException("RACDCERT DELRING of " + getUseridLabel() + " failed", e);
		}
		this.zosSecurity.dssUnregister(ResourceType.ZOS_KEYRING.getName(), getUseridLabel(), sysplexId, runName);
	}

	public void connectCertificate(String userid, String label, RACFCertificateType type, boolean defaultCertificate, RACFCertificateType usage)	throws ZosSecurityManagerException {
		try {
			StringBuilder sb = new StringBuilder();
			
			switch(type) {
			case NONE:
			case PERSONAL:
				sb.append("ID(");
				sb.append(userid);
				sb.append(") ");
				break;
			case CERTAUTH:
			case SITE:
				sb.append(type.toString());
				sb.append(" ");
				break;
			}
			
			sb.append("LABEL('");
			sb.append(label);
			sb.append("') ");
			
			if (defaultCertificate) {
				sb.append("DEFAULT ");
			}
			
			if (usage != null) {
				if (usage == RACFCertificateType.NONE) {
					usage = RACFCertificateType.PERSONAL;
				}
				sb.append("USAGE(");
				sb.append(usage.toString());
				sb.append(") ");
			}
			
			
			JsonObject jsonBody = new JsonObject();
			jsonBody.addProperty("parameters", sb.toString());

			JsonObject response = zosSecurity.clientRequest(sysplexId, HttpMethod.POST, "/api/keyring/" + getUseridLabel() + "/certificate", zosSecurityServerQueryParams, jsonBody);
			RacfOutputProcessing.analyseOutput(response, COMMAND.RACDCERT_CONNECT, getUseridLabel(), zosSecurity.isOutputReporting());
			
			if (response.get("output").getAsString().contains("IRRD107I")) {
				throw new ZosSecurityManagerException("The certificate was not found so could not be connected to the keyring");
			}
			
			if (zosSecurity.isResourceReporting()) {
				String listRing = list();
				if (!zosSecurity.isOutputReporting()) {
					logger.debug("Updated RACDCERT LISTRING of '" + getUseridLabel() + "' \n" + listRing);
				}
			}
		} catch (ZosSecurityManagerException e) {
			throw new ZosSecurityManagerException("RACDCERT CONNECT of " + getUseridLabel() + " failed", e);
		}
	}

	@Override
	public void connectCertificate(IZosCertificate certificate, boolean defaultCertificate, RACFCertificateType usage)
			throws ZosSecurityManagerException {
		connectCertificate(certificate.getUserid(), certificate.getLabel(), certificate.getType(), defaultCertificate, usage);
	}

	@Override
	public void connectCertificate(IZosCertificate certificate, boolean defaultCertificate)
			throws ZosSecurityManagerException {
		connectCertificate(certificate.getUserid(), certificate.getLabel(), certificate.getType(), defaultCertificate, null);
	}

	@Override
	public void connectCertificate(String userid, String label, boolean defaultCertificate)
			throws ZosSecurityManagerException {
		connectCertificate(userid, label, RACFCertificateType.NONE, defaultCertificate, null);
	}


	@Override
	public void connectCertificate(IZosCertificate certificate) throws ZosSecurityManagerException {
		connectCertificate(certificate.getUserid(), certificate.getLabel(), certificate.getType(), false, null);
	}

	@Override
	public void connectCertificate(String userid, String label) throws ZosSecurityManagerException {
		connectCertificate(userid, label, RACFCertificateType.NONE, false, null);
	}

	public void removeCertificate(String userid, String label, RACFCertificateType type) throws ZosSecurityManagerException {
		try {
			HashMap<String, String> rcQueryParams = new HashMap<String, String>(zosSecurityServerQueryParams);

			StringBuilder sb = new StringBuilder();
			
			switch(type) {
			case NONE:
			case PERSONAL:
				rcQueryParams.put("type", "ID(" + userid + ")");
				break;
			case CERTAUTH:
			case SITE:
				rcQueryParams.put("type", type.toString());
				break;
			}
			
			rcQueryParams.put("label", label);
			
			JsonObject jsonBody = new JsonObject();
			jsonBody.addProperty("parameters", sb.toString());

			JsonObject response = zosSecurity.clientRequest(sysplexId, HttpMethod.DELETE, "/api/keyring/" + getUseridLabel() + "/certificate", rcQueryParams, null);
			RacfOutputProcessing.analyseOutput(response, COMMAND.RACDCERT_REMOVE, getUseridLabel(), zosSecurity.isOutputReporting());
			
			if (zosSecurity.isResourceReporting()) {
				String listRing = list();
				if (!zosSecurity.isOutputReporting()) {
					logger.debug("Updated RACDCERT LISTRING of '" + getUseridLabel() + "' \n" + listRing);
				}
			}
		} catch (ZosSecurityManagerException e) {
			throw e;
		} catch (Exception e) {
			throw new ZosSecurityManagerException("RACDCERT REMOVE of " + getUseridLabel() + " failed", e);
		}
	}

	@Override
	public void removeCertificate(IZosCertificate certificate) throws ZosSecurityManagerException {
		removeCertificate(certificate.getUserid(), certificate.getLabel(), certificate.getType());
	}

	@Override
	public void removeCertificate(String userid, String label) throws ZosSecurityManagerException {
		removeCertificate(userid, label, RACFCertificateType.NONE);
	}

	public static IZosKeyring createKeyring(ZosSecurityImpl zosSecurity, IZosImage image, String userid, String label) throws ZosSecurityManagerException {

		if (userid == null || userid.trim().isEmpty()) {
			throw new ZosSecurityManagerException("The userid is missing");
		}
		if (label == null || label.trim().isEmpty()) {
			throw new ZosSecurityManagerException("The label is missing");
		}
		if (image == null) {
			throw new ZosSecurityManagerException("The image is missing");
		}

		userid = userid.trim();
		label = label.trim();
		
		ZosKeyringImpl keyring = new ZosKeyringImpl(zosSecurity, userid, label, image);
		zosSecurity.dssRegister(ResourceType.ZOS_KEYRING.getName(), userid + "/" + label);

		keyring.createKeyringInRACF();

		return keyring;
	}

	private void createKeyringInRACF() throws ZosSecurityManagerException {
		try {
			JsonObject response = zosSecurity.clientRequest(sysplexId, HttpMethod.POST, "/api/keyring/" + getUseridLabel(), zosSecurityServerQueryParams, null);
			RacfOutputProcessing.analyseOutput(response, COMMAND.RACDCERT_ADDRING, getUseridLabel(), zosSecurity.isOutputReporting());

			if (zosSecurity.isResourceReporting()) {
				String listKeyring = list();
				if (!zosSecurity.isOutputReporting()) {
					logger.debug("Updated RACDCERT ADDRING of " + getUseridLabel() + "' \n" + listKeyring);
				}
			}
		} catch (ZosSecurityManagerException e) {
			throw new ZosSecurityManagerException("RACDCERT ADDRING of " + getUseridLabel() + " failed", e);
		}
	}

	@Override
	public String list() throws ZosSecurityManagerException {
		try {
			JsonObject response = zosSecurity.clientRequest(sysplexId, HttpMethod.GET, "/api/keyring/" + getUseridLabel(), zosSecurityServerQueryParams, null);
			RacfOutputProcessing.analyseOutput(response, COMMAND.RACDCERT_LISTRING, getUseridLabel(), zosSecurity.isOutputReporting());
			return response.get("output").getAsString();	
		} catch (ZosSecurityManagerException e) {
			throw new ZosSecurityManagerException("RACDCERT LISTRING of " + getUseridLabel() + " failed", e);
		}
	}
}
