/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.jmeter.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
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
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.jmeter.IJMeterSession;
import dev.galasa.jmeter.JMeterManagerException;
import dev.galasa.jmeter.JMeterSession;
import dev.galasa.jmeter.JMeterManagerField;
import dev.galasa.jmeter.internal.properties.JMeterMode;
import dev.galasa.jmeter.internal.properties.JMeterPropertiesSingleton;

@Component(service = { IManager.class })
public class JMeterManagerImpl extends AbstractManager {

    private static final Log logger = LogFactory.getLog(JMeterManagerImpl.class);
    protected List<IJMeterSession> activeSessions;

    private IFramework framework;
    private String jmxPath; //NOSONAR
    private String propPath; //NOSONAR

    // DockerManager Connection (optional - only needed for Docker mode)
    private IDockerManagerSpi dockerManager;
    protected List<IDockerContainer> activeContainers;

    protected static final String NAMESPACE = "jmeter";
    private boolean required = false;

    private int sessionID = 0;


    /**
     * The actual method for provisioning the JMeter session.
     *
     * Supports two execution modes controlled by CPS property jmeter.mode.execution:
     * - LOCAL: Runs JMeter using external binary installation (requires jmeter.binary.path)
     * - DOCKER: Runs JMeter in a Docker container (requires Docker Manager)
     *
     * @param field The annotated field
     * @param annotations Field annotations
     * @return IJMeterSession instance
     * @throws JMeterManagerException if provisioning fails
     * @throws DockerManagerException if Docker provisioning fails (Docker mode only)
     */
    @GenerateAnnotatedField(annotation = JMeterSession.class)
    public IJMeterSession generateJMeterSession(Field field, List<Annotation> annotations)
            throws JMeterManagerException, DockerManagerException {

        sessionID++;

        // Receiving the annotation values, JmxPath is essential and PropPath has an empty default
        JMeterSession sess = field.getAnnotation(JMeterSession.class);
        this.jmxPath = sess.jmxPath();
        this.propPath = sess.propPath();

        logger.info("JMX Path: " + this.jmxPath);
        logger.info("Properties Path: " + this.propPath);

        // Determine execution mode from CPS property
        String mode = "LOCAL";
        try {
            mode = JMeterMode.get();
        } catch (JMeterManagerException e) {
            logger.warn("Failed to get JMeter mode from CPS, defaulting to LOCAL mode", e);
        }
        logger.info("JMeter execution mode: " + mode);

        IJMeterSession session;

        if ("DOCKER".equalsIgnoreCase(mode)) {
            // Docker mode - requires Docker Manager
            session = createDockerSession();
        } else {
            // Local mode (default) - external JMeter binary
            session = createLocalSession();
        }

        activeSessions.add(session);
        return session;
    }

    /**
     * Create a JMeter session using Docker execution mode
     *
     * @return Docker-based JMeter session
     * @throws JMeterManagerException if Docker provisioning fails
     */
    private IJMeterSession createDockerSession() throws JMeterManagerException {
        if (dockerManager == null) {
            throw new JMeterManagerException(
                "Docker mode requested but Docker Manager is not available. " +
                "Either switch to LOCAL mode or ensure Docker Manager is enabled.");
        }

        try {
            IDockerContainer container = dockerManager.provisionContainer(
                "jmeter_" + sessionID,
                "galasadev/galasa-jmeter:latest",
                false,
                "PRIMARY");
            
            JMeterSessionImpl session = new JMeterSessionImpl(
                framework, this, sessionID, this.jmxPath, this.propPath,
                container, NAMESPACE);
            
            activeContainers.add(container);
            logger.info("Created Docker JMeter session " + sessionID);
            
            return session;
        } catch (DockerManagerException e) {
            throw new JMeterManagerException(
                String.format("Unable to provision Docker container for session %d", sessionID), e);
        }
    }

    /**
     * Create a JMeter session using local binary execution mode
     *
     * @return Local binary-based JMeter session
     * @throws JMeterManagerException if local session creation fails
     */
    private IJMeterSession createLocalSession() throws JMeterManagerException {
        try {
            // Create temporary filesystem directory for JMeter to work in
            // JMeter needs a real filesystem path, not a virtual RAS path
            Path workingDir = Files.createTempDirectory("jmeter-session-" + sessionID + "-");
            
            JMeterLocalSessionImpl session = new JMeterLocalSessionImpl(
                sessionID, framework, workingDir, jmxPath, propPath);
            
            logger.info("Created Local JMeter session " + sessionID + " with working directory: " + workingDir);
            return session;
        } catch (Exception e) {
            throw new JMeterManagerException(
                String.format("Unable to create local JMeter session %d", sessionID), e);
        }
    }

    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        
        // Initialize CPS singleton before any property access
        try {
            JMeterPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (JMeterManagerException | ConfigurationPropertyStoreException e) {
            throw new ManagerException("Failed to initialize JMeter CPS", e);
        }
        
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
        // Only depend on Docker Manager if running in Docker mode
        String mode = "LOCAL";
        try {
            mode = JMeterMode.get();
        } catch (JMeterManagerException e) {
            logger.warn("Failed to get JMeter mode, defaulting to LOCAL", e);
        }
        if ("DOCKER".equalsIgnoreCase(mode) && otherManager instanceof IDockerManager) {
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
        
        // Only add Docker Manager dependency if running in Docker mode
        String mode = "LOCAL";
        try {
            mode = JMeterMode.get();
        } catch (JMeterManagerException e) {
            logger.warn("Failed to get JMeter mode from CPS, defaulting to LOCAL mode", e);
        }
        
        if ("DOCKER".equalsIgnoreCase(mode)) {
            dockerManager = addDependentManager(allManagers, activeManagers, galasaTest, IDockerManagerSpi.class);
            if (dockerManager == null) {
                logger.warn("Docker mode requested but Docker Manager not available. Consider using LOCAL mode.");
            }
        } else {
            logger.info("Running in LOCAL mode - Docker Manager not required");
        }
    }

    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        generateAnnotatedFields(JMeterManagerField.class);
    }

    @Override
    public void provisionStop(){
        // Create a copy to avoid ConcurrentModificationException
        List<IJMeterSession> sessionsToStop = new ArrayList<>(activeSessions);
        for(IJMeterSession session: sessionsToStop) {
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