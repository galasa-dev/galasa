/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosliberty;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

/**
 * An IZosLibertyServerXmlElementList contains a list of XML elements
 */
public interface IZosLibertyServerXmlElementList 
{    
    /**
     * Put an XML element in the element list
     * @param elementName - The name of the element to add
     * @param attributes - A HashMap of attributes
     */
    public void add(String elementName, Map<String, String> attributes);
    
    /**
     * Get a collection of XML elements in the element list
     * @return collection of XML elements
     */
    public Collection<Entry<String, Map<String, String>>> entries();

    /**
     * Clear the XML element list
     */
    public void clear();
}