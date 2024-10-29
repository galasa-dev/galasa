/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.resources;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;

import dev.galasa.zossecurity.KeyringAlreadyExistsException;
import dev.galasa.zossecurity.ProfileAlreadyExistsException;
import dev.galasa.zossecurity.ProfileConfigurationException;
import dev.galasa.zossecurity.ProfileNotFoundException;
import dev.galasa.zossecurity.RacfSyntaxErrorException;
import dev.galasa.zossecurity.UseridNotFoundException;
import dev.galasa.zossecurity.ZosSecurityManagerException;
import dev.galasa.zossecurity.internal.RACFMessages;

public class RacfOutputProcessing {
	
	private static final Log logger = LogFactory.getLog(RacfOutputProcessing.class);
	
	public enum COMMAND {
		ADDUSER,
		ALTUSER,
		LISTUSER,
		CONNECT,
		REMOVE,
		RDEFINE, 
		RLIST, 
		RALTER, 
		PERMIT, 
		RDELETE,
		DELUSER,
		REFRESH, 
		RACDCERT_ADDRING, 
		RACDCERT_LISTRING, 
		RACDCERT_DELRING, 
		RACDCERT_LIST, 
		RACDCERT_DELETE, 
		RACDCERT_ADD, 
		RACDCERT_CONNECT, 
		RACDCERT_REMOVE, 
		RACMAP_MAP, 
		RACMAP_DELMAP, 
		RACMAP_LISTMAP
	}

	
	public static JsonObject analyseOutput(JsonObject jsonResponse, COMMAND command, String resourceName, boolean outputReporting) throws ZosSecurityManagerException {
		
		reportFailure(jsonResponse);
		if (jsonResponse == null || jsonResponse.get("output") == null || jsonResponse.get("output").getAsString() == null) {
			throw new ZosSecurityManagerException("No output string was returned for " + command + " of " + resourceName);
		}

		String stringOutput = "";
		String stringCommand = "";
		if (jsonResponse.get("output") != null || jsonResponse.get("output").getAsString() != null) {
			stringOutput = jsonResponse.get("output").getAsString().trim();
		}
		if (jsonResponse.get("output") != null || jsonResponse.get("output").getAsString() != null) {
			stringCommand = jsonResponse.get("output").getAsString().trim();
		}

		if (stringOutput.isEmpty()) {
			logger.debug("No output was returned from RACF for the " + command + " of " + resourceName + "\n" + stringCommand);
			return jsonResponse;
		}

		if (outputReporting) {
			logger.debug("RACF output from the " + command + " command\n" + stringCommand + "\n" + stringOutput);
		}
		
		if (stringOutput.contains("RACF authorization error")) {
			throw new ZosSecurityManagerException("RACF output contains 'RACF authorization error' indicating the userid does not have the correct authorities");
		}

		Set<String> messages = RACFMessages.getMessages(stringOutput);
		if (messages.isEmpty()) {
			return jsonResponse;
		}
		filterUserOkMessages(messages);
		checkForInvalidMessages(messages, stringOutput, stringCommand, resourceName);

		if (!messages.isEmpty()) {
			String invalidMessages = RACFMessages.getInvalidMessages(messages);
			throw new ZosSecurityManagerException("Unknown messages detected in " + command + " of " + resourceName + " - " + invalidMessages + "\n" + stringCommand + "\n" + stringOutput);
		}

		return jsonResponse;
	}



	public static void reportFailure(JsonObject jsonResponse) throws ZosSecurityManagerException {
		if (jsonResponse.get("failed") == null || !jsonResponse.get("failed").getAsBoolean()) {
			return;
		}

		String message = "";
		String stacktrace = "";
		if (jsonResponse.get("message") != null || jsonResponse.get("message").getAsString() != null) {
			message = jsonResponse.get("message").getAsString();
		}
		if (jsonResponse.get("stacktrace") != null || jsonResponse.get("stacktrace").getAsString() != null) {
			message = jsonResponse.get("stacktrace").getAsString();
		}

		throw new ZosSecurityManagerException("Call to the zossec server failed with '" + message + "'\n"+stacktrace);
	}

	public static void checkForInvalidMessages(Set<String> messages, String output, String command, String resourceName) throws ZosSecurityManagerException {
		if (messages.contains("ICH30001I")) {
			throw new UseridNotFoundException("User " + resourceName + " was not found\n" + command + "\n" + output);
		}
		if (messages.contains("ICH10102I")) {
			throw new ProfileAlreadyExistsException("Profile " + resourceName + " already exists\n" + command + "\n" + output);
		}
		if (messages.contains("ICH13003I") || messages.contains("ICH12102I")) {
			throw new ProfileNotFoundException("Profile " + resourceName + " was not found\n" + command + "\n" + output);
		}
		if (messages.contains("ICH11004I")) {
			throw new ProfileConfigurationException("Profile " + resourceName + " configuration error\n" + command + "\n" + output);
		}
		if (messages.contains("IKJ56701I") || messages.contains("IKJ56702I") ) {
			throw new RacfSyntaxErrorException("Invalid syntax detected on the RACF command\n" + command + "\n" + output);
		}
		if (messages.contains("IRRD122I")) {
			throw new KeyringAlreadyExistsException("Keyring " + resourceName + " already exists\n" + command + "\n" + output);
		}
		if (messages.contains("ICH06006I")) {
			throw new ProfileConfigurationException("Not authorised to resource " + resourceName + " which can be caused by RACLISTed classes, you may need to refresh the class before attempting permits\n" + command + "\n" + output);
		}
	}

	public static void filterUserOkMessages(Set<String> messages) {
		messages.remove("IRRD113I");  // The certificate that you are adding is self-signed
		messages.remove("IRRD199I");  // Certificate with label 'xxxxxx' is added for user xxxxxx.
		messages.remove("IRR52021I"); // Not authorised to view field
		messages.remove("ICH14063I"); // SETROPTS command complete
		messages.remove("ICH14016I"); // CANNOT REFRESH xxxxxxx, GLOBAL ACCESS CHECKING INACTIVE.
		messages.remove("ICH14070I"); // SETROPTS RACLIST REFRESH had no effect on class xxxxxxxx
		messages.remove("IRRW210I");  // RACLISTed profiles for the xxxxxx class will not reflect changes unti
		messages.remove("IRRD105I");  // No certificate information was found for user
		messages.remove("IRRD115I");  // User xxxx has no rings
		messages.remove("IRRW204I");  // No information was found for user xxxxxx.
		messages.remove("IRRW206I");  // No matching identity mapping was found for this user.
		messages.remove("IRRD107I");  // No matching certificate was found for this user.
		messages.remove("IRRD114I");  // Ring xxxxxx does not exist.
		messages.remove("ICH13004I"); // NOTHING TO LIST
		messages.remove("ICH12002I"); // RACLISTED PROFILES FOR xxxxxx WILL NOT REFLECT THE DELETION(S) UNTIL
		messages.remove("ICH10006I"); // RACLISTED PROFILES FOR xxxxxx WILL NOT REFLECT THE ADDITION(S) UNTIL	
		messages.remove("ICH06011I"); // RACLISTED PROFILES FOR xxxxxx WILL NOT REFLECT THE UPDATE(S) UNTIL
		messages.remove("ICH11009I"); // RACLISTED PROFILES FOR xxxxxx WILL NOT REFLECT THE UPDATE(S) 
	}

}
