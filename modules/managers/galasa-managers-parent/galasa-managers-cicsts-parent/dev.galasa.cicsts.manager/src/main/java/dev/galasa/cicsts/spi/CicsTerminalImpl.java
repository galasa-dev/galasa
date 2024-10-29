/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.spi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.ipnetwork.IpNetworkManagerException;
import dev.galasa.textscan.spi.ITextScannerManagerSpi;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.Zos3270ManagerException;
import dev.galasa.zos3270.common.screens.TerminalSize;
import dev.galasa.zos3270.spi.NetworkException;
import dev.galasa.zos3270.spi.Zos3270TerminalImpl;

public class CicsTerminalImpl extends Zos3270TerminalImpl implements ICicsTerminal {

    private Log                            logger       = LogFactory.getLog(getClass());

    public final ICicsRegionProvisioned cicsRegion;
    public final ICicstsManagerSpi cicstsManager;

    public final boolean connectAtStartup;
    public final String loginCredentialsTag;

    public CicsTerminalImpl(ICicstsManagerSpi cicstsManager, IFramework framework, ICicsRegionProvisioned cicsRegion, String host, int port, boolean ssl, boolean connectAtStartup, ITextScannerManagerSpi textScanner, String loginCredentialsTag)
            throws TerminalInterruptedException, Zos3270ManagerException, ZosManagerException {
        super(cicsRegion.getNextTerminalId(), host, port, ssl, framework, false, cicsRegion.getZosImage(), new TerminalSize(80, 24), new TerminalSize(0, 0), textScanner);

        this.cicsRegion = cicsRegion;
        this.cicstsManager = cicstsManager;
        this.connectAtStartup = connectAtStartup;
        this.loginCredentialsTag = loginCredentialsTag;

        setAutoReconnect(connectAtStartup);
    }

    public CicsTerminalImpl(ICicstsManagerSpi cicstsManager, IFramework framework, ICicsRegionProvisioned cicsRegion, IIpHost ipHost, boolean connectAtStartup, ITextScannerManagerSpi textScanner, String loginCredentialsTag)
            throws TerminalInterruptedException, IpNetworkManagerException, Zos3270ManagerException, ZosManagerException {
        this(cicstsManager, framework, cicsRegion, ipHost.getHostname(), ipHost.getTelnetPort(), ipHost.isTelnetPortTls(), connectAtStartup, textScanner, loginCredentialsTag);
    }

    public CicsTerminalImpl(ICicstsManagerSpi cicstsManager, IFramework framework, ICicsRegionProvisioned cicsRegion, boolean connectAtStartup, ITextScannerManagerSpi textScanner, String loginCredentialsTag) throws TerminalInterruptedException, IpNetworkManagerException,
    Zos3270ManagerException, ZosManagerException {
        this(cicstsManager, framework, cicsRegion, cicsRegion.getZosImage().getIpHost(), connectAtStartup, textScanner, loginCredentialsTag);
    }

    public CicsTerminalImpl(ICicstsManagerSpi cicstsManager, IFramework framework, ICicsRegionProvisioned cicsRegion, boolean connectAtStartup, ITextScannerManagerSpi textScanner) throws TerminalInterruptedException, IpNetworkManagerException,
    Zos3270ManagerException, ZosManagerException {
        this(cicstsManager, framework, cicsRegion, cicsRegion.getZosImage().getIpHost(), connectAtStartup, textScanner, "");
    }

    @Override
    public ICicsRegion getCicsRegion() {
        return this.cicsRegion;
    }

    @Override
    public boolean connectToCicsRegion() throws CicstsManagerException {
    	if (this.cicstsManager.getLogonProviders().isEmpty()) {
    		throw new CicstsManagerException("Missing a CICS TS logon provider, none have been registered");
    	}
    	
        try {
            for(ICicsRegionLogonProvider logonProvider : this.cicstsManager.getLogonProviders()) {
                if (logonProvider.logonToCicsRegion(this)) {
                    return true;
                }
            }
        } catch(Exception e) {
            throw new CicstsManagerException("Failed to connect terminal",e);
        }

        return false;
    }

    @Override
    public ICicsTerminal resetAndClear() throws CicstsManagerException {
        // Really don't like this code.   Wish we had an alternative.

        logger.trace("Attempting to reset the CICS TS screen");

        try {
            boolean foundNative = false;
            for (int resetCount = 0; resetCount < 20; resetCount++ ) {
                if ( (resetCount % 2) == 0 ) { 
                    clear().wfk().pf3().wfk();
                    if (isTextInField("DFHAC2001") || isTextInField("DFHRT4401")) {
                        foundNative = true;
                        break;
                    }
                }

                clear().wfk().enter().wfk();

                if (isTextInField("DFHAC2001") || isTextInField("DFHRT4401")) {
                    foundNative = true;
                    break;
                }
            }
            if (!foundNative) {
                throw new CicstsManagerException("Unable to locate the native CICS TS screen");
            }
            
            clear().wfk();
        } catch(Exception e) {
            throw new CicstsManagerException("Unable to reset the CICS TS screen", e);
        }

        return this;
    }

    public boolean isConnectAtStartup() {
        return this.connectAtStartup;
    }

	@Override
	public void setUppercaseTranslation(boolean ucctran) throws CicstsManagerException {
		try {
			resetAndClear();
			type("CEOT " + (ucctran? "UCTRAN":"NOUCTRAN")).enter().waitForKeyboard();
			pf3().waitForKeyboard();
		} catch (KeyboardLockedException | FieldNotFoundException | NetworkException | TerminalInterruptedException | TimeoutException e) {
			throw new CicstsManagerException("Unable to set Uppercase Translation status", e);
		}
	}

	@Override
	public boolean isUppercaseTranslation() throws CicstsManagerException {
		try {
			resetAndClear();
			type("CEOT").enter().waitForKeyboard();
			home().newLine().newLine();
			String uccStatus = retrieveFieldAtCursor();
			pf3().waitForKeyboard();
			if (uccStatus.equals(new String(new byte[] { 0x20, 0x20, 0x20 })) || uccStatus.equals("Tra")) {
				return false;
			} else if (uccStatus.equals("Uct")) {
				return true;
			} else {
				throw new CicstsManagerException("Unable to find Uppercase Translation status on screen");
			}
		} catch (KeyboardLockedException | FieldNotFoundException | NetworkException | TerminalInterruptedException | TimeoutException e) {
			throw new CicstsManagerException("Unable to get Uppercase Translation status", e);
		}
	}

  @Override
  public String getLoginCredentialsTag() {
      return this.loginCredentialsTag;
  }

}
