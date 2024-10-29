/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosprogram.internal;

import java.io.IOException;
import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosfile.IZosDataset;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosprogram.IZosProgram;
import dev.galasa.zosprogram.ZosProgram.Language;
import dev.galasa.zosprogram.ZosProgramException;
import dev.galasa.zosprogram.ZosProgramManagerException;

/**
 * Implementation of {@link IZosProgram}
 *
 */
public class ZosProgramImpl implements IZosProgram {
    
    private static final Log logger = LogFactory.getLog(ZosProgramImpl.class);
    
    private ZosProgramManagerImpl zosProgramManager;
    public ZosProgramManagerImpl getZosProgramManager() {
		return zosProgramManager;
	}

	private Field field;
    private IZosImage image;
    private String name;
    private String location;
    private Language language;
    private boolean cics;
    private IZosDataset loadlib;
    private String programSource;
    private IZosBatchJob compileJob;
    private boolean compile;

    public ZosProgramImpl(ZosProgramManagerImpl zosProgramManager, Field field, String imageTag, String name, String location, Language language, boolean cics, String loadlib, boolean compile) throws ZosProgramException {
    	this.zosProgramManager = zosProgramManager;
        this.field = field;
        try {
            initalise(zosProgramManager.getZosManager().getImageForTag(imageTag), name, location, language, cics, loadlib, compile);
        } catch (ZosManagerException e) {
            throw new ZosProgramException(e);
        }
    }

    public ZosProgramImpl(ZosProgramManagerImpl zosProgramManager, IZosImage image, String name, String programSource, Language language, boolean cics, String loadlib) throws ZosProgramException {
    	this.zosProgramManager = zosProgramManager;
    	this.programSource = programSource;
        initalise(image, name, null, language, cics, loadlib, false);
    }
    
    protected void initalise(IZosImage image, String name, String location, Language language, boolean cics, String loadlib, boolean compile) throws ZosProgramException {
        this.image = image;
        this.name = name;
        this.location = location;
        this.language = language;
        this.cics = cics;
        this.compile = compile;
                
        setLoadlib(loadlib);
    }

    @Override
    public Field getField() {
        return this.field;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public String getLocation() {
        return this.location;
    }

    @Override
    public Language getLanguage() {
        return this.language;
    }

    @Override
    public boolean isCics() {
        return this.cics;
    }

    @Override
    public IZosDataset getLoadlib() {
        return this.loadlib;
    }

    @Override
    public IZosImage getImage() {
        return this.image;
    }

    @Override
    public String getProgramSource() throws ZosProgramException {
        if (this.programSource == null) {
            loadProgramSource();
        }
        return this.programSource;
    }

    @Override
    public String toString() {
        return "name=" + name + ", location=" + location + ", language=" + language +  ", loadlib=" + loadlib + ", image=" + image;
    }

    @Override
    public IZosBatchJob getCompileJob() {
        return this.compileJob;
    }
    
    protected void setLoadlib(Object loadlib) throws ZosProgramException {
        if (loadlib != null) {
            if (loadlib instanceof IZosDataset) {
                this.loadlib = (IZosDataset) loadlib;
            } else {
                try {
                    this.loadlib = zosProgramManager.getZosFile().getZosFileHandler().newDataset((String) loadlib, image);
                } catch (ZosFileManagerException e) {
                    throw new ZosProgramException("Unable to instantiate loadlib data set object", e); 
                }
            }
        }
    }

    protected void setCompileJob(IZosBatchJob compileJob) {
        this.compileJob = compileJob;
    }

    protected void loadProgramSource() throws ZosProgramException {
        IBundleResources testBundleResources = zosProgramManager.getTestBundleResources();
        String sourcePath;
        if (getLocation() == null) {
            sourcePath = getName() + "." + getLanguage().getFileExtension();
        } else {
            if (getLocation().endsWith(getLanguage().getFileExtension())) {
                sourcePath = getLocation();
            } else {
                sourcePath = getLocation() + "/" + getName() + getLanguage().getFileExtension();
            }
        }
        try {
            logger.info("Loading source for " + getLanguage() + " program " + getName() + " from " + sourcePath);
            programSource = testBundleResources.retrieveFileAsString(sourcePath);
        } catch (TestBundleResourceException | IOException e) {
            throw new ZosProgramException("Problem loading program source", e);
        }
    }
    
    protected String logForField() {
        return (getField() != null? " for field \"" + getField().getName() + "\"": "");
    }

    @Override
    public IZosProgram compile() throws ZosProgramManagerException {
        logger.info("Compile " + getLanguage() + " program \"" + getName() + "\"" + logForField());
        switch (getLanguage()) {
        case ASSEMBLER:
            ZosAssemblerProgramCompiler assemblerProgram = new ZosAssemblerProgramCompiler(this);
            assemblerProgram.compile();
            break;
        case COBOL:
            ZosCobolProgramCompiler cobolProgram = new ZosCobolProgramCompiler(this);
            cobolProgram.compile();
            break;
        case C:
            ZosCProgramCompiler cProgram = new ZosCProgramCompiler(this);
            cProgram.compile();
            break;
        case PL1:
            ZosPl1ProgramCompiler pl1Program = new ZosPl1ProgramCompiler(this);
            pl1Program.compile();
            break;
        default:
            throw new ZosProgramManagerException("Invalid program language: " + getLanguage());
        }
        return this;
    }

    protected boolean getCompile() {
        return this.compile;
    }
}
