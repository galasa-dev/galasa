/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosliberty.internal;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
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

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.IZosUNIXFile.UNIXFileDataType;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosliberty.IZosLibertyServerXml;
import dev.galasa.zosliberty.IZosLibertyServerXmlElementList;
import dev.galasa.zosliberty.ZosLibertyServerException;

public class ZosLibertyServerXmlImpl implements IZosLibertyServerXml {
    
    private IZosUNIXFile serverXmlUnixfile;
    private Document serverXmlDocument;

    public ZosLibertyServerXmlImpl(IZosUNIXFile serverXmlFile) {
        this.serverXmlUnixfile = serverXmlFile;
        this.serverXmlUnixfile.setDataType(UNIXFileDataType.BINARY);
    }

    @Override
    public void setFromDocument(Document content) {
        this.serverXmlDocument = content;
    }

    @Override
    public void setFromString(String content) throws ZosLibertyServerException {
        this.serverXmlDocument = stringToDocument(content);
    }

    @Override
    public void setFromZosUNIXFile(IZosUNIXFile content) throws ZosLibertyServerException {
        try {
            if (content.exists()) {
                content.setDataType(UNIXFileDataType.BINARY);
                byte[] fromContent = content.retrieveAsBinary();
                stringToDocument(new String(fromContent, StandardCharsets.UTF_8));
                this.serverXmlUnixfile.storeBinary(fromContent);
            } else {
                throw new ZosLibertyServerException("File " + content.getUnixPath() + " does not exist");
            }
        } catch (ZosUNIXFileException e) {
            throw new ZosLibertyServerException("Problem creating server.xml from " + content.getUnixPath(), e);
        }
    }

    @Override
    public String getAsString() throws ZosLibertyServerException {
        return documentToString(getAsDocument());
    }

    @Override
    public Document getAsDocument() throws ZosLibertyServerException {
        if (this.serverXmlDocument == null) {
            throw new ZosLibertyServerException("server.xml document is null");
        }
        return this.serverXmlDocument;
    }

    @Override
    public IZosUNIXFile getAsZosUNIXFile() throws ZosLibertyServerException {
        return this.serverXmlUnixfile;
    }

    @Override
    public void store() throws ZosLibertyServerException {
        try {
            if (!this.serverXmlUnixfile.exists()) {
                this.serverXmlUnixfile.create(PosixFilePermissions.fromString("rwxrwxrwx"));
            } else {
                this.serverXmlUnixfile.setShouldCleanup(false);
            }
            this.serverXmlUnixfile.setDataType(UNIXFileDataType.BINARY);
            this.serverXmlUnixfile.storeBinary(documentToString(getAsDocument()).getBytes());
        } catch (ZosUNIXFileException e) {
            throw new ZosLibertyServerException("Unable to build server.xml file on the zOS UNIX file system", e);
        }
    }
    
    @Override
    public void loadFromFileSystem() throws ZosLibertyServerException {
        String serverXmlString;
        try {
            serverXmlString = new String(this.serverXmlUnixfile.retrieveAsBinary(), StandardCharsets.UTF_8);
        } catch (ZosUNIXFileException e) {
            throw new ZosLibertyServerException("Unable to retrieve server.xml from zOS UNIX file system", e);
        }
        this.serverXmlDocument = stringToDocument(serverXmlString);
    }

    @Override
    public void saveToResultsArchive(String rasPath) throws ZosLibertyServerException {
        try {
            if (this.serverXmlUnixfile.exists()) {
                this.serverXmlUnixfile.saveToResultsArchive(rasPath);
            }
        } catch (ZosUNIXFileException e) {
            throw new ZosLibertyServerException("Unable to store the content of the Liberty server.xml to the Results Archive Store", e);
        }
    }

