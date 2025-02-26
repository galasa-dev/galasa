/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.spi;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import dev.galasa.ProductVersion;
import dev.galasa.imstm.ImstmManagerException;
import dev.galasa.imstm.IImsSystem;
import dev.galasa.imstm.IImsTerminal;

public interface IImstmManagerSpi {

    void registerProvisioner(IImsSystemProvisioner provisioner);

    @NotNull
    List<IImsSystemLogonProvider> getLogonProviders();

    @NotNull
    String getProvisionType();

    @NotNull
    ProductVersion getDefaultVersion();
    
    
    public void imstmSystemStarted(IImsSystem region) throws ImstmManagerException;

	public IImsTerminal generateImsTerminal(String tag) throws ImstmManagerException;
	
	public Map<String, IImsSystem> getTaggedImsSystems();

	public IImsSystem locateImsSystem(String tag) throws ImstmManagerException;

	public List<IImsTerminal> getImsTerminals();

    public String getNextTerminalId(IImsSystem imsSystem);
}
