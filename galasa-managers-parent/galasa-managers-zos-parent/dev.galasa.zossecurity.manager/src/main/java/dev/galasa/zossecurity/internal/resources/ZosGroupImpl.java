/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.resources;

import dev.galasa.zossecurity.IZosGroup;

public class ZosGroupImpl implements IZosGroup, Comparable<ZosGroupImpl> {
	
	private final String groupid;
	
	public ZosGroupImpl(String groupid) {
		this.groupid = groupid;
	}

	@Override
	public String getGroupid() {
		return groupid;
	}

	@Override
	public int compareTo(ZosGroupImpl o) {
		return groupid.compareTo(o.groupid);
	}

	@Override
	public String toString() {
		return "[zOS Security Group] " + groupid;
	}
	
}
