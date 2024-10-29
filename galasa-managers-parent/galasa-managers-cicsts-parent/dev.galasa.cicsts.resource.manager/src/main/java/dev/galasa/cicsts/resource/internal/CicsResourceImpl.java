/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.resource.internal;

import java.util.Map;

import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.cicsresource.CicsBundleResourceException;
import dev.galasa.cicsts.cicsresource.CicsJvmserverResourceException;
import dev.galasa.cicsts.cicsresource.CicsResourceManagerException;
import dev.galasa.cicsts.cicsresource.ICicsBundle;
import dev.galasa.cicsts.cicsresource.ICicsResource;
import dev.galasa.cicsts.cicsresource.IJvmprofile;
import dev.galasa.cicsts.cicsresource.IJvmserver;
import dev.galasa.cicsts.cicsresource.IJvmserver.JvmserverType;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosliberty.IZosLibertyServer;

public class CicsResourceImpl implements ICicsResource {

    private CicsResourceManagerImpl cicsResourceManagerImpl;
    private ICicsRegion cicsRegion;
    private IZosImage zosImage;
    private IZosFileHandler zosFileHandler;

    public CicsResourceImpl(CicsResourceManagerImpl cicsResourceManagerImpl, ICicsRegion cicsRegion) throws CicsResourceManagerException {
        this.cicsResourceManagerImpl = cicsResourceManagerImpl;
        this.cicsRegion = cicsRegion;
        this.zosImage = cicsRegion.getZosImage();
        try {
            this.zosFileHandler =  cicsResourceManagerImpl.getZosFileHandler();
        } catch (CicsResourceManagerException e) {
            throw new CicsResourceManagerException("Unable to get zOS File Handler", e);
        }
    }

    @Override
	public ICicsBundle newCicsBundle(ICicsTerminal cicsTerminal, Class<?> testClass, String name, String group, String bundlePath, Map<String, String> parameters) throws CicsBundleResourceException {
		return new CicsBundleImpl(this.cicsResourceManagerImpl, this.cicsRegion, cicsTerminal, testClass, name, group, bundlePath, null, parameters);
	}

    @Override
	public ICicsBundle newCicsBundle(ICicsTerminal cicsTerminal, Class<?> testClass, String name, String group, String bundleDir) throws CicsBundleResourceException {
		return new CicsBundleImpl(this.cicsResourceManagerImpl, this.cicsRegion, cicsTerminal, testClass, name, group, null, bundleDir, null);
	}

	@Override
    public IJvmserver newJvmserver(ICicsTerminal cicsTerminal, String name, String group, String jvmprofileName, JvmserverType jvmserverType) throws CicsJvmserverResourceException {
        return new JvmserverImpl(this.cicsResourceManagerImpl, this.cicsRegion, cicsTerminal, name, group, jvmprofileName, jvmserverType);
    }

    @Override
    public IJvmserver newJvmserver(ICicsTerminal cicsTerminal, String name, String group, IJvmprofile jvmprofile) throws CicsJvmserverResourceException {
        return new JvmserverImpl(this.cicsResourceManagerImpl, this.cicsRegion, cicsTerminal, name, group, jvmprofile);
    }

    @Override
    public IJvmserver newLibertyJvmserver(ICicsTerminal cicsTerminal, String name, String group, IJvmprofile jvmprofile, IZosLibertyServer libertyServer) throws CicsJvmserverResourceException {
        return new JvmserverImpl(this.cicsResourceManagerImpl, this.cicsRegion, cicsTerminal, name, group, jvmprofile, libertyServer);
    }

    @Override
    public IJvmprofile newJvmprofile(String jvmprofileName) {
        return new JvmprofileImpl(this.zosFileHandler, this.zosImage, jvmprofileName);
    }

    @Override
    public IJvmprofile newJvmprofile(String jvmprofileName, Map<String, String> content) {
        return new JvmprofileImpl(this.zosFileHandler, this.zosImage, jvmprofileName, content);
    }

    @Override
    public IJvmprofile newJvmprofile(String jvmprofileName, String content) {
        return new JvmprofileImpl(this.zosFileHandler, this.zosImage, jvmprofileName, content);
    }

}
