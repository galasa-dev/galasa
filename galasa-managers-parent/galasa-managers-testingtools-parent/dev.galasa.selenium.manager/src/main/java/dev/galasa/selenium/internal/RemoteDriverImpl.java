/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium.internal;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.google.gson.JsonObject;

import dev.galasa.artifact.IArtifactManager;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.IDockerContainer;
import dev.galasa.docker.spi.IDockerManagerSpi;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.spi.IHttpManagerSpi;
import dev.galasa.kubernetes.IDeployment;
import dev.galasa.kubernetes.IKubernetesNamespace;
import dev.galasa.kubernetes.IService;
import dev.galasa.kubernetes.spi.IKubernetesManagerSpi;
// import dev.galasa.docker.DockerManagerException;
// import dev.galasa.docker.IDockerContainer;
// import dev.galasa.docker.spi.IDockerManagerSpi;
import dev.galasa.selenium.Browser;
import dev.galasa.selenium.IChromeOptions;
import dev.galasa.selenium.IEdgeOptions;
import dev.galasa.selenium.IFirefoxOptions;
import dev.galasa.selenium.IInternetExplorerOptions;
import dev.galasa.selenium.IOperaOptions;
import dev.galasa.selenium.ISeleniumManager;
import dev.galasa.selenium.IWebDriver;
import dev.galasa.selenium.IWebPage;
import dev.galasa.selenium.SeleniumManagerException;
import dev.galasa.selenium.internal.properties.SeleniumGridEndpoint;
import dev.galasa.selenium.internal.properties.SeleniumKubernetesNamespace;
import dev.galasa.selenium.internal.properties.SeleniumKubernetesNodeSelector;
import dev.galasa.selenium.internal.properties.SeleniumWebDriverType;

public class RemoteDriverImpl extends DriverImpl implements IWebDriver {
	private static final Log logger = LogFactory.getLog(RemoteDriverImpl.class);
	
    private List<WebPageImpl>   webPages = new ArrayList<>();
    private URL                 remoteDriverEndpoint;
    private Path                screenshotRasDirectory;
    private Browser             browser;
    private SeleniumManagerImpl seleniumManager;
    private IDynamicStatusStoreService dss;
    
    private SeleniumEnvironment seleniumEnvironment;
    
    private String driverSlotName;
    private String k8sRunName;
    
    public RemoteDriverImpl(SeleniumEnvironment seleniumEnvironment,SeleniumManagerImpl seleniumManager, 
    		Browser browser, String slotName, Path screenshotRasDirectory) throws SeleniumManagerException {
    	this.seleniumEnvironment = seleniumEnvironment;
    	this.seleniumManager = seleniumManager;
    	this.browser = browser;
    	this.driverSlotName = slotName;
        this.screenshotRasDirectory = screenshotRasDirectory;
        this.dss = seleniumManager.getDss();
        
    	try {
			switch(SeleniumWebDriverType.get()) {
			case ("docker"):
				provisionDocker(seleniumManager.getDockerManager(), seleniumManager.getHttpManager());
				break;
			case ("kubernetes"):
                this.k8sRunName = slotName.replace("_", "-").toLowerCase();
				provisionK8s(seleniumManager.getKubernetesManager(), seleniumManager.getArtifactManager(), seleniumManager.getHttpManager());
				break;
			case ("grid"):
				provisionGrid(seleniumManager.getHttpManager());
				break;
			default :
				throw new SeleniumManagerException("Unsupported Driver Type: " + SeleniumWebDriverType.get());
			}
		} catch (ConfigurationPropertyStoreException e) {
			throw new SeleniumManagerException("Failed to create remote driver",e);
		}
    }

    private void provisionGrid(IHttpManagerSpi httpManager) throws SeleniumManagerException {
    	try {
    		this.remoteDriverEndpoint = new URL(SeleniumGridEndpoint.get());
    		
    		IHttpClient client = httpManager.newHttpClient();
            client.setURI(remoteDriverEndpoint.toURI());
    		
            HttpClientResponse<JsonObject> resp = client.getJson("/status");
            if (resp.getStatusCode() >200 ) {
            	throw new SeleniumManagerException("Bad response from Grid: " + resp.getStatusLine());
            }
    	} catch (MalformedURLException |ConfigurationPropertyStoreException |URISyntaxException  |HttpClientException e) {
    		throw new SeleniumManagerException("Failed to provision a Grid Driver.", e);
    	}
    	
    }
 
