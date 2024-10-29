/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosliberty;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dev.galasa.zosfile.IZosUNIXFile;

/**
 * Represents the server.xml configuration file in a zOS Liberty Server.<p>
 * <b>NOTE:</b> The {@link IZosLibertyServerXml} object may not be the same as the content of the <code>server.xml</code> on the
 * zOS UNIX file system.<br>
 * Use the {@link #loadFromFileSystem()} method to update the object with the content of the <code>server.xml</code> on the
 * zOS UNIX file system.<br>
 * Use the {@link #store()} method to update the the <code>server.xml</code> on the zOS UNIX file system with with the content of the object.<br>
 */
public interface IZosLibertyServerXml {

    /**
     * Replace the Liberty server.xml object from {@link Document} content
     * @param serverXml the server.xml content
     * @throws ZosLibertyServerException 
     */
    public void setFromDocument(Document serverXml) throws ZosLibertyServerException;

    /**
     * Replace the Liberty server.xml object from {@link String} content
     * @param serverXml the server.xml content
     * @throws ZosLibertyServerException 
     */
    public void setFromString(String serverXml) throws ZosLibertyServerException;

    /**
     * Replace the Liberty server.xml object from {@link IZosUNIXFile} content
     * @param serverXml the server.xml content
     * @throws ZosLibertyServerException 
     */
    public void setFromZosUNIXFile(IZosUNIXFile serverXml) throws ZosLibertyServerException;

    /**
     * Get the Liberty server.xml object as a {@link Document}
     * @return the content of the Liberty server.xml
     * @throws ZosLibertyServerException 
     */
    public Document getAsDocument() throws ZosLibertyServerException;

    /**
     * Get the Liberty server.xml object as a {@link String}
     * @return the content of the Liberty server.xml
     * @throws ZosLibertyServerException 
     */
    public String getAsString() throws ZosLibertyServerException;

    /**
     * Get the Liberty server.xml as a {@link IZosUNIXFile}
     * @return the Liberty server.xml file
     * @throws ZosLibertyServerException 
     */
    public IZosUNIXFile getAsZosUNIXFile() throws ZosLibertyServerException;
    
    /**
     * Create or replace the server.xml file on the zOS UNIX file system
     * @throws ZosLibertyServerException
     */
    public void store() throws ZosLibertyServerException;
    
    /**
     * Update the content of the {@link IZosLibertyServerXml} with the content of the server.xml file from the zOS UNIX file system 
     * @throws ZosLibertyServerException
     */
    public void loadFromFileSystem() throws ZosLibertyServerException;

    /**
     * Store the content of the Liberty server.xml to the Results Archive Store
     * @param rasPath path in Results Archive
     * @throws ZosLibertyServerException
     */
    public void saveToResultsArchive(String rasPath) throws ZosLibertyServerException;
    
    /**
     * Get an list of XML elements from the Liberty server.xml Document object.<br>
     * N.B. a list is returned because there may be more than on instance of the named XML element in the Document
     * @param elementName the XML element name
     * @throws ZosLibertyServerException
     */
    public List<Element> getElements(String elementName) throws ZosLibertyServerException;
    
    /**
     * Get an list of XML elements from the Liberty server.xml Document object.<br>
     * N.B. a list is returned because there may be more than on instance of the named XML element in the Document
     * @param elementName the XML element name
     * @param id the id of the named XML element
     * @throws ZosLibertyServerException
     */
    public List<Element> getElementsById(String elementName, String id) throws ZosLibertyServerException;
    
    /**
     * Add a simple XML element to the Liberty server.xml Document object
     * @param elementName the XML element name
     * @param elementAttributes the XML element attributes
     * @return the newly added XML element
     * @throws ZosLibertyServerException
     */
    public Element addElement(String elementName, Map<String, String> elementAttributes) throws ZosLibertyServerException;
    
    /**
     * Add a simple XML element as a child to supplied XML element in the Liberty server.xml Document object
     * @param parent the parent XML element
     * @param elementName the XML element name
     * @param elementAttributes the XML element attributes
     * @return the newly added XML element
     * @throws ZosLibertyServerException
     */
    public Element addElementToParent(Element parent, String elementName, Map<String, String> elementAttributes) throws ZosLibertyServerException;

    /**
     * Add a compound XML element to the Liberty server.xml Document object
     * @param elementName the XML element name
     * @param elementAttributes the XML element attributes
     * @param subElements an {@link IZosLibertyServerXmlElementList} of sub XML elements
     * @return the newly added XML element
     * @throws ZosLibertyServerException
     */
    public Element addCompoundElement(String elementName, Map<String, String> elementAttributes, IZosLibertyServerXmlElementList subElements) throws ZosLibertyServerException;

    /**
     * Add a compound XML element as a child to supplied XML element in the Liberty server.xml Document object 
     * @param parent the parent XML element
     * @param elementName the XML element name
     * @param elementAttributes the XML element attributes
     * @param subElements an {@link IZosLibertyServerXmlElementList} of sub XML elements
     * @return the newly added XML element
     * @throws ZosLibertyServerException
     */
    public Element addCompoundElementToParent(Element parent, String elementName, Map<String, String> elementAttributes, IZosLibertyServerXmlElementList subElements) throws ZosLibertyServerException;

    /**
     * Add or replace attributes in supplied XML element. If any of the supplied attributes match an existing attribute, the value will be replaced otherwise the supplied attribute will be added. 
     * @param element the XML element
     * @param attributes the new XML element attributes
     * @return the modified XML element
     */
    public Element modifyElementAttributes(Element element, Map<String, String> attributes);
    
    /**
     * Add a text context XML element as a child to supplied XML element in the Liberty server.xml Document object 
     * @param parent the parent XML element
     * @param elementName the XML element name
     * @param elementText the XML element text
     * @return the newly added XML element
     * @throws ZosLibertyServerException
     */
    public Element addTextContextElement(Element parent, String elementName, String elementText) throws ZosLibertyServerException;
    
    /**
     * Add one or more XML comment before the supplied XML element
     * @param element the XML element
     * @param comments the comments to add
     * @throws ZosLibertyServerException
     */
    public void addCommentsBefore(Element element, String ... comments) throws ZosLibertyServerException;
    
    /**
     * Remove all XML elements with specified name from the Liberty server.xml Document object
     * @param elementName the XML element name
     * @throws ZosLibertyServerException
     */
    public void removeElements(String elementName) throws ZosLibertyServerException;
    
    /**
     * Remove all XML elements with specified name and id from the Liberty server.xml Document object
     * @param elementName the XML element name
     * @param id the XML element id
     * @throws ZosLibertyServerException
     */
    public void removeElementsById(String elementName, String id) throws ZosLibertyServerException;

    /**
     * Remove an attribute from supplied XML element 
     * @param element the XML element
     * @param attribute the attribute to remove
     * @return the modified XML element
     */
    public Element removeElementAttribute(Element element, String attribute);
    
    /**
     * Remove a text context XML element in the Liberty server.xml Document object
     * @param parent the parent XML element
     * @param elementName the XML element name
     * @param elementText the XML element text
     * @throws ZosLibertyServerException
     */
    public void removeTextContextElement(Element parent, String elementName, String elementText);
    
    /**
     * Create an {@link IZosLibertyServerXmlElementList} 
     * @return {@link IZosLibertyServerXmlElementList}
     */
    public IZosLibertyServerXmlElementList newElementList();
}
