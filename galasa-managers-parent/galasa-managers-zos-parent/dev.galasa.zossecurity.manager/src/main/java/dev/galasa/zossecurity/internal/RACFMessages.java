/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RACFMessages {
	
	private static final Pattern messageSearch = Pattern.compile("((ICH|IRR|IKJ)(D|W|\\d)\\d\\d\\d\\d?[IWE])");

	public static Set<String> getMessages(String output) {
		HashSet<String> messages = new HashSet<String>();
		
		Matcher matcher = messageSearch.matcher(output);
		
		while (matcher.find()) {
			String msgid = matcher.group(0);
			if (!messages.contains(msgid)) {
				messages.add(msgid);
			}
		}
		
		return messages;
	}

	public static String getInvalidMessages(Set<String> messages) {
		StringBuilder sb = new StringBuilder();
		
		for(String message : messages) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(message);
		}
		
		return sb.toString();
	}

}
