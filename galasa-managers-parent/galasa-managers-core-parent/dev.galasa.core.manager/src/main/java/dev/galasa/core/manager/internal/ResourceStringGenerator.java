/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.core.manager.internal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.core.manager.CoreManagerException;
import dev.galasa.core.manager.ResourceString;
import dev.galasa.core.manager.internal.properties.ResourceStringPattern;
import dev.galasa.framework.spi.DssAdd;
import dev.galasa.framework.spi.DssDelete;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreMatchException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IResourcePoolingService;
import dev.galasa.framework.spi.InsufficientResourcesAvailableException;
import dev.galasa.framework.spi.ResourceUnavailableException;

/**
 * Generate Resource Strings using random letters
 * 
 * 
 *  
 *
 */
public class ResourceStringGenerator {

	private final static Log                    logger = LogFactory.getLog(ResourceStringGenerator.class);

	private CoreManagerImpl                     coreManager;
	// Maintain a map of what tags have already been provisioned so duplicates are not created
	// Also maintain a list so the Resource Strings can be discarded at the end of the run
	private HashMap<String, ResourceStringImpl> resourceStrings = new HashMap<>();


	public ResourceStringGenerator(CoreManagerImpl coreManager)   {
		this.coreManager = coreManager;
	}

	/**
	 * Attempt to generate a string for a field
	 * 
	 * @param field - The field to provision for
	 * @return A resource string implementation
	 * @throws CoreManagerException
	 * @throws ResourceUnavailableException
	 */
	public ResourceStringImpl generateString(Field field) throws CoreManagerException, ResourceUnavailableException {
		// Get the @ResourceString annotation, with the length and tag
		ResourceString annotation = field.getAnnotation(ResourceString.class);
		int stringLength = annotation.length();
		String tag       = annotation.tag();
		
		// Make sure we have a tag, if missing default to PRIMARY
		if (tag == null || tag.trim().isEmpty()) {
			tag = "PRIMARY";
		}
		
		// Always check that the tag hasn't already been provisioned,  if the test refers to 
		// the same tag, then they must get the same instantiated object
		ResourceStringImpl resourceString = resourceStrings.get(tag);
		if (resourceString != null) {
			if (resourceString.getLength() != stringLength) {
				throw new CoreManagerException("Resource string with tag " + tag + " used multiple times with different string lengths");
			}
			return resourceString;
		}
		
		// Get the utility routines
		IResourcePoolingService rps    = this.coreManager.getFramework().getResourcePoolingService();
		IDynamicStatusStoreService dss = this.coreManager.getDss();
		String runName                 = this.coreManager.getRunName();

		// Retrieve the string patterns for this length, or the default if not provided in the CPS
		List<String> stringPatterns = ResourceStringPattern.get(stringLength); 

		// Keep a record of what has been rejected
		ArrayList<String> rejectedResourceStrings = new ArrayList<>();
		while(resourceString == null) {
			try {
				// Ask the ResourcePoolingService for a set of possible strings
				// Normally we would ask for a set of 10 non-continous Strings,  but in case the length of the string is 1
				// we will ask for 1 at a time so can make the full use of {A-Z} or whatever is provided
				// The RPS will check the string isn't already allocted via the dss.core.resource.string.X key
				List<String> possibleResourceStrings = rps.obtainResources(stringPatterns, rejectedResourceStrings, 1, 1, dss, "resource.string.");
				// Go through the possibles and attempt to reserve them
				// Although small,  there is a chance that a possible string is reserved by another test between 
				// the RPS checking and us reserving.
				for(String possibleResourceString : possibleResourceStrings) {
					// Attempt to reserve the string
					if (reserveResourceString(dss, runName, possibleResourceString)) {
						// We managed to reserve it, create an instance
						resourceString = new ResourceStringImpl(possibleResourceString, stringLength);
						// Save it in our list, for discard and duplicate fields
						this.resourceStrings.put(tag, resourceString);
						logger.info("Resource string '" + resourceString.getString() + "' assigned to tag " + tag);
						break;
					} else {
						// Another test just reserved the string, so reject it and go around.  By rejecting it, it 
						// will speed up the RPS process
						rejectedResourceStrings.add(possibleResourceString);
					}
				}
			} catch(InsufficientResourcesAvailableException e) {
				// There are no strings available,  so inform the framework that we should go into wait state and try again later
				// All resources (from other managers) will be discarded by using this
				throw new ResourceUnavailableException("There are no Resource Strings available for generation");
			}
		}

		return resourceString;
	}

