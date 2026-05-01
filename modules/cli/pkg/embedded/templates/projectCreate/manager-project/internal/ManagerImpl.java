/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package {{.PackageName}}.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import {{.PackageName}}.{{.CapitalizedManagerName}}ManagerException;
import {{.PackageName}}.{{.CapitalizedManagerName}}Resource;
import {{.PackageName}}.I{{.CapitalizedManagerName}}Manager;
import {{.PackageName}}.I{{.CapitalizedManagerName}}Resource;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;

@Component(service = { IManager.class })
public class {{.CapitalizedManagerName}}ManagerImpl extends AbstractManager implements I{{.CapitalizedManagerName}}Manager {
    
    protected static final String NAMESPACE = "{{.PackageName}}";

    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        
        if (galasaTest.isJava()) {
            List<AnnotatedField> ourFields = findAnnotatedFields({{.CapitalizedManagerName}}ManagerField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers, galasaTest);
            }
        }
    }

    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers,
            @NotNull GalasaTest galasaTest) throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }
        activeManagers.add(this);
    }

    @GenerateAnnotatedField(annotation = {{.CapitalizedManagerName}}Resource.class)
    public I{{.CapitalizedManagerName}}Resource generate{{.CapitalizedManagerName}}Resource(Field field, List<Annotation> annotations)
            throws {{.CapitalizedManagerName}}ManagerException {
        {{.CapitalizedManagerName}}Resource annotation = field.getAnnotation({{.CapitalizedManagerName}}Resource.class);
        return new {{.CapitalizedManagerName}}ResourceImpl(annotation.resourceTag());
    }

    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        // TODO: Provision resources before test execution
    }

    @Override
    public void provisionDiscard() {
        // TODO: Clean up resources after test execution
    }
}

