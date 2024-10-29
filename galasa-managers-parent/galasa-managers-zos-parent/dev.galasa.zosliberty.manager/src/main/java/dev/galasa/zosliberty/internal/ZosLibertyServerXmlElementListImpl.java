/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosliberty.internal;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import dev.galasa.zosliberty.IZosLibertyServerXmlElementList;

public class ZosLibertyServerXmlElementListImpl implements IZosLibertyServerXmlElementList
{
    private MultiValuedMap<String, Map<String, String>> elements;

    public ZosLibertyServerXmlElementListImpl()
    {
        this.elements = new ArrayListValuedHashMap<>();
    }

    @Override
    public void add(String elementName, Map<String, String> attributes)
    {
        elements.put(elementName, attributes);
    }

    @Override
    public void clear()
    {
        this.elements.clear();
    }

    @Override
    public Collection<Entry<String, Map<String, String>>> entries() {
        return this.elements.entries();
    }

}
