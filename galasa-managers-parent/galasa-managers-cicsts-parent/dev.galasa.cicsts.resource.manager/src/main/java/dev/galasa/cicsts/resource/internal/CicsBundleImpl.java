/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.resource.internal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import dev.galasa.artifact.IArtifactManager;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.ISkeletonProcessor.SkeletonType;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.cicsts.CicstsHashMap;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.cicsresource.CicsBundleResourceException;
import dev.galasa.cicsts.cicsresource.CicsJvmserverResourceException;
import dev.galasa.cicsts.cicsresource.CicsResourceManagerException;
import dev.galasa.cicsts.cicsresource.CicsResourceStatus;
import dev.galasa.cicsts.cicsresource.ICicsBundle;
import dev.galasa.cicsts.resource.internal.properties.DefaultResourceTimeout;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.IZosUNIXFile.UNIXFileDataType;
import dev.galasa.zosfile.ZosUNIXFileException;

public class CicsBundleImpl implements ICicsBundle {
    
    private static final Log logger = LogFactory.getLog(CicsBundleImpl.class);

	private IBundleResources testBundleResources;
    private CicsResourceManagerImpl cicsResourceManager;
	private IArtifactManager artifactManager;
    private ICicsRegion cicsRegion;
    private ICicsTerminal cicsTerminal;
    private Class<?> testClass;
    private IZosImage cicsZosImage;
    private IZosUNIXFile runTemporaryUNIXPath;
    private IZosFileHandler zosFileHandler;
	private String localBundlePath;
	private Map<String, Object> parameters = new HashMap<>();
	private boolean shouldDeploy;
	private List<CicsBundleComponent> cicsBundleComponents = new ArrayList<>();
    
    private String resourceDefinitionName;
    private String resourceDefinitionGroup;
    private String resourceDefinitionDescription;
    private CicsResourceStatus resourceDefinitionStatus = CicsResourceStatus.ENABLED;
    private String resourceDefinitionBundledir;

	private int defaultTimeout;

    private static final String SLASH_SYBMOL = "/";

	private static final String RESOURCE_TYPE_BUNDLE = "BUNDLE";

	public CicsBundleImpl(CicsResourceManagerImpl cicsResourceManager, ICicsRegion cicsRegion, ICicsTerminal cicsTerminal, Class<?> testClass, String name, String group, String bundlePath, String bunndledir, Map<String, String> parameters) throws CicsBundleResourceException {
        this.cicsResourceManager = cicsResourceManager;
        this.cicsResourceManager.registerCicsBundle(this);
        this.artifactManager = this.cicsResourceManager.getArtifactManager();
        try {
            this.zosFileHandler = this.cicsResourceManager.getZosFileHandler();
        } catch (CicsResourceManagerException e) {
            throw new CicsBundleResourceException("Unable to get zOS File Handler", e);
        }
        this.cicsRegion = cicsRegion;
        this.cicsZosImage = cicsRegion.getZosImage();
        this.cicsTerminal = cicsTerminal;
        this.testClass = testClass;
        this.resourceDefinitionName = name;
        this.resourceDefinitionGroup = group;
        try {        	                          
			this.runTemporaryUNIXPath = this.zosFileHandler.newUNIXFile(cicsRegion.getRunTemporaryUNIXDirectory().getUnixPath() + "CICSBundles" + SLASH_SYBMOL + getName() + SLASH_SYBMOL, this.cicsZosImage);
		} catch (CicstsManagerException | ZosUNIXFileException e) {
			throw new CicsBundleResourceException("Unable to get run temporary UNIX path", e);
		}
        // CICS bundle source already stored on file system 
        if (bundlePath == null) {
            this.shouldDeploy = false;
            this.resourceDefinitionBundledir = bunndledir;
        } else {
            this.shouldDeploy = true;
	        if (bundlePath.endsWith(SLASH_SYBMOL)) {
	        	this.localBundlePath = bundlePath;
	        } else {
	        	this.localBundlePath = bundlePath + SLASH_SYBMOL;
	    	}
	    	String root = new File(this.localBundlePath).getName();
	    	this.resourceDefinitionBundledir = this.runTemporaryUNIXPath.getUnixPath() + root + SLASH_SYBMOL;
	        if (parameters != null && !parameters.isEmpty()) {
	        	this.parameters.putAll(parameters);
	        }
    		this.testBundleResources = this.artifactManager.getBundleResources(this.testClass);
        }
	}

	@Override
	public void setDefinitionDescriptionAttribute(String value) {
		this.resourceDefinitionDescription = value;
	}

