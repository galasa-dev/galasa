/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.artifact.BundleResources;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.core.manager.Logger;
import dev.galasa.docker.DockerContainer;
import dev.galasa.docker.DockerContainerConfig;
import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.DockerVolume;
import dev.galasa.docker.IDockerContainer;
import dev.galasa.docker.IDockerContainerConfig;
import dev.galasa.docker.IDockerExec;
import dev.galasa.docker.IDockerVolume;
import dev.galasa.http.HttpClient;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.IHttpClient;

/**
 * IVT for the Docker Manager
 * 
 * This IVT will use the standard Apache httpd Docker Image to ensure the basic
 * functionality of the Docker Manager is working correctly.<br>
 * <br>
 * Outline of the IVT is:- <br>
 * 1) recycle the container to ensure it can be restarted <br>
 * 2) Store a test html file into the container<br>
 * 3) Issue a command to ensure the file is on the filesystem<br>
 * 4) Attempt to use http to retrieve the html file<br>
 * 5) Retrieve the console logs to ensure the html file was accessed<br>
 * 6) retrieve the stored file to ensure it comes back
 * 7) Create a user define container using the @DockerContainerConfig annoation
 * 8) Change config and start with ENV's and ensure they are set
 * 9) Mounts a non created volume to the container and ensure all is created and mounted
 * 
 *  
 *
 */
@Test
public class DockerManagerIVT {

    @Logger
    public Log logger;

    @DockerContainer(image = "galasa-dev/httpd:2.4.59", dockerContainerTag = "a", start = false)
    public IDockerContainer container;

    @DockerContainer(image = "galasa-dev/httpd:2.4.59", dockerContainerTag = "b", start = false)
    public IDockerContainer containerSecondry;
    
    @DockerContainer(image = "galasa-dev/httpd:2.4.59", dockerContainerTag = "AUTOSTART", start = true)
    public IDockerContainer containerAutoStart;

    @DockerContainerConfig
    public IDockerContainerConfig config1;

    @DockerContainerConfig(
        dockerVolumes =  {
            @DockerVolume(volumeTag = "testVolume", mountPath = "/tmp/testvol"),
        }
    )
    public IDockerContainerConfig config2;

    @BundleResources
    public IBundleResources resources;

    @HttpClient
    public IHttpClient httpClient;

    /**
     * Ensure a container was provisioned
     * 
     * @throws DockerManagerException if there is a problem with the docker manager
     */
    @Test
    public void checkDockerContainerNotNull() throws DockerManagerException {
        assertThat(container).as("Docker Container").isNotNull();
    }
    
    /**
     * Recycle the container to ensure it will restart after being bounced
     * 
     * @throws DockerManagerException if there is a problem with the docker manager
     */
    @Test
    public void startAndStopContainer() throws DockerManagerException {
        logger.info("Stopping the Docker Container");
        container.stop();
        logger.info("Starting the Docker Container");
        container.start();
    }
    
    /**
     * Ensures the auto start for containers is working
     * 
     * @throws DockerManagerException
     */
    @Test
    public void checkContainerAutostart() throws DockerManagerException {
    	assertThat(containerAutoStart.isRunning()).isEqualTo(true);
    }

    @Test
    public void ensureExitCodeIsCorrect() throws DockerManagerException {
    	container.start();
    	container.stop();
    	long exitCode = container.getExitCode();
    	// Code will be 137 as stop function actually issues a kill
    	assertThat(exitCode).isEqualTo(137);
    }
    
    /**
     * Store a test html file in the container for httpd to provide. issue a ls
     * command to esnure it exists on the filesystem
     * 
     * @throws DockerManagerException      if there is a problem with the docker
     *                                     manager
     * @throws TestBundleResourceException if the test html file cant be found
     */
    @Test
    public void storeFilesInContainer() throws DockerManagerException, TestBundleResourceException {
    	container.start();
        // Retrieve the test html file
        InputStream isHtml = resources.retrieveFile("/test1.html");

        // Store it in the container
        container.storeFile("/usr/local/apache2/htdocs/test1.html", isHtml);

        // Check it is there via ls command
        IDockerExec exec = container.exec("/bin/ls", "-l", "/usr/local/apache2/htdocs/test1.html");
        assertThat(exec.waitForExec()).as("The waitForExec finished true").isTrue();

        String cmdResult = exec.getCurrentOutput();
        logger.info("Result from ls:-\n" + cmdResult);
        assertThat(cmdResult).contains("-rw");
    }