	/**
	 * Discard the resource strings we provisioned
	 */
	public void discard() {
		// Get the DSS and the runname
		IDynamicStatusStoreService dss = this.coreManager.getDss();
		String runName                 = this.coreManager.getRunName();
		
		// For each string we provisioned, discard it
		for(Entry<String, ResourceStringImpl> entry : this.resourceStrings.entrySet()) {
			try {
				// Call a STATIC method so the same code can be used from the Resource Management server
				discardResourceString(dss, runName, entry.getValue().getString());
			} catch (CoreManagerException | DynamicStatusStoreMatchException e) {
				// If discard failed, ignore but log, let the Resource Management server clean up when it can
				logger.warn("Failed to discard resource string tagged " + entry.getKey(), e);
			}
		}
	}

	public static boolean reserveResourceString(IDynamicStatusStoreService dss, String runName, String possibleResourceString) throws CoreManagerException {
		
		// Create the DSS entries to cover this resource.   If the Resource String we want to reserve is BAX,  then we will create 2 keys, assuming runname or B1
		//      dss.core.resource.string.BAX = B1
		//      dss.run.B1.resource.string.BAX = active
		//
		// The first key is what reserves the string and records which run has it
		// The second key is a list of strings a run has,  so if a run is cleaned up, we can delete all the resource strings it had, 
		// the active is a literal, just to have something in the value.
		
		DssAdd owner = new DssAdd("resource.string." + possibleResourceString, runName);
		DssAdd byRun = new DssAdd("run." + runName + ".resource.string." + possibleResourceString, "active");
		
		try {
			// add the entries, they both need to be added and not exist beforehand
			dss.performActions(owner, byRun);
			
			// After this point, this is where you would reserve the resources in your server if necessary, but in this case it is a virtual resource, so not.
			// DO NOT build anything at this point, that should be done in provision build		
		} catch (DynamicStatusStoreMatchException e) {
			// One of the keys already exists, prob another test reserved them a millisecond before we tried to, not an error, go and try another one
			return false;
		} catch (DynamicStatusStoreException e) {
			// Something seriously wrong with the DSS, grounds to run termination
			throw new CoreManagerException("Problem updating the DSS with a new resource string reservation", e);
		}
		
		return true;
	}
	
	/**
	 * Discard the the resource string, called from the test run or resource management cleanup
	 * 
	 * @param dss
	 * @param runName
	 * @param resourceString
	 * @throws DynamicStatusStoreMatchException
	 * @throws CoreManagerException
	 */
	public static void discardResourceString(IDynamicStatusStoreService dss, String runName, String resourceString) throws DynamicStatusStoreMatchException, CoreManagerException {
		// Build the keys and the values they should have been.
		// Set the values to what they should be, so in case stuff got out of sync, we don't break another run.  Highly unlikely, but you never know
		DssDelete owner = new DssDelete("resource.string." + resourceString, runName);
		DssDelete byRun = new DssDelete("run." + runName + ".resource.string." + resourceString, "active");
		
		// Under normal circumstances should one of the keys have been deleted without the other.  An admin may have done, but they will clean the other one as well
		// But normal operations, the keys should always be deleted as a pair
		
		try {
			// Here you would attempt to clean up the resource concerned, ie log onto docker engine and delete the running container etc,
			// But as these are virtual resources, nothing to do by delete the keys
			
			dss.performActions(owner, byRun);
		} catch (DynamicStatusStoreException e) {
			// If there is a mismatch, something seriously wrong with the contents with the DSS
			throw new CoreManagerException("Problem updating the DSS with a new resource string reservation", e);
		}
	}



}
