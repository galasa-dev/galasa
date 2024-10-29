/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.ceda.internal;

import javax.validation.constraints.NotNull;

import dev.galasa.cicsts.CedaException;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICeda;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.spi.NetworkException;

public class CedaImpl implements ICeda{

	private ICicsRegion cicsRegion;
	
	public CedaImpl(ICicsRegion cicsRegion) {
		this.cicsRegion = cicsRegion;
	}
	

	@Override
	public void createResource(@NotNull ICicsTerminal terminal, @NotNull String resourceType, 
            @NotNull String resourceName, @NotNull String groupName, String resourceParameters) throws CedaException{
		
		if(cicsRegion != terminal.getCicsRegion()) {
			 throw new CedaException("The provided terminal is not from the correct CICS Region");
		}

		if (!terminal.isClearScreen()) {
		    try {
                terminal.resetAndClear();
            } catch (CicstsManagerException e) {
                throw new CedaException("Problem reset and clearing screen for ceda transaction", e);
            }
		}

		try {
			if(resourceParameters==null){
				terminal.type("CEDA DEFINE " + resourceType + "(" + resourceName +
						") GROUP(" + groupName + ") ").enter().wfk();
			}else{

				terminal.type("CEDA DEFINE " + resourceType + "(" + resourceName +
						") GROUP(" + groupName + ") " + resourceParameters).enter().wfk();
			}
		}catch(TimeoutException | KeyboardLockedException | NetworkException | TerminalInterruptedException | FieldNotFoundException e) {
			throw new CedaException("Problem with starting the CEDA transaction", e);
		}

		try {
			if(terminal.retrieveScreen().contains("DEFINE SUCCESSFUL")){
				if(terminal.retrieveScreen().contains("MESSAGES:")) {
					terminal.pf9().wfk();
				}
			}
		}catch (Exception e) {
			throw new CedaException("Problem determining the result from the CEDA command", e);
		}

		try {
			terminal.pf3().wfk();
			terminal.clear().wfk();
		}catch(Exception e) {
			throw new CedaException("Unable to return terminal back into reset state", e);
		}

	}

	@Override
	public void installGroup(@NotNull ICicsTerminal terminal, @NotNull String groupName) throws CedaException {
		if(cicsRegion != terminal.getCicsRegion()) {
			 throw new CedaException("The provided terminal is not from the correct CICS Region");
		}

        if (!terminal.isClearScreen()) {
            try {
                terminal.resetAndClear();
            } catch (CicstsManagerException e) {
                throw new CedaException("Problem reset and clearing screen for ceda transaction", e);
            }
        }

		try {
			terminal.type("CEDA INSTALL GROUP(" + groupName + ")").enter().wfk();

		}catch(Exception e) {
			throw new CedaException("Problem with starting the CEDA transaction");
		}

		try {
			if(!terminal.retrieveScreen().contains("INSTALL SUCCESSFUL")) {
				terminal.pf9().wfk();
				terminal.pf3().wfk();
				terminal.clear().wfk();
				throw new CedaException("Errors detected whilst installing group");
			}
		}catch(Exception e) {
			throw new CedaException("Problem determining the result from the CEDA command", e);
		}

		try {
			terminal.pf3().wfk();
			terminal.clear().wfk();
		}catch(Exception e) {
			throw new CedaException("Unable to return terminal back into reset state", e);
		}
	}

	@Override
	public void installResource(@NotNull ICicsTerminal terminal, @NotNull String resourceType, 
            @NotNull String resourceName, @NotNull String cedaGroup)
			throws CedaException {

		if(cicsRegion != terminal.getCicsRegion()) {
			 throw new CedaException("The provided terminal is not from the correct CICS Region");
		}

        if (!terminal.isClearScreen()) {
            try {
                terminal.resetAndClear();
            } catch (CicstsManagerException e) {
                throw new CedaException("Problem reset and clearing screen for ceda transaction", e);
            }
        }

		try {

			terminal.type("CEDA INSTALL " + resourceType + "(" + resourceName + ") GROUP(" +
					cedaGroup + ")").enter().wfk();

		}catch(Exception e) {
			throw new CedaException("Problem with starting the CEDA transaction", e);
		}

		try {
			boolean error = false;
			try {
				if (terminal.retrieveScreen().contains("USE P9 FOR S MSGS")) {
					error = true;

					//if the terminal contains the error then error = true elseif it contains
					//the success then error = false
				}else if(!terminal.retrieveScreen().contains("INSTALL SUCCESSFUL")) {
					error = true;
				}

				if(error) {
					terminal.pf9().wfk();
					throw new CedaException("Errors detected whilst installing group");
				}
			}catch(Exception e) {
				error = true;
			}
		}catch(Exception e) {
			throw new CedaException("Problem determining the result from the CEDA command");
		}

		try {
			terminal.pf3().wfk();
			terminal.clear().wfk();
		}catch(Exception e) {
			throw new CedaException("Unable to return terminal back into reset state", e);
		}

	}

