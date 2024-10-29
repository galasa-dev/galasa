/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;

import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosfile.IZosDataset;
import dev.galasa.zosfile.IZosDataset.DatasetDataType;
import dev.galasa.zosfile.IZosDataset.RecordFormat;
import dev.galasa.zosfile.IZosDataset.SpaceUnit;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.ZosDatasetException;
import dev.galasa.zossecurity.IZosCertificate;
import dev.galasa.zossecurity.IZosKeyring;
import dev.galasa.zossecurity.ZosSecurityManagerException;
import dev.galasa.zossecurity.datatypes.RACFCertificateTrust;
import dev.galasa.zossecurity.datatypes.RACFCertificateType;
import dev.galasa.zossecurity.internal.ZosSecurityImpl;
import dev.galasa.zossecurity.internal.ZosSecurityImpl.HttpMethod;
import dev.galasa.zossecurity.internal.ZosSecurityImpl.ResourceType;
import dev.galasa.zossecurity.internal.resources.RacfOutputProcessing.COMMAND;

public class ZosCertificateImpl implements IZosCertificate {

	private final ZosSecurityImpl zosSecurity;
	private final String userid;
	private final RACFCertificateType type;
	private final String label;
	private final String sysplexId;
	private final String runName;
	private final IZosImage image;

	private final Map<String, String> zosSecurityServerQueryParams = new HashMap<String, String>();

	private static final Log logger = LogFactory.getLog(ZosCertificateImpl.class);

	public ZosCertificateImpl(ZosSecurityImpl zosSecurity, RACFCertificateType type, String userid, String label, IZosImage image) {
		this.zosSecurity = zosSecurity;
		this.type = type;
		this.userid = userid;
		this.label = label;
		this.image = image;
		this.sysplexId = image.getSysplexID();
		this.runName = zosSecurity.getRunName();

		zosSecurityServerQueryParams.put("runid", this.runName);
	}

	public ZosCertificateImpl(ZosSecurityImpl zosSecurity, String type, String userid, String label, String sysplexId, String runName) {
		this.zosSecurity = zosSecurity;
		this.type = RACFCertificateType.valueOf(type);
		this.userid = userid;
		this.label = label;
		this.image = null;
		this.sysplexId = sysplexId;
		this.runName = runName;

		zosSecurityServerQueryParams.put("runid", this.runName);
	}

	@Override
	public void free() throws ZosSecurityManagerException {
		zosSecurity.dssFree(ResourceType.ZOS_CERTIFICATE.getName(), getTypeUseridLabel());
		logger.debug("zOS Certificate '" + getTypeUseridLabel() + "' was freed");
	}

