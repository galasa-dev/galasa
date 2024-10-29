/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.jmeter.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import dev.galasa.ManagerException;
import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.IDockerContainer;
import dev.galasa.docker.IDockerManager;
import dev.galasa.docker.spi.IDockerManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.jmeter.IJMeterSession;
import dev.galasa.jmeter.JMeterManagerException;
import dev.galasa.jmeter.JMeterSession;
import dev.galasa.jmeter.JMeterManagerField;

@Component(service = { IManager.class })
public class JMeterManagerImpl extends AbstractManager {

    private static final Log logger                     = LogFactory.getLog(JMeterManagerImpl.class);
    protected List<JMeterSessionImpl> activeSessions;

    private IFramework framework;
    private String jmxPath; //NOSONAR
    private String propPath; //NOSONAR

    // DockerManager Connection
    private IDockerManagerSpi dockerManager;
    protected List<IDockerContainer> activeContainers;

    protected static final String NAMESPACE             = "jmeter";
    private boolean required                            = false;


    private int sessionID = 0;


    /**
     * The actual method for provisioning the JMeter session with a container that
     * can run JMeter
     * 
     * @param field
     * @param annotations
     * @return IJMeterSession instance
     * @throws JMeterManagerException
     * @throws DockerManagerException
     */
    @GenerateAnnotatedField(annotation = JMeterSession.class)
    public IJMeterSession generateJMeterSession(Field field, List<Annotation> annotations)
            throws JMeterManagerException, DockerManagerException {

        sessionID++;

        // Receiving the annotation values, JmxPath is essential and PropPath has an empty default
        JMeterSession sess = field.getAnnotation(JMeterSession.class);
        this.jmxPath = sess.jmxPath();
        this.propPath = sess.propPath();

        logger.info(this.jmxPath);
        logger.info(this.propPath);

        JMeterSessionImpl session;
        
        try {
            IDockerContainer container = dockerManager.provisionContainer("jmeter_" + sessionID, "galasadev/galasa-jmeter:latest", false, "PRIMARY");
            session = new JMeterSessionImpl(framework, this, sessionID, this.jmxPath, this.propPath, container, logger, NAMESPACE);
            activeContainers.add(container);
            activeSessions.add(session);
        } catch (DockerManagerException e) {
            throw new JMeterManagerException(String.format("Unable to provision the docker container for session %d", sessionID));
        }

        return session;
    }

    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        
        if(galasaTest.isJava()) {
            List<AnnotatedField> ourFields = findAnnotatedFields(JMeterManagerField.class);
            if (ourFields.isEmpty() && this.required) {
                return;
            }
            youAreRequired(allManagers, activeManagers, galasaTest);
        }

        this.framework = framework;
        this.activeSessions = new ArrayList<>();
        this.activeContainers = new ArrayList<>();

        logger.info("JMeter manager has been succesfully initialised.");

        
    }

    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        if (otherManager instanceof IDockerManager) {
            return true;
        }

        return super.areYouProvisionalDependentOn(otherManager);
    }

    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
            throws ManagerException {
        this.required = true;

        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
        dockerManager = addDependentManager(allManagers, activeManagers, galasaTest, IDockerManagerSpi.class);
    }

    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        generateAnnotatedFields(JMeterManagerField.class);
    }

    @Override
    public void provisionStop(){
        for(IJMeterSession session: activeSessions) {
            try {
                session.stopTest();
            } catch (JMeterManagerException e) {
               logger.info("The manager was not able to shutdown all sessions that are currently active");
            }
        }
    }


    public void shutdown(int sessionID) {
        for(IJMeterSession session: activeSessions) {
            try {
                if ( session.getSessionID() == sessionID) {
                    session.stopTest();
                }
            } catch (JMeterManagerException e) {
                logger.info("The manager was not able to shutdown this session " + session.getSessionID());
            }
        }
    }

}