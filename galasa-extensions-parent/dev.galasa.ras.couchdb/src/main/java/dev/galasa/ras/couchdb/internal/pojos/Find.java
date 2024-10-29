/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ras.couchdb.internal.pojos;

import com.google.gson.JsonArray;

public class Find {

    public Object    selector;
    public JsonArray sort;
    public Integer   limit;
    public Integer   skip;
    public Boolean   execution_stats;
    public String    bookmark;

}