    @Override
    public List<Element> getElements(String elementName) throws ZosLibertyServerException {
        List<Element> elementList = new ArrayList<>();         
        NodeList nodes = getAsDocument().getElementsByTagName(elementName);
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                elementList.add((Element) nodes.item(i));
            }                
        }
        return elementList;
    }
    
    @Override
    public List<Element> getElementsById(String elementName, String id) throws ZosLibertyServerException {
        List<Element> elementList = new ArrayList<>();
        List<Element> elements = getElements(elementName);
        for (Element element : elements) {
            String idAttribute = element.getAttribute("id");
            if (idAttribute != null && idAttribute.equals(id)) {
                elementList.add(element);
            }
        }
        return elementList;
    }
    
    @Override
    public Element addElement(String elementName, Map<String, String> elementAttributes) throws ZosLibertyServerException {
        return addElementToParent((Element) getParentNode(), elementName, elementAttributes);
    }

    @Override
    public Element addElementToParent(Element parent, String elementName, Map<String, String> elementAttributes) throws ZosLibertyServerException {
        Element element = getAsDocument().createElement(elementName);
        parent.appendChild(element);
        if (elementAttributes != null) {
            for (Entry<String, String> entry : elementAttributes.entrySet()) {
                element.setAttribute(entry.getKey(), entry.getValue());
            }
        }
        return element;
    }

    @Override
    public Element addCompoundElement(String elementName, Map<String, String> attributes, IZosLibertyServerXmlElementList subElements) throws ZosLibertyServerException {
        Element parent = addElement(elementName, attributes);
        if (subElements != null) {
            for (Entry<String, Map<String, String>> element    : subElements.entries()) {
                addElementToParent(parent, element.getKey(), element.getValue());
            }
        }
        
        return parent;        
    }

    @Override
    public Element addCompoundElementToParent(Element parent, String elementName, Map<String, String> attributes, IZosLibertyServerXmlElementList subElements) throws ZosLibertyServerException {
        parent = addElementToParent(parent, elementName, attributes);
        if (subElements != null) {
            for (Entry<String, Map<String, String>> element    : subElements.entries()) {
                addElementToParent(parent, element.getKey(), element.getValue());
            }
        }
        
        return parent;        
    }

    @Override
    public Element modifyElementAttributes(Element element, Map<String, String> attributes) {
        Element newElement = (Element) element.cloneNode(false);
        for (Entry<String, String> entry : attributes.entrySet()) {
            newElement.setAttribute(entry.getKey(), entry.getValue());
        }
        Node parent = element.getParentNode();
        parent.removeChild(element);
        parent.appendChild(newElement);
        return newElement;
    }

    @Override
    public Element addTextContextElement(Element parent, String elementName, String elementText) throws ZosLibertyServerException {
        Element element = getAsDocument().createElement(elementName);
        element.setTextContent(elementText.trim());
        parent.appendChild(element);
        return element;
    }

    @Override
    public void addCommentsBefore(Element element, String ... comments) throws ZosLibertyServerException {
        for (String comment : comments) {
            Comment commentElement = getAsDocument().createComment(comment);
            element.getParentNode().insertBefore(commentElement, element);
        }
    }

    @Override
    public void removeElements(String elementName) throws ZosLibertyServerException {
        List<Element> elementsToDelete = getElements(elementName);
        for (Element element : elementsToDelete) {
            element.getParentNode().removeChild(element);
        }
    }

    @Override
    public void removeElementsById(String elementName, String id) throws ZosLibertyServerException {
        List<Element> elementsToDelete = getElementsById(elementName, id);
        for (Element element : elementsToDelete) {
            element.getParentNode().removeChild(element);
        }
    }
    
    @Override
    public Element removeElementAttribute(Element element, String attribute) {
        element.removeAttribute(attribute);
        return element;
    }

    @Override
    public void removeTextContextElement(Element parent, String elementName, String elementText) {        
        NodeList nodes = parent.getElementsByTagName(elementName);
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i).getTextContent() != null && nodes.item(i).getTextContent().trim().equals(elementText)) {
                parent.removeChild(nodes.item(i));
            }                
        }
    }

    @Override
    public IZosLibertyServerXmlElementList newElementList() {
        return new ZosLibertyServerXmlElementListImpl();
    }

    @Override
    public String toString() {
        return "[zOS Liberty server.xml] " + (this.serverXmlUnixfile != null? this.serverXmlUnixfile.getUnixPath(): "");
    }

    protected Document stringToDocument(String content) throws ZosLibertyServerException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(content)));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new ZosLibertyServerException("Unable to convert server.xml java.lang.String to org.w3c.dom.Document");
        }
    }

    protected String documentToString(Document document) throws ZosLibertyServerException {
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
            throw new ZosLibertyServerException("Unable to convert server.xml org.w3c.dom.Document to java.lang.String");
        }
    }

    private Node getParentNode() throws ZosLibertyServerException {
        return getAsDocument().getElementsByTagName("server").item(0);
    }

}