    private void provisionDocker(IDockerManagerSpi dockerManager, IHttpManagerSpi httpManager) throws SeleniumManagerException {
        try {
            IDockerContainer container = dockerManager.provisionContainer("Selenium_Standalone_node_"+this.driverSlotName, browser.getDockerImageName(), true, "PRIMARY");
            
            List<InetSocketAddress> dockerEndpoint = container.getExposedPorts().get("4444/tcp");
            this.remoteDriverEndpoint = new URL("http:/"+ dockerEndpoint.get(0));
            
            IHttpClient client = httpManager.newHttpClient();
            client.setURI(remoteDriverEndpoint.toURI());
            for (int i=0;i<5;i++) {
            	try {
                    Thread.sleep(2000);
            		HttpClientResponse<JsonObject> resp = client.getJson("/status");
                	if (resp.getStatusCode() < 202) {
                		break;
                	}
            	} catch (HttpClientException e) {
            		continue;
            	}
            	throw new SeleniumManagerException("Selenium node failed to become ready");
            }
        } catch (DockerManagerException | MalformedURLException | InterruptedException | URISyntaxException e) {
            throw new SeleniumManagerException(e);
        } 
        
    }
    
    private void provisionK8s(IKubernetesManagerSpi k8Manager, IArtifactManager artifactManager, IHttpManagerSpi httpManager) throws SeleniumManagerException {
    	String seleniumPodYaml = generatePodYaml(artifactManager);
    	String seleniumServiceYaml = generateServiceYaml(artifactManager);
    	try {
    		IKubernetesNamespace namespace = k8Manager.getNamespaceByTag(SeleniumKubernetesNamespace.get()); 
    	
    		IService service = (IService)namespace.createResource(seleniumServiceYaml);
            IDeployment pod = (IDeployment)namespace.createResource(seleniumPodYaml);

    		InetSocketAddress socket = service.getSocketAddressForPort(4444);
    		
    		this.remoteDriverEndpoint = new URL("http://" + socket.getHostString() + ":" + Integer.toString(socket.getPort()));
    		IHttpClient client = httpManager.newHttpClient();
            client.setURI(remoteDriverEndpoint.toURI());
    	
    		for (int i=0;i<=10;i++) {
    			try {
    				if (client.getJson("/status").getStatusCode() == 200){
                        logger.debug("Connected to grid at: " + this.remoteDriverEndpoint);
        				return;
        			}
    			} catch (HttpClientException e) {
                    Thread.sleep(5000);
    				logger.debug("Failed to reach node endpoint. Retrying in 5 seconds");
    			}
    		}
    	} catch (Exception e) {
    		throw new SeleniumManagerException("Unable to provision K8 node", e);
    	}
    	throw new SeleniumManagerException("Selenium Node took too long to ready.");
    	
    }
    
    private String generateServiceYaml(IArtifactManager artifacts) throws SeleniumManagerException {
    	IBundleResources resources = artifacts.getBundleResources(getClass());
        logger.trace("Generating Service Yaml");
    	
    	try {
			String yaml = resources.retrieveFileAsString("resources/selenium-node-expose.yaml");
			String runName = seleniumManager.getFramework().getTestRunName();
			yaml = yaml.replace("<RUNNAME>", this.k8sRunName);
			
            logger.trace(yaml);
			return yaml;
    	} catch (IOException | TestBundleResourceException e) {
    		throw new SeleniumManagerException("Failed to generate service yaml", e);
    	}
    }
    
    private String generatePodYaml(IArtifactManager artifacts) throws SeleniumManagerException {
    	logger.trace("Generating Pod Yaml");
    	IBundleResources resources = artifacts.getBundleResources(getClass());
    	try {
			String yaml = resources.retrieveFileAsString("resources/selenium-node-deployment.yaml");
			yaml = yaml.replace("<IMAGE_NAME>", this.browser.getDockerImageName());
			
			String runName = seleniumManager.getFramework().getTestRunName();
			yaml = yaml.replace("<RUNNAME>", this.k8sRunName);
			
			String nodeSelectors = "";
			String[] selectors = SeleniumKubernetesNodeSelector.get();
			if (selectors.length > 1) {
				nodeSelectors += "      nodeSelector:\n";
				for(String selector : selectors) {
					nodeSelectors += "        " + selector + "\n";
				}
			}
			yaml = yaml.replace("<NODE_SELECTOR>", nodeSelectors);
			logger.trace(yaml);
			return yaml;
		} catch (TestBundleResourceException | IOException | ConfigurationPropertyStoreException e) {
			throw new SeleniumManagerException("Unable to generate pod yaml",e);
		} 
    }

    private RemoteWebDriver remoteDriver() throws SeleniumManagerException {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setBrowserName(browser.getDriverName());
        capabilities.setCapability("GALASA", "GALASA");

        RemoteWebDriver driver = new RemoteWebDriver(this.remoteDriverEndpoint, capabilities);
        // Selenium Environment setSessionID in Dss for grid cleanup
        try {
			if (dss.get("driver.slot."+ driverSlotName +".session") == null) {
				dss.put("driver.slot."+ driverSlotName +".session", driver.getSessionId().toString());
			} else {
				// Clear the driver of any pages left open
				discard();
				dss.put("driver.slot."+ driverSlotName +".session", driver.getSessionId().toString());
			}
		} catch (DynamicStatusStoreException e) {
			throw new SeleniumManagerException("Failed to set session to a slot", e);
		}
        
        return driver;
    }
    
