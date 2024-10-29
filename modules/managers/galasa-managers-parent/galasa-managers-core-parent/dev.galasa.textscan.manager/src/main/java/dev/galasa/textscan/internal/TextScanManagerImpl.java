/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.textscan.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.textscan.ILogScanner;
import dev.galasa.textscan.ITextScanner;
import dev.galasa.textscan.LogScanner;
import dev.galasa.textscan.TextScanManagerException;
import dev.galasa.textscan.TextScanManagerField;
import dev.galasa.textscan.TextScanner;
import dev.galasa.textscan.spi.ITextScannerManagerSpi;

@Component(service = { IManager.class })
public class TextScanManagerImpl extends AbstractManager implements ITextScannerManagerSpi {

    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);

        if(Boolean.TRUE.equals(galasaTest.isJava())) {
            //*** Check to see if any of our annotations are present in the test class
            //*** If there is,  we need to activate
            List<AnnotatedField> ourFields = findAnnotatedFields(TextScanManagerField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers, galasaTest);
            }
        }
    }

    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
    }


    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#provisionGenerate()
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        generateAnnotatedFields(TextScanManagerField.class);
    }

    @GenerateAnnotatedField(annotation=TextScanner.class)
    public ITextScanner generateTextScanner(Field field, List<Annotation> annotations) {
        return new TextScannerImpl();
    }

    @GenerateAnnotatedField(annotation=LogScanner.class)
    public ILogScanner generateLogScanner(Field field, List<Annotation> annotations) {
        return new LogScannerImpl();
    }

    @Override
    public ITextScanner getTextScanner() throws TextScanManagerException {
    	return new TextScannerImpl();
    }

    @Override
    public ILogScanner getLogScanner() throws TextScanManagerException {
    	return new LogScannerImpl();
    }
}