	@Override
	public String getUserid() {
		return this.userid;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

	private String getTypeUseridLabel() {
		return this.type.toString() + "/" + this.userid + "/" + this.label;
	}

	@Override
	public void delete() throws ZosSecurityManagerException {
		try {
			JsonObject response = zosSecurity.clientRequest(sysplexId, HttpMethod.DELETE, "/api/certificate/" + getTypeUseridLabel(), zosSecurityServerQueryParams, null);
			RacfOutputProcessing.analyseOutput(response, COMMAND.RACDCERT_DELETE, getTypeUseridLabel(), zosSecurity.isOutputReporting());
		} catch (ZosSecurityManagerException e) {
			throw new ZosSecurityManagerException("RACDCERT DELETE of " + getTypeUseridLabel() + " failed", e);
		}
		zosSecurity.dssUnregister(ResourceType.ZOS_CERTIFICATE.getName(), getTypeUseridLabel(), sysplexId, runName);
	}

	@Override
	public void connectKeyring(IZosKeyring keyring) throws ZosSecurityManagerException {
		keyring.connectCertificate(this);
	}

	@Override
	public void connectKeyring(IZosKeyring keyring, boolean defaultCertificate) throws ZosSecurityManagerException {
		keyring.connectCertificate(this, defaultCertificate);
	}

	@Override
	public void connectKeyring(IZosKeyring keyring, boolean defaultCertificate, RACFCertificateType usage)
			throws ZosSecurityManagerException {
		keyring.connectCertificate(this, defaultCertificate, usage);
	}

	@Override
	public void removeKeyring(IZosKeyring keyring) throws ZosSecurityManagerException {
		keyring.removeCertificate(this);
	}


	public static IZosCertificate createCertificate(ZosSecurityImpl zosSecurity, IZosImage image, String userid, String label, KeyStore keyStore, String password, RACFCertificateType type, RACFCertificateTrust trust, int certificateStoreNumber) throws ZosSecurityManagerException {

		if (userid == null || userid.trim().isEmpty()) {
			throw new ZosSecurityManagerException("The userid is missing");
		}
		if (label == null || label.trim().isEmpty()) {
			throw new ZosSecurityManagerException("The label is missing");
		}
		if (image == null) {
			throw new ZosSecurityManagerException("The image is missing");
		}

		if (type == null) {
			type = RACFCertificateType.NONE;
		}

		userid = userid.trim();
		label = label.trim();

		Properties resourceProperties = new Properties();
		resourceProperties.put("certificateType", type.toString());
		resourceProperties.put("core.generic.resource.cleanup.ignore","true");
		StringWriter sw = new StringWriter();
		try {
			resourceProperties.store(sw, null);
		} catch(Exception e) {
			throw new ZosSecurityManagerException("Error creating zossec certificate resource properties", e);
		}

		ZosCertificateImpl certificate = new ZosCertificateImpl(zosSecurity, type, userid, label, image);
		zosSecurity.dssRegister(ResourceType.ZOS_CERTIFICATE.getName(), type.toString() + "/" + userid + "/" + label);

		certificate.createCertificateInRACF(keyStore, password, type, trust, certificateStoreNumber);

		return certificate;
	}

	private void createCertificateInRACF(KeyStore keyStore, String password, RACFCertificateType type, RACFCertificateTrust trust, int certificateStoreNumber) throws ZosSecurityManagerException {

		IZosFileHandler fileHandler = zosSecurity.getZosFileHandler();
		IZosDataset certDsn = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			keyStore.store(baos, password.toCharArray());

			certDsn = fileHandler.newDataset(zosSecurity.getRunDatasetHLQ(image) + "." + zosSecurity.getRunName() + ".C" + certificateStoreNumber + ".P12", image);
			certDsn.setDataType(DatasetDataType.BINARY);
			certDsn.setRecordFormat(RecordFormat.VARIABLE_BLOCKED);
			certDsn.setRecordlength(84);
			certDsn.setBlockSize(0);
			certDsn.setSpace(SpaceUnit.TRACKS, 5, 1);
			if (certDsn.exists()) {
				certDsn.delete();
			}
			certDsn.create();
			certDsn.storeBinary(baos.toByteArray());
			
			logger.info("Certificate stored in dataset '" + certDsn.getName() + "'");

		} catch(ZosManagerException | KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new ZosSecurityManagerException("Unable to store certificate on the image",e);
		}


		StringBuilder command = new StringBuilder();

		if (trust != null) {
			command.append(trust.toString());
			command.append(" ");
		}


		try {
			JsonObject jsonBody = new JsonObject();
			jsonBody.addProperty("parameters", command.toString());
			jsonBody.addProperty("dsn", certDsn.getName());
			jsonBody.addProperty("password", password);

			JsonObject response = zosSecurity.clientRequest(sysplexId, HttpMethod.POST, "/api/certificate/" + getTypeUseridLabel(), zosSecurityServerQueryParams, jsonBody);
			RacfOutputProcessing.analyseOutput(response, COMMAND.RACDCERT_ADD, getTypeUseridLabel(), zosSecurity.isOutputReporting());

			if (zosSecurity.isResourceReporting()) {
				String listProfile = list();
				if (!zosSecurity.isOutputReporting()) {
					logger.debug("Updated RLIST of " + getTypeUseridLabel() + " \n" + listProfile);
				}
			}
			
			try {
				certDsn.delete();
			} catch (ZosDatasetException e) {
				throw new ZosSecurityManagerException("Unable to delete certificate dataset the image", e);
			}
			logger.info("Certificate dataset '" + certDsn.getName() + "' deleted");
		} catch (ZosSecurityManagerException e) {
			throw new ZosSecurityManagerException("RDEFINE of " + getTypeUseridLabel() + " failed", e);
		}
	}

	@Override
	public String list() throws ZosSecurityManagerException {
		try {
			JsonObject response = zosSecurity.clientRequest(sysplexId, HttpMethod.GET, "/api/certificate/" + getTypeUseridLabel(), zosSecurityServerQueryParams, null);
			JsonObject jsonResponse = RacfOutputProcessing.analyseOutput(response, COMMAND.RACDCERT_LIST, getTypeUseridLabel(), zosSecurity.isOutputReporting());
			return jsonResponse.get("output").getAsString();	
		} catch (ZosSecurityManagerException e) {
			throw new ZosSecurityManagerException("RACDCERT LIST of " + getTypeUseridLabel() + " failed", e);
		}
	}

	@Override
	public RACFCertificateType getType() {
		return this.type;
	}
	
	@Override
	public String toString() {
		return "[zOS Security Certificate] " + getTypeUseridLabel();
	}
}