    private RemoteWebDriver remoteDriver(DesiredCapabilities capabilities) throws SeleniumManagerException {
        capabilities.setBrowserName(browser.getDriverName());

        return new RemoteWebDriver(this.remoteDriverEndpoint, capabilities);
    }

    @Override
    public IWebPage allocateWebPage() throws SeleniumManagerException {
        return allocateWebPage(null);
    }

    @Override
    public IWebPage allocateWebPage(String url) throws SeleniumManagerException {
        RemoteWebDriver driver = null;

        try {
            driver = remoteDriver();

            if (driver == null)
                throw new SeleniumManagerException("Unsupported driver type: " + browser.getDriverName());
        } catch (SeleniumManagerException e) {
            throw new SeleniumManagerException("Issue provisioning web driver", e);
        }

        return allocatePage(seleniumManager, driver, url, screenshotRasDirectory);
    }

    @Override
    public IWebPage allocateWebPage(String url, IFirefoxOptions options) throws SeleniumManagerException {
    	RemoteWebDriver driver = null;

        try {
            driver = remoteDriver(new DesiredCapabilities(options.getOptions()));

            if (driver == null)
                throw new SeleniumManagerException("Unsupported driver type: " + browser.getDriverName());
        } catch (SeleniumManagerException e) {
            throw new SeleniumManagerException("Issue provisioning web driver", e);
        }

        return allocatePage(seleniumManager, driver, url, screenshotRasDirectory);
    }

    @Override
    public IWebPage allocateWebPage(String url, IChromeOptions options) throws SeleniumManagerException {
    	RemoteWebDriver driver = null;

        try {
            driver = remoteDriver(new DesiredCapabilities(((ChromeOptionsImpl)options).get()));

            if (driver == null)
                throw new SeleniumManagerException("Unsupported driver type: " + browser.getDriverName());
        } catch (SeleniumManagerException e) {
            throw new SeleniumManagerException("Issue provisioning web driver", e);
        }

        return allocatePage(seleniumManager, driver, url, screenshotRasDirectory);
    }

    @Override
    public IWebPage allocateWebPage(String url, IEdgeOptions options) throws SeleniumManagerException {
    	RemoteWebDriver driver = null;

        try {
            driver = remoteDriver(new DesiredCapabilities(((EdgeOptionsImpl)options).get()));

            if (driver == null)
                throw new SeleniumManagerException("Unsupported driver type: " + browser.getDriverName());
        } catch (SeleniumManagerException e) {
            throw new SeleniumManagerException("Issue provisioning web driver", e);
        }

        return allocatePage(seleniumManager, driver, url, screenshotRasDirectory);
    }

    @Override
    public IWebPage allocateWebPage(String url, IInternetExplorerOptions options) throws SeleniumManagerException {
    	RemoteWebDriver driver = null;

        try {
            driver = remoteDriver(new DesiredCapabilities(((InternetExplorerOptionsImpl)options).get()));

            if (driver == null)
                throw new SeleniumManagerException("Unsupported driver type: " + browser.getDriverName());
        } catch (SeleniumManagerException e) {
            throw new SeleniumManagerException("Issue provisioning web driver", e);
        }

        return allocatePage(seleniumManager, driver, url, screenshotRasDirectory);
    }

    @Override
    public IWebPage allocateWebPage(String url, IOperaOptions options) throws SeleniumManagerException {
    	RemoteWebDriver driver = null;

        try {
            driver = remoteDriver(new DesiredCapabilities(((OperaOptionsImpl)options).get()));

            if (driver == null)
                throw new SeleniumManagerException("Unsupported driver type: " + browser.getDriverName());
        } catch (SeleniumManagerException e) {
            throw new SeleniumManagerException("Issue provisioning web driver", e);
        }

        return allocatePage(seleniumManager, driver, url, screenshotRasDirectory);
    }

    @Override
    public IFirefoxOptions getFirefoxOptions() {
        return new FirefoxOptionsImpl();
    }

    @Override
    public IChromeOptions getChromeOptions() {
        return new ChromeOptionsImpl();
    }

    @Override
    public IEdgeOptions getEdgeOptions() {
        return new EdgeOptionsImpl();
    }
    
    @Override
    public IOperaOptions getOperaOptions() {
        return new OperaOptionsImpl();
    }

    @Override
    public IInternetExplorerOptions getInternetExplorerOptions() {
        return new InternetExplorerOptionsImpl();
    }

    public void discard() {
       discardPages();
    }

}