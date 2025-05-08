/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ras.couchdb.internal;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.ras.IRasSearchCriteria;
import dev.galasa.framework.spi.ras.RasSearchCriteriaQueuedFrom;
import dev.galasa.framework.spi.ras.RasSearchCriteriaQueuedTo;

public class CouchdbRasQueryBuilder {

    public JsonObject buildGetRunsQuery(IRasSearchCriteria... searchCriterias) throws ResultArchiveStoreException {
        JsonObject selector = new JsonObject();
        JsonArray and = new JsonArray();
        selector.add("$and", and);

        for (IRasSearchCriteria searchCriteria : searchCriterias) {
            if (searchCriteria instanceof RasSearchCriteriaQueuedFrom) {
                RasSearchCriteriaQueuedFrom sFrom = (RasSearchCriteriaQueuedFrom) searchCriteria;

                JsonObject criteria = new JsonObject();
                JsonObject jFrom = new JsonObject();
                jFrom.addProperty("$gte", sFrom.getFrom().toString());
                criteria.add("queued", jFrom);
                and.add(criteria);
            } else if (searchCriteria instanceof RasSearchCriteriaQueuedTo) {
                RasSearchCriteriaQueuedTo sTo = (RasSearchCriteriaQueuedTo) searchCriteria;

                JsonObject criteria = new JsonObject();
                JsonObject jTo = new JsonObject();
                jTo.addProperty("$lt", sTo.getTo().toString());
                criteria.add("queued", jTo);
                and.add(criteria);
            } else {
                addInArrayConditionToQuery(and, searchCriteria.getCriteriaName(), searchCriteria.getCriteriaContent());
            }
        }
        return selector;
    }

    private void addInArrayConditionToQuery(JsonArray existingQuery, String field, String[] inArray) {
        if (inArray != null && inArray.length > 0) {
            JsonArray inArrayJson = new JsonArray();
            for (String in : inArray) {
                if (in != null && !in.isEmpty()) {
                    inArrayJson.add(in);
                }
            }

            if (!inArrayJson.isEmpty()) {
                JsonObject inCondition = new JsonObject();
                inCondition.add("$in", inArrayJson);
        
                JsonObject criteria = new JsonObject();
                criteria.add(field, inCondition);
        
                existingQuery.add(criteria);
            }
        }
    }
}
