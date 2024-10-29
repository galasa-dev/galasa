/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.gherkin;

import java.util.*;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.IGherkinExecutable;
import dev.galasa.framework.spi.IStatementOwner;
import dev.galasa.framework.spi.language.gherkin.GherkinMethod;
import dev.galasa.framework.spi.language.gherkin.GherkinTest;
import dev.galasa.zos3270.Zos3270ManagerException;
import dev.galasa.zos3270.internal.Zos3270ManagerImpl;
import dev.galasa.zos3270.spi.Zos3270TerminalImpl;

public class Gherkin3270Coordinator {
    
    // The manager that this Coordinator is a facade of.
    private final Zos3270ManagerImpl manager;
    
    // The gherkin test is essentially a feature.
    private final GherkinTest feature;

    // This coordinator keeps a reference of the terminals using the id that the gherkin scenario uses,
    // which is different to the underlying terminal id.
    // This is a mapping of the gherkin terminal id key to the terminal itself.
    private final HashMap<String, Zos3270TerminalImpl> terminals = new HashMap<>();
    private final HashMap<String, String> terminalImageTags = new HashMap<>();
    
    public Gherkin3270Coordinator(Zos3270ManagerImpl manager, GherkinTest gherkinTest) {
        this.manager = manager;
        this.feature    = gherkinTest;
    }
    
    public boolean registerStatements() throws ManagerException {
        //*** Do we have any statements we need to support
        if (!manager.registerStatements(this.feature, getStatementOwners())) {
            return false;
        }  
        return true;
    }
    
    public IStatementOwner[] getStatementOwners() {
        return new IStatementOwner[] {
                new Gherkin3270GivenTerminal(this, this.manager),
                new Gherkin3270WaitTextField(this, this.manager),
                new Gherkin3270CheckAppearsOnce(this, this.manager),
                new Gherkin3270WaitKeyboard(this, this.manager),
                new Gherkin3270MoveCursor(this, this.manager),
                new Gherkin3270PressBasicKeys(this, this.manager),
                new Gherkin3270PressPfKeys(this, this.manager),
                new Gherkin3270Credentials(this, this.manager),
                new Gherkin3270Type(this, this.manager),
                new Gherkin3270TypeInField(this, this.manager)};
    }
    
    protected Zos3270TerminalImpl getTerminal(String id) throws Zos3270ManagerException {
        Zos3270TerminalImpl terminal = this.terminals.get(id);
        return terminal ;
    }

    protected void registerTerminal(String id, Zos3270TerminalImpl terminal, String imageTag) {
        this.terminals.put(id, terminal);
        this.terminalImageTags.put(id, imageTag);
    }

    public void provisionGenerate() throws Zos3270ManagerException {
        // Provision any terminals that are Given
        List<GherkinMethod> methods = this.feature.getMethods();
        for( GherkinMethod method : methods ) {

            List<IGherkinExecutable> executables = method.getExecutables();
            for(IGherkinExecutable executable : executables) {
                Object owner = executable.getOwner();

                if (owner instanceof Gherkin3270GivenTerminal) {
                    ((Gherkin3270GivenTerminal)owner).provision(executable);
                }
            }
        }
    }
    
    protected String getImageTagForTerminal(String id) {
        return this.terminalImageTags.get(id);
    }
    
    public static String defaultTerminaId(String id) {
        return AbstractManager.defaultString(id, "A").toUpperCase();
    }

    public static String defaultImageTag(String tag) {
        return AbstractManager.defaultString(tag, "PRIMARY").toUpperCase();
    }

}
