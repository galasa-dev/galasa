/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.datatypes;

/**
 * This enum represents the different types of RACF classes that CICS uses for security
 * 
 *  
 *
 */
public enum ZosCicsClassResource {
	Transaction           ("TRN", "T", "G", "XTRAN", 0),
	PSB                   ("PSB", "P", "Q", "XPSB", 0),
	PCT                   ("PCT", "A", "B", "XPCT", 0),
	TransientDataQueues   ("DCT", "D", "E", "XDCT", 0),
	Files                 ("FCT", "F", "H", "XFCT", 0),
	Journals              ("JCT", "J", "K", "XJCT", 0),
	Programs              ("PPT", "M", "N", "XPPT", 0),
	TemporaryStorageQueues("TST", "S", "U", "XTST", 0),
	Commands              ("CMD", "C", "V", "XCMD", 0),
	Resources             ("RES", "R", "W", "XRES", 660);


	private final String suffix;
	private final String memberPrefix;
	private final String groupingPrefix;
	private final String sit;
	private final int    minCicsLevel;

	private ZosCicsClassResource(String suffix, String memberPrefix, String groupingPrefix, String sit, int minCicsLevel) {
		this.suffix         = suffix;
		this.memberPrefix   = memberPrefix;
		this.groupingPrefix = groupingPrefix;
		this.sit            = sit;
		this.minCicsLevel   = minCicsLevel;
	}

	public String getSuffix() {
		return suffix;
	}

	public String getMemberPrefix() {
		return memberPrefix;
	}

	public String getGroupingPrefix() {
		return groupingPrefix;
	}
	
	public String getSIT() {
		return sit;
	}
	
	public int getMinCicsLevel() {
		return this.minCicsLevel;
	}

	public String getClassName(String setName, ZosCicsClassType type) {
		switch (type) {
		case MEMBER:
			return memberPrefix + setName + suffix;
		case GROUPING:
			return groupingPrefix + setName + suffix; 
		default:
			return null;
		}

	}	
}