	@Override
	public void setDefinitionStatusAttribute(CicsResourceStatus value) {
		this.resourceDefinitionStatus = value;
	}

    @Override
    public String getResourceDefinitionNameAttribute() {
        return this.resourceDefinitionName;
    }

    @Override
    public String getResourceDefinitionGroupAttribute() {
        return this.resourceDefinitionGroup;
    }

    @Override
    public String getResourceDefinitionDescriptionAttribute() {
        return this.resourceDefinitionDescription;
    }

    @Override
    public CicsResourceStatus getResourceDefinitionStatusAttribute() {
        return this.resourceDefinitionStatus;
    }

    @Override
    public String getResourceDefinitionBundledirAttribute() {
        return this.resourceDefinitionBundledir;
    }	

	@Override
	public void buildResourceDefinition() throws CicsBundleResourceException {
        try {
            if (resourceDefined()) {
                throw new CicsBundleResourceException(RESOURCE_TYPE_BUNDLE + " " + getName() + " already exists");
            }
            boolean setUcctran = false;
            if (this.cicsTerminal.isUppercaseTranslation() == true) {
            	this.cicsTerminal.setUppercaseTranslation(false);
            	setUcctran = true;
            }
            this.cicsRegion.ceda().createResource(this.cicsTerminal, RESOURCE_TYPE_BUNDLE, getName(), getResourceDefinitionGroupAttribute(), buildResourceParameters());
            if (setUcctran) {
            	this.cicsTerminal.setUppercaseTranslation(true);
            }

            if (!resourceDefined()) {
                throw new CicsBundleResourceException("Failed to define " + RESOURCE_TYPE_BUNDLE + " resource definition");
            } 
        } catch (CicstsManagerException e) {
            throw new CicsBundleResourceException("Unable to build " + RESOURCE_TYPE_BUNDLE + " resource definition", e);
        }
	}
	
	@Override
	public void deploy() throws CicsBundleResourceException {
		if (!this.shouldDeploy) {
			throw new CicsBundleResourceException("The CICS bundle content was not supplied when the ICicsBundle was created");
		}
        try {
        	findComponents();
        	for (CicsBundleComponent bundleComponent : this.cicsBundleComponents) {
    			logger.debug("Copying file " + bundleComponent.localPath + " to " + bundleComponent.targetPath);

                IZosUNIXFile bundleFile = this.zosFileHandler.newUNIXFile(bundleComponent.targetPath, cicsZosImage);
                if (bundleComponent.type.isBinaryBundleResource()) {
                	bundleFile.setDataType(UNIXFileDataType.BINARY);
                } else {
                	bundleFile.setDataType(UNIXFileDataType.TEXT);
                }
                if (!bundleFile.exists()) {
                    bundleFile.create(PosixFilePermissions.fromString("rwxrwxrwx"));
                }
                bundleFile.storeBinary(bundleComponent.content);
    		}
        } catch (ZosUNIXFileException e) {
            throw new CicsBundleResourceException("Problem deploying CICS bundle to zOS UNIX file system", e);
        }
	}

	private void findComponents() throws CicsBundleResourceException {
    	try {
    		String localPath = this.localBundlePath + "META-INF/cics.xml";
    		String targetPath = this.getResourceDefinitionBundledirAttribute() + "META-INF/cics.xml";
    		byte[] content = IOUtils.toByteArray(this.testBundleResources.retrieveSkeletonFile(localPath, this.parameters, SkeletonType.VELOCITY));
    		CicsBundleComponent cicsBundleComponent = new CicsBundleComponent(localPath, targetPath, content, CicsBundleResourceType.CICSXML);
			this.cicsBundleComponents.add(cicsBundleComponent);
    		NodeList nodes = getDocument(cicsBundleComponent).getElementsByTagName("define");
    		for (int i = 0; i < nodes.getLength(); i++) {
    			NamedNodeMap attributes = nodes.item(i).getAttributes();
    			String type = attributes.getNamedItem("type").getNodeValue();
    			String path = attributes.getNamedItem("path").getNodeValue();
    			localPath = this.localBundlePath + path;
    			targetPath = getResourceDefinitionBundledirAttribute() + path;
    			content = IOUtils.toByteArray(this.testBundleResources.retrieveSkeletonFile(localPath, this.parameters, SkeletonType.VELOCITY));
    			CicsBundleResourceType componentType = CicsBundleResourceType.valueOf(new File(type).getName());
    			cicsBundleComponent = new CicsBundleComponent(localPath, targetPath, content, componentType);
    			this.cicsBundleComponents.add(cicsBundleComponent);
    			if (componentType.getSubComponentType() != null) {
    				parseCicsBundleComponent(cicsBundleComponent);
    			}
            }
    	} catch (CicsBundleResourceException | TestBundleResourceException | IOException e) {
    		throw new CicsBundleResourceException("Problem retrieving the CICS bundle files from the test bundle", e);
		}
	}