    /**
     * Retrieve the file from httpd and ensure the contents are correct
     * 
     * @throws DockerManagerException if there is a problem with the docker manager
     * @throws HttpClientException    if there is a problem retrieving the file
     * @throws URISyntaxException     if there is a problem with the host name of
     *                                the exposed port
     */
    @Test
    public void retrieveHtml() throws DockerManagerException, HttpClientException, URISyntaxException {
        InetSocketAddress exposedPort = container.getFirstSocketForExposedPort("80/tcp");
        assertThat(exposedPort).as("Correctly retrieved the exposed port").isNotNull();

        URI uri = new URI("http://" + exposedPort.getHostName() + ":" + exposedPort.getPort());

        httpClient.setURI(uri);

        String html = httpClient.getText("/test1.html").getContent();

        assertThat(html).as("Checking the HTML container the Galasa constant text").contains("Galasa Docker Test");
    }

    /**
     * Check to container logs to ensure the file was retrieved
     * 
     * @throws DockerManagerException if there is a problem with the docker manager
     */
    @Test
    public void retrieveContainerLog() throws DockerManagerException {
        String log = container.retrieveStdOut();
        logger.info("Container Log:-\n" + log);

        assertThat(log).as("checking that the test1.html was retrieved and logged")
                .contains("\"GET /test1.html HTTP/1.1\" 200");
    }

    /**
     * Pull back the test file and see if it contains the test ext
     * 
     * @throws DockerManagerException if there is a problem with the docker manager
     * @throws IOException
     */
    @Test
    public void retrieveFile() throws DockerManagerException, IOException {
       String htmlTest1 = container.retrieveFileAsString("/usr/local/apache2/htdocs/test1.html");
        
       assertThat(htmlTest1).as("check we can pull back the file").contains("<h1>Galasa Docker Test</h1>");
    }   
    
    /**
     * Start a docker container with a an empty config. Should result in normal startup
     * @throws DockerManagerException
     */
    @Test
    public void startWithConfig() throws DockerManagerException {
        assertThat(config1).isNotNull();
        container.startWithConfig(this.config1);
    }

    /**
     * Start a container with some environment variables set
     * 
     * @throws DockerManagerException
     */
    @Test
    public void startWithConfigAndEnvsSet() throws DockerManagerException {
        HashMap<String,String> envs = new HashMap<>();
        envs.put("FOO", "GALASA");

        config1.setEnvs(envs);
        container.startWithConfig(this.config1);

        IDockerExec exec = container.exec("env");
        exec.waitForExec();
        String out = exec.getCurrentOutput();;
        assertThat(out).contains("FOO=GALASA");
    }

    /**
     * Start a docker container with a docker volume mounted
     * 
     * @throws DockerManagerException
     */
    @Test
    public void startWithConfigAndVolumes() throws DockerManagerException {
        assertThat(config2).isNotNull();
        container.startWithConfig(config2);
    }

    @Test
    public void twoContainersShareVolume() throws DockerManagerException {
        container.startWithConfig(config2);
        containerSecondry.startWithConfig(config2);

        container.exec("/bin/touch", "/tmp/testvol/test.log").waitForExec();

        IDockerExec exec = containerSecondry.exec("/bin/ls", "/tmp/testvol");
        exec.waitForExec();
        assertThat(exec.getCurrentOutput()).contains("test.log");
    }

    @Test 
    public void preLoadVolumeWithConfig() throws DockerManagerException, TestBundleResourceException, InterruptedException {
        IDockerVolume volume = config2.getVolumeByTag("testVolume");
        InputStream in = resources.retrieveFile("SampleConfig.cfg");
        volume.LoadFile("TestConfigFile.cfg", in);
        container.startWithConfig(config2);
        IDockerExec cmd = container.exec("/bin/cat", "/tmp/testvol/TestConfigFile.cfg");
        cmd.waitForExec();
        assertThat(cmd.getCurrentOutput()).contains("IsThisConfig=true");
    }

