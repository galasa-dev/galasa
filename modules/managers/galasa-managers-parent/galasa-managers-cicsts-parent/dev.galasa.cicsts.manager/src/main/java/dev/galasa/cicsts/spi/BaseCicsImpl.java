/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.spi;

import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICeci;
import dev.galasa.cicsts.ICeda;
import dev.galasa.cicsts.ICemt;
import dev.galasa.cicsts.MasType;
import dev.galasa.cicsts.cicsresource.CicsJvmserverResourceException;
import dev.galasa.cicsts.cicsresource.ICicsResource;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosfile.IZosUNIXFile;

public abstract class BaseCicsImpl implements ICicsRegionProvisioned {

    protected final ICicstsManagerSpi cicstsManager;
    private final String cicsTag;
    private final String applid;
    private final IZosImage zosImage;
    private final MasType   masType;
    private final String sysid;

    private int lastTerminalId;

    private ICeci ceci;
    private ICeda ceda;
    private ICemt cemt;
    private ICicsResource cicsResource;
	private IZosUNIXFile runTemporaryUNIXPath;

    private static final String SLASH_SYBMOL = "/";

    public BaseCicsImpl(ICicstsManagerSpi cicstsManager, String cicsTag, IZosImage zosImage, String applid, MasType masType) {
        this.cicstsManager = cicstsManager;
        this.cicsTag = cicsTag;
        this.applid = applid;
        this.zosImage = zosImage;
        this.masType  = masType;
        this.sysid = null;
    }
    
    public BaseCicsImpl(ICicstsManagerSpi cicstsManager, String cicsTag, IZosImage zosImage, String applid, MasType masType, String sysid) {
        this.cicstsManager = cicstsManager;
        this.cicsTag = cicsTag;
        this.applid = applid;
        this.zosImage = zosImage;
        this.masType  = masType;
        this.sysid = sysid;
    }

    @Override
    public String getTag() {
        return this.cicsTag;
    }

    @Override
    public String getApplid() {
        return this.applid;
    }
    
    @Override
    public String getSysid() {
        return this.sysid;
    }

    @Override
    public IZosImage getZosImage() {
        return this.zosImage;
    }

    @Override
    public String toString() {
        return "CICS Region[" + this.applid + "]";
    }

    @Override
    public String getNextTerminalId() {
        lastTerminalId++;
        return this.applid + "_" + Integer.toString(lastTerminalId);
    }

    @Override
    public MasType getMasType() {
        return this.masType;
    }

    protected  ICicstsManagerSpi getCicstsManager() {
        return this.cicstsManager;
    }

    @Override
    public ICeci ceci() throws CicstsManagerException {
        if (this.ceci == null) {
            this.ceci = this.cicstsManager.getCeciProvider().getCeci(this);
        }
        return this.ceci;
    }

    @Override
    public ICeda ceda() throws CicstsManagerException {
        if (this.ceda == null) {
            this.ceda = this.cicstsManager.getCedaProvider().getCeda(this);
        }
        return this.ceda;
    }

    @Override
    public ICemt cemt() throws CicstsManagerException {
        if (this.cemt == null) {
            this.cemt = this.cicstsManager.getCemtProvider().getCemt(this);
        }
        return this.cemt;
    }

    @Override
	public ICicsResource cicsResource() throws CicstsManagerException {
        if (this.cicsResource == null) {
            this.cicsResource = this.cicstsManager.getCicsResourceProvider().getCicsResource(this);
        }
        return this.cicsResource;
	}

	@Override
	public IZosUNIXFile getRunTemporaryUNIXDirectory() throws CicstsManagerException {
		if (this.runTemporaryUNIXPath == null) {
	        try {
	            this.runTemporaryUNIXPath = this.cicstsManager.getZosFileHandler().newUNIXFile(getZosImage().getRunTemporaryUNIXPath() + SLASH_SYBMOL + getApplid() + SLASH_SYBMOL, getZosImage());
	            if (!this.runTemporaryUNIXPath.exists()) {
	            	this.runTemporaryUNIXPath.create();
	            	this.runTemporaryUNIXPath.setShouldArchive(false);
	            	this.runTemporaryUNIXPath.setShouldCleanup(true);
	            }
	        } catch (ZosManagerException e) {
	            throw new CicsJvmserverResourceException("Unable to create the Run Temporary UNIX directory for applid " + getApplid() + " on image " + getZosImage().getImageID(), e);
	        }
		}
		return this.runTemporaryUNIXPath;
	}

}