	private void parseCicsBundleComponent(CicsBundleComponent cicsBundleComponent) throws CicsBundleResourceException {
		try {
			NodeList nodes = getDocument(cicsBundleComponent).getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
	    		String localPath;
	    		String targetPath;
	    		byte[] content;
				
				Element element = (Element) nodes.item(i);
				String nodeName = element.getTagName();
				String symbolicName = element.getAttribute("symbolicname");
				if (nodeName.equals("osgibundle")) {
					String version = element.getAttribute("version");
					String fileName = symbolicName + "_" + version + ".jar";
					localPath = this.localBundlePath + fileName;
					targetPath = getResourceDefinitionBundledirAttribute() + fileName;
					content = IOUtils.toByteArray(this.testBundleResources.retrieveJar(symbolicName, version, this.localBundlePath));
				} else if (nodeName.equals("nodejsapp")) {
					//TODO !!??
					throw new CicsBundleResourceException("nodejsapp not yet implemented");
				} else {
					String fileName = symbolicName + "." + cicsBundleComponent.type.getSubComponentType().toString().toLowerCase();
					localPath = this.localBundlePath + fileName;
					targetPath = getResourceDefinitionBundledirAttribute() + fileName;
					content = IOUtils.toByteArray(this.testBundleResources.retrieveFile(localPath));
				}
				this.cicsBundleComponents.add(new CicsBundleComponent(localPath, targetPath , content, cicsBundleComponent.type.getSubComponentType()));
			}
		} catch (CicsBundleResourceException | TestBundleResourceException | IOException e) {
			throw new CicsBundleResourceException("Problem parsing bundle component", e);
		}
	}

	private Document getDocument(CicsBundleComponent cicsBundleComponent) throws CicsBundleResourceException {
		try {
			logger.trace("Parsing " + cicsBundleComponent.localPath);
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	        Document document = documentBuilderFactory.newDocumentBuilder().parse(new ByteArrayInputStream(cicsBundleComponent.content));
    		logger.debug("Content:" + "\n" + documentToString(document));
	        return document;
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new CicsBundleResourceException("Problem retrieving content of \"" + cicsBundleComponent.localPath + "\" from the test bundle", e);
		}
	}
	
    protected String documentToString(Document document) throws CicsBundleResourceException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            // Remove blank lines
            document.normalize();
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nodeList = (NodeList) xPath.evaluate("//text()[normalize-space()='']", document, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                node.getParentNode().removeChild(node);
            }
            
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            Source source = new DOMSource(document);            
            StringWriter stringWriter = new StringWriter();
            StreamResult result = new StreamResult(stringWriter);
            transformer.transform(source, result);
            return stringWriter.toString();
        } catch (XPathExpressionException | TransformerException e) {
            throw new CicsBundleResourceException("Unable to convert server.xml org.w3c.dom.Document to java.lang.String");
        }
    }

	@Override
	public void buildInstallResourceDefinition() throws CicsBundleResourceException {
        buildResourceDefinition();
        if (this.shouldDeploy) {
        	deploy();
        }
        installResourceDefinition();
	}

	@Override
	public void installResourceDefinition() throws CicsBundleResourceException {
        try {
            if (resourceInstalled()) {
                throw new CicsBundleResourceException(RESOURCE_TYPE_BUNDLE + " " + getName() + " already installed");
            }
            this.cicsRegion.ceda().installResource(this.cicsTerminal, RESOURCE_TYPE_BUNDLE, getName(), this.resourceDefinitionGroup);
            //TODO: should return messages????
            if (!resourceInstalled()) {
                throw new CicsBundleResourceException("Failed to install " + RESOURCE_TYPE_BUNDLE + " resource definition");
            }
        } catch (CicstsManagerException e) {
            throw new CicsBundleResourceException("Unable to install " + RESOURCE_TYPE_BUNDLE + " resource definition", e);
        }
	}

	@Override
	public boolean resourceDefined() throws CicsBundleResourceException {
        try {
            return this.cicsRegion.ceda().resourceExists(this.cicsTerminal, RESOURCE_TYPE_BUNDLE, getName(), resourceDefinitionGroup);
        } catch (CicstsManagerException e) {
            throw new CicsBundleResourceException("Unable to display " + RESOURCE_TYPE_BUNDLE + " resource definition", e);
        }
	}

	@Override
	public boolean resourceInstalled() throws CicsBundleResourceException {
        try {
            return this.cicsRegion.cemt().inquireResource(this.cicsTerminal, RESOURCE_TYPE_BUNDLE, getName()) != null;
        } catch (CicstsManagerException e) {
            throw new CicsBundleResourceException("Unable to inquire " + RESOURCE_TYPE_BUNDLE + "", e);
        }
	}

	@Override
	public void enable() throws CicsBundleResourceException {
        try {
            if (!resourceInstalled()) {
                throw new CicsBundleResourceException(RESOURCE_TYPE_BUNDLE + " " + getName() + " does not exist");
            }
            this.cicsRegion.cemt().setResource(this.cicsTerminal, RESOURCE_TYPE_BUNDLE, getName(), "ENABLED");
        } catch (CicstsManagerException e) {
            throw new CicsBundleResourceException("Problem enabling " + RESOURCE_TYPE_BUNDLE + " " + getName(), e);
        }
	}

	@Override
	public boolean waitForEnable() throws CicsBundleResourceException {
        return waitForEnable(getDefaultTimeout());
	}

	@Override
	public boolean waitForEnable(int timeout) throws CicsBundleResourceException {
        logger.trace("Waiting " + timeout + " second(s) for " + RESOURCE_TYPE_BUNDLE + " " +  getName() + " to be enabled");
	    LocalDateTime timeoutTime = LocalDateTime.now().plusSeconds(timeout);
	    while (LocalDateTime.now().isBefore(timeoutTime)) {
            if (isEnabled()) {
                return true;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new CicsBundleResourceException("Interrupted during wait", e);
            }
        }
        return isEnabled();
	}

	@Override
	public boolean isEnabled() throws CicsBundleResourceException {
        if (!resourceInstalled()) {
            return false;
        }
        boolean enabled = cemtInquire().isParameterEquals("enablestatus", CicsResourceStatus.ENABLED.toString());
        if (enabled) {
            logger.trace(RESOURCE_TYPE_BUNDLE + " " +  getName() + " is enabled");
        } else {
            logger.trace(RESOURCE_TYPE_BUNDLE + " " +  getName() + " is NOT enabled");
        }
        return enabled;
	}

	@Override
	public boolean disable() throws CicsBundleResourceException {
        try {
            if (!resourceInstalled()) {
                throw new CicsJvmserverResourceException(RESOURCE_TYPE_BUNDLE + " " + getName() + " does not exist");
            }
            this.cicsRegion.cemt().setResource(this.cicsTerminal, RESOURCE_TYPE_BUNDLE, getName(), "DISABLED");
        } catch (CicstsManagerException e) {
            throw new CicsBundleResourceException("Problem disabling " + RESOURCE_TYPE_BUNDLE + " " + getName(), e);
        }
        return isEnabled();
	}

	@Override
	public boolean waitForDisable() throws CicsBundleResourceException {
        return waitForDisable(getDefaultTimeout());
	}

	@Override
	public boolean waitForDisable(int timeout) throws CicsBundleResourceException {
        logger.trace("Waiting " + timeout + " second(s) for " + RESOURCE_TYPE_BUNDLE + " " +  getName() + " to be disabled");
	    LocalDateTime timeoutTime = LocalDateTime.now().plusSeconds(timeout);
	    while (LocalDateTime.now().isBefore(timeoutTime)) {
            if (!isEnabled()) {
                return true;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new CicsBundleResourceException("Interrupted during wait", e);
            }
        }
        if (isEnabled()) {
            throw new CicsBundleResourceException(RESOURCE_TYPE_BUNDLE + " " + getName() + " not disabled in " + timeout + " second(s)");
        }
        return true;
	}

	@Override
	public boolean disableDiscardInstall() throws CicsBundleResourceException {
		return disableDiscardInstall(getDefaultTimeout());
	}

	@Override
	public boolean disableDiscardInstall(int timeout) throws CicsBundleResourceException {
		disable();
		waitForDisable(timeout);
		discard();
		installResourceDefinition();
		return waitForEnable();
	}

	@Override
	public void delete() throws CicsBundleResourceException {
        try {
            if (resourceDefined()) {
                this.cicsRegion.ceda().deleteResource(this.cicsTerminal, RESOURCE_TYPE_BUNDLE, getName(), resourceDefinitionGroup);
            }
        } catch (CicstsManagerException e) {
        	throw new CicsBundleResourceException("Problem deleteing " + RESOURCE_TYPE_BUNDLE + " " + getName(), e);
        }
	}

	@Override
	public void discard() throws CicsBundleResourceException {
        try {
            if (resourceInstalled()) {
                this.cicsRegion.cemt().discardResource(cicsTerminal, RESOURCE_TYPE_BUNDLE, getName());
	            if (resourceInstalled()) {
	            	throw new CicsBundleResourceException(RESOURCE_TYPE_BUNDLE + " was not discarded" + getName());
	            }
            }
        } catch (CicstsManagerException e) {
            throw new CicsBundleResourceException("Problem discarding " + RESOURCE_TYPE_BUNDLE + " " + getName(), e);
        }
	}

	@Override
	public void disableDiscardDelete() throws CicsBundleResourceException {
        disable();
        waitForDisable();
        discard();
        delete();
	}

	@Override
	public void build() throws CicsBundleResourceException {
        try {
            if (this.shouldDeploy) {
            	deploy();
            }
            buildResourceDefinition();
            installResourceDefinition();
        } catch (CicsBundleResourceException e) {
            throw new CicsBundleResourceException("Problem building " + RESOURCE_TYPE_BUNDLE + " " + getName(), e);
        }
	}	

	@Override
	public String getName() {
		return this.resourceDefinitionName;
	}

	@Override
    public String toString() {
        return "[CICS Bundle] " + getName();
    }

    protected int getDefaultTimeout() throws CicsBundleResourceException {
        if (this.defaultTimeout == -1) {
            try {
                this.defaultTimeout = DefaultResourceTimeout.get(this.cicsZosImage);
            } catch (CicsResourceManagerException e) {
                throw new CicsBundleResourceException("Problem creating getting default resource timeout", e);
            }
        }
        return this.defaultTimeout;
    }

    protected String buildResourceParameters() {
        StringBuilder resourceParameters = new StringBuilder();
        appendNotNull(resourceParameters, "DESCRIPTION", getResourceDefinitionDescriptionAttribute());
        appendNotNull(resourceParameters, "STATUS", getResourceDefinitionStatusAttribute().toString());
        appendNotNull(resourceParameters, "BUNDLEDIR", getResourceDefinitionBundledirAttribute());
        return resourceParameters.toString();
    }

    protected StringBuilder appendNotNull(StringBuilder resourceParameters, String attribute, String value) {
        if (value != null && !value.isEmpty()) {
            resourceParameters.append(attribute);
            resourceParameters.append(" (");
            resourceParameters.append(value);
            resourceParameters.append(") ");
        }
        return resourceParameters;
    }

    protected CicstsHashMap cemtInquire() throws CicsBundleResourceException {
        if (!resourceInstalled()) {
            throw new CicsBundleResourceException("JVMSERVER " + getName() + " does not exist");
        }
        CicstsHashMap cemtMap;
        try {
            cemtMap = this.cicsRegion.cemt().inquireResource(this.cicsTerminal, RESOURCE_TYPE_BUNDLE, getName());
        } catch (CicstsManagerException e) {
            throw new CicsBundleResourceException("Problem inquiring JVMSERVER " + getName(), e);
        }
        return cemtMap;
    }
	
	protected void cleanup() {
        try {
            if (!resourceInstalled()) {
                logger.info(RESOURCE_TYPE_BUNDLE + " " + getName() + " has not been installed");
            } else {
                try {
                    disable();
                    waitForDisable();
                } catch (CicsBundleResourceException e) {
                    logger.error("Problem in cleanup phase", e);
                }
                try {
                    discard();
                } catch (CicsBundleResourceException e) {
                    logger.error("Problem in cleanup phase", e);
                }
            }
        } catch (CicstsManagerException e) {
            logger.error("Problem in cleanup phase", e);
        }
        try {
            delete();
        } catch (CicsBundleResourceException e) {
            logger.error("Problem in cleanup phase", e);
        }
    }
	
	private class CicsBundleComponent {

		private String localPath;
		private String targetPath;
		private byte[] content;
		private CicsBundleResourceType type;

		private CicsBundleComponent(String localPath, String targetPath, byte[] content, CicsBundleResourceType type) {
			this.localPath = localPath;
			this.targetPath = targetPath;
			this.content = content;
			this.type = type;
		}
	}
}