    @Test 
    public void preLoadVolumeWithConfigAsString() throws DockerManagerException, TestBundleResourceException {
        IDockerVolume volume = config2.getVolumeByTag("testVolume");
        volume.LoadFileAsString("AnotherConfig.cfg", "AdditionalStringConfigs");

        container.startWithConfig(config2);

        IDockerExec cmd = container.exec("/bin/cat", "/tmp/testvol/AnotherConfig.cfg");
        cmd.waitForExec();
        assertThat(cmd.getCurrentOutput()).contains("AdditionalStringConfigs");
    }

    @Test 
    public void preLoadVolumeWithMultipleConfigs() throws DockerManagerException, TestBundleResourceException {
        IDockerVolume volume = config2.getVolumeByTag("testVolume");
        volume.LoadFileAsString("YetAnotherConfig.cfg", "AdditionalStringConfigs");
        volume.LoadFileAsString("EvenYetAnotherConfig.cfg", "AdditionalStringConfigsAgain");

        container.startWithConfig(config2);

        IDockerExec cmd = container.exec("/bin/cat", "/tmp/testvol/YetAnotherConfig.cfg");
        cmd.waitForExec();
        assertThat(cmd.getCurrentOutput()).contains("AdditionalStringConfigs");
        cmd = container.exec("/bin/cat", "/tmp/testvol/EvenYetAnotherConfig.cfg");
        cmd.waitForExec();
        assertThat(cmd.getCurrentOutput()).contains("AdditionalStringConfigsAgain");
    }

    @Test
    public void exposePortForContainerThroughConfig() throws DockerManagerException {
        List<String> ports = new ArrayList<>();
        ports.add("8080/tcp");
        ports.add("8081/tcp");
        config1.setExposedPorts(ports);

        container.startWithConfig(config1);
        Map<String, List<InetSocketAddress>> exposedPorts = container.getExposedPorts();
        assertThat(exposedPorts.containsKey(ports.get(0)));
        assertThat(exposedPorts.containsKey(ports.get(1)));
        InetSocketAddress exposedPort = container.getFirstSocketForExposedPort("8080/tcp");
        assertThat(exposedPort).as("Correctly retrieved the exposed port").isNotNull();
    }
    
    @Test
    public void getRandomExposedSocketFromContainer() throws DockerManagerException {
    	List<String> ports = new ArrayList<>();
        ports.add("8080/tcp");
        config1.setExposedPorts(ports);
    	container.startWithConfig(config1);
    	InetSocketAddress randomSocket = container.getRandomSocketForExposedPort("8080/tcp");
    	assertThat(randomSocket).isNotNull();
    }

    @Test
    public void testANonCleanShutDownRestart() throws DockerManagerException, InterruptedException {
        container.start();
        container.exec("/usr/local/apache2/bin/httpd", "-k", "stop").waitForExec();
        // Adding a wait for low performance platforms
        Instant restartTimer = Instant.now().plus(5, ChronoUnit.SECONDS);
        while(Instant.now().isBefore(restartTimer)) {
        	if(!container.isRunning()) {
        		break;
        	}
        }
        assertThat(container.isRunning()).isEqualTo(false);

        container.startWithConfig(config1);
        assertThat(container.isRunning()).isEqualTo(true);
    }
    
    @Test
    public void testFilePermissionsChange() throws DockerManagerException {
    	IDockerVolume volume = config2.getVolumeByTag("testVolume");
        volume.LoadFileAsString("AnotherConfig.cfg", "AdditionalStringConfigs");
        volume.fileChmod("755", "AnotherConfig.cfg");

        container.startWithConfig(config2);

        IDockerExec cmd = container.exec("ls", "-l", "/tmp/testvol/AnotherConfig.cfg");
        cmd.waitForExec();
        assertThat(cmd.getCurrentOutput()).contains("-rwxr-xr-x");
    }
    
    @Test
    public void testFileOwnerChange() throws DockerManagerException {
    	IDockerVolume volume = config2.getVolumeByTag("testVolume");
        volume.LoadFileAsString("AnotherConfig.cfg", "AdditionalStringConfigs");
        volume.fileChown("200", "AnotherConfig.cfg");

        container.startWithConfig(config2);

        IDockerExec cmd = container.exec("ls", "-l", "/tmp/testvol/AnotherConfig.cfg");
        cmd.waitForExec();
        assertThat(cmd.getCurrentOutput()).contains("200");
    }
    
}