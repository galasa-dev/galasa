/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.core.manager.internal.properties;

import java.util.ArrayList;
import java.util.List;

import dev.galasa.core.manager.CoreManagerException;
import dev.galasa.core.manager.internal.CorePropertiesSingleton;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;


/**
 * Resource String Pattern
 * 
 * @galasa.cps.property
 * 
 * @galasa.name core.resource.string.[length].pattern
 * 
 * @galasa.description Sets the patterns of resources strings for a length of string.  The patterns are from the 
 *                     Galasa ResourcePoolingService which uses a homegrown syntax.  For each character can be a constant
 *                     or a random choice from a literal,  eg {A-Z} will result in a single character between A and Z inclusive.
 *                     {0-9} or {a-zA-Z0-9} are options.   DFH{A-Z}{0-1}{0-9}{0-9}{0-9},  could result in DFHA1789 for example,
 *                     the 4th character can only be 0 or 1.
 * 
 * @galasa.required No
 * 
 * @galasa.default {A-Z} for each byte for the specified length
 * 
 * @galasa.valid_values Any resource pooling strings comma separated
 * 
 * @galasa.examples 
 * <code>core.resource.string.8.length={A-Z}{A-Z}{A-Z}{A-Z}{A-Z}{A-Z}{A-Z}{A-Z}<br>
 * </code>
 * */
public class ResourceStringPattern extends CpsProperties {

	public static List<String> get(final int length) throws CoreManagerException {
		try {
			List<String> pattern = getStringList(CorePropertiesSingleton.cps(), "resource.string." + Integer.toString(length), "pattern");
			if (!pattern.isEmpty()) {
				return pattern;
			}
			
			ArrayList<String> defaultPattern = new ArrayList<>();
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < length; i++) {
				sb.append("{A-Z}");
			}
			defaultPattern.add(sb.toString());
			return defaultPattern;
		} catch (final ConfigurationPropertyStoreException e) {
			throw new CoreManagerException("Problem asking the CPS for the resource string pattern", e);
		}
	}
}
