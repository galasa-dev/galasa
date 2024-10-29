/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.galasaecosystem.internal;

import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Path;

import javax.validation.constraints.NotNull;

import dev.galasa.galasaecosystem.EcosystemEndpoint;
import dev.galasa.galasaecosystem.GalasaEcosystemManagerException;
import dev.galasa.ipnetwork.IIpHost;

public class SimPlatformInstance {

    private final int processNumber;
    private final Path consoleFile;
    private final IIpHost host;

    //TODO this needs to be dynamic incase we are running on a shared instance of linux
    private final int telnetPort   = 2023;
    private final int databasePort = 2027;
    private final int zosmfPort    = 2040;
    private final int webPort      = 2080;


    public SimPlatformInstance(@NotNull int processNumber, @NotNull Path consoleFile, @NotNull IIpHost host) {
        this.processNumber = processNumber;
        this.consoleFile   = consoleFile;
        this.host          = host;
    }

    public int getProcessNumber() {
        return this.processNumber;
    }

    @NotNull
    public Path getConsoleFile() {
        return consoleFile;
    }

    public Object getSimPlatformEndpoint(EcosystemEndpoint endpoint) throws GalasaEcosystemManagerException {
        try {
            switch(endpoint) {
                case SIMBANK_DATABASE:
                    return new InetSocketAddress(this.host.getHostname(), this.databasePort);
                case SIMBANK_MANAGEMENT_FACILITY:
                    return new URL("http://" + this.host.getHostname() + ":" + this.zosmfPort);
                case SIMBANK_TELNET:
                    return new InetSocketAddress(this.host.getHostname(), this.telnetPort);
                case SIMBANK_WEBSERVICE:
                    return new URL("http://" + this.host.getHostname() + ":" + this.webPort);
                default:
                    throw new GalasaEcosystemManagerException("Unrecognised endpoint " + endpoint);
            }
        } catch(GalasaEcosystemManagerException e) {
            throw e;
        } catch(Exception e) {
            throw new GalasaEcosystemManagerException("Problem resolving SimPlatform endpoint",e);
        }
    }

}
