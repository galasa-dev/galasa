/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.spi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.imstm.IImsSystem;
import dev.galasa.imstm.IImsTerminal;
import dev.galasa.imstm.ImstmManagerException;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.ipnetwork.IpNetworkManagerException;
import dev.galasa.textscan.spi.ITextScannerManagerSpi;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.Zos3270ManagerException;
import dev.galasa.zos3270.common.screens.TerminalSize;
import dev.galasa.zos3270.spi.Zos3270TerminalImpl;

public class ImsTerminalImpl extends Zos3270TerminalImpl implements IImsTerminal {

    private Log logger = LogFactory.getLog(getClass());

    public final IImsSystem imsSystem;
    public final IImstmManagerSpi imstmManager;

    public final boolean connectAtStartup;
    public final String loginCredentialsTag;

    public ImsTerminalImpl(IImstmManagerSpi imstmManager, IFramework framework, IImsSystem imsSystem, String host, int port, boolean ssl, boolean connectAtStartup, ITextScannerManagerSpi textScanner, String loginCredentialsTag)
            throws TerminalInterruptedException, Zos3270ManagerException, ZosManagerException {
        super(imstmManager.getNextTerminalId(imsSystem), host, port, ssl, framework, false, imsSystem.getZosImage(), new TerminalSize(80, 24), new TerminalSize(0, 0), textScanner);

        this.imsSystem = imsSystem;
        this.imstmManager = imstmManager;
        this.connectAtStartup = connectAtStartup;
        this.loginCredentialsTag = loginCredentialsTag;

        setAutoReconnect(connectAtStartup);
    }

    public ImsTerminalImpl(IImstmManagerSpi imstmManager, IFramework framework, IImsSystem imsSystem, IIpHost ipHost, boolean connectAtStartup, ITextScannerManagerSpi textScanner, String loginCredentialsTag)
            throws TerminalInterruptedException, IpNetworkManagerException, Zos3270ManagerException, ZosManagerException {
        this(imstmManager, framework, imsSystem, ipHost.getHostname(), ipHost.getTelnetPort(), ipHost.isTelnetPortTls(), connectAtStartup, textScanner, loginCredentialsTag);
    }

    public ImsTerminalImpl(IImstmManagerSpi imstmManager, IFramework framework, IImsSystem imsSystem, boolean connectAtStartup, ITextScannerManagerSpi textScanner, String loginCredentialsTag) throws TerminalInterruptedException, IpNetworkManagerException,
    Zos3270ManagerException, ZosManagerException {
        this(imstmManager, framework, imsSystem, imsSystem.getZosImage().getIpHost(), connectAtStartup, textScanner, loginCredentialsTag);
    }

    public ImsTerminalImpl(IImstmManagerSpi imstmManager, IFramework framework, IImsSystem imsSystem, boolean connectAtStartup, ITextScannerManagerSpi textScanner) throws TerminalInterruptedException, IpNetworkManagerException,
    Zos3270ManagerException, ZosManagerException {
        this(imstmManager, framework, imsSystem, imsSystem.getZosImage().getIpHost(), connectAtStartup, textScanner, "");
    }

    @Override
    public IImsSystem getImsSystem() {
        return this.imsSystem;
    }

    @Override
    public void connectToImsSystem() throws ImstmManagerException {
    	if (this.imstmManager.getLogonProviders().isEmpty()) {
    		throw new ImstmManagerException("Missing an IMS TM logon provider, none have been registered");
    	}
    	
        try {
            for(IImsSystemLogonProvider logonProvider : this.imstmManager.getLogonProviders()) {
                if (logonProvider.logonToImsSystem(this)) {
                    return;
                }
            }
        } catch(Exception e) {
            throw new ImstmManagerException("Failed to connect terminal",e);
        }

        throw new ImstmManagerException("Failed to connect terminal to IMS TM system");
    }

    @Override
    public IImsTerminal resetAndClear() throws ImstmManagerException {
        logger.trace("Attempting to reset the IMS TM screen");

        try {
            clear().wfk().enter().wfk();

            if (!isTextInField("DFS249")) {
                throw new ImstmManagerException("Unable to locate the native IMS TM screen");
            }
            
            clear().wfk();
        } catch(Exception e) {
            throw new ImstmManagerException("Unable to reset the IMS TM screen", e);
        }

        return this;
    }

    public boolean isConnectAtStartup() {
        return this.connectAtStartup;
    }

    @Override
    public String getLoginCredentialsTag() {
        return this.loginCredentialsTag;
    }
}