	@Override
	public void deleteGroup(@NotNull ICicsTerminal terminal, @NotNull String groupName) throws CedaException {
		if(cicsRegion != terminal.getCicsRegion()) {
			 throw new CedaException("The provided terminal is not from the correct CICS Region");
		}

        if (!terminal.isClearScreen()) {
            try {
                terminal.resetAndClear();
            } catch (CicstsManagerException e) {
                throw new CedaException("Problem reset and clearing screen for ceda transaction", e);
            }
        }

		try {
			terminal.type("CEDA DELETE GROUP(" + groupName + ") ALL").enter().wfk();
		}catch(Exception e) {
			throw new CedaException("Problem with starting the CEDA transaction");
		}

		try {
			if(!terminal.retrieveScreen().contains("DELETE SUCCESSFUL")) {
				terminal.pf9().wfk();
				terminal.pf3().wfk();
				terminal.clear().wfk();

				throw new CedaException("Errors detected whilst discarding group");
			}
		}catch(Exception e) {
			throw new CedaException("Problem determining the result from the CEDA command", e);
		}

		try {
			terminal.pf3().wfk();
			terminal.clear().wfk();
		}catch(Exception e) {
			throw new CedaException("Unable to return terminal back into reset state", e);
		}
	}

	@Override
	public void deleteResource(@NotNull ICicsTerminal terminal, @NotNull String resourceType, 
            @NotNull String resourceName, @NotNull String groupName)
			throws CedaException {

		if(cicsRegion != terminal.getCicsRegion()) {
			 throw new CedaException("The provided terminal is not from the correct CICS Region");
		}

        if (!terminal.isClearScreen()) {
            try {
                terminal.resetAndClear();
            } catch (CicstsManagerException e) {
                throw new CedaException("Problem reset and clearing screen for ceda transaction", e);
            }
        }


		try {

			terminal.wfk();
			terminal.type("CEDA DELETE " + resourceType + "("
					+ resourceName + ") GROUP(" + groupName + ")").enter().wfk();
		}catch(Exception e) {
			throw new CedaException("Problem with starting the CEDA transaction", e);
		}

		try {
			if(!terminal.retrieveScreen().contains("DELETE SUCCESSFUL")) {
				terminal.pf9().wfk();
				terminal.pf3().wfk();
        terminal.clear().wfk();
				throw new CedaException("Errors detected whilst discarding group");
			}
		}catch(Exception e) {
			throw new CedaException("Problem determining the result from the CEDA command)", e);

		}
		try {
			terminal.pf3().wfk();
			terminal.clear().wfk();
		}catch(Exception e) {
			throw new CedaException("Unable to return terminal back into reset state", e);
		}

	}


	@Override
	public boolean resourceExists(@NotNull ICicsTerminal terminal, @NotNull String resourceType, @NotNull String resourceName, @NotNull String groupName) throws CedaException {
		if (cicsRegion != terminal.getCicsRegion()) {
			throw new CedaException("The provided terminal is not from the correct CICS Region");
		}

        if (!terminal.isClearScreen()) {
            try {
                terminal.resetAndClear();
            } catch (CicstsManagerException e) {
                throw new CedaException("Problem reset and clearing screen for ceda transaction", e);
            }
        }


		try {
			terminal.wfk();
			terminal.type("CEDA DISPLAY " + resourceType + "(" + resourceName + ") GROUP(" + groupName + ")").enter().wfk();
		} catch(Exception e) {
			throw new CedaException("Problem with starting the CEDA transaction", e);
		}

		boolean exists = false;
		try {
			if (terminal.retrieveScreen().contains("RESULTS: 1 TO 1 OF 1"))	{
				exists = true;
			} else if (!terminal.retrieveScreen().contains("DISPLAY UNSUCCESSFUL")) {
				terminal.pf9().wfk();
        terminal.pf3().wfk();
        terminal.clear().wfk();
				throw new CedaException("Errors detected whilst displaying resource");
			}
		} catch(Exception e) {
			throw new CedaException("Problem determining the result from the CEDA command)", e);

		}
		try {
			terminal.pf3().wfk();
			terminal.clear().wfk();
		} catch(Exception e) {
			throw new CedaException("Unable to return terminal back into reset state", e);
		}

		return exists;
	}

}
