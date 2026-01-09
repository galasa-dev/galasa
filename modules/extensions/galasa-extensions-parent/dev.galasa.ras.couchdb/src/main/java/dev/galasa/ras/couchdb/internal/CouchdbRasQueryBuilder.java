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
import dev.galasa.framework.spi.ras.RasSearchCriteriaRequestor;
import dev.galasa.framework.spi.ras.RasSearchCriteriaTags;
import dev.galasa.framework.spi.ras.RasSearchCriteriaUser;

public class CouchdbRasQueryBuilder {

    public JsonObject buildGetRunsQuery(IRasSearchCriteria... searchCriterias) throws ResultArchiveStoreException {
        JsonObject selector = new JsonObject();
        JsonArray and = new JsonArray();
        selector.add("$and", and);

        for (IRasSearchCriteria searchCriteria : searchCriterias) {
            if (searchCriteria instanceof RasSearchCriteriaQueuedFrom) {
                addQueuedFromToQuery(and, searchCriteria);
            } else if (searchCriteria instanceof RasSearchCriteriaQueuedTo) {
                addQueuedToToQuery(and, searchCriteria);
            } else if (searchCriteria instanceof RasSearchCriteriaTags) {
                // Tags are stored in a list in CouchDB documents, so we need to use an "in" operator for the query
                addInArrayConditionToQuery(and, searchCriteria.getCriteriaName(), searchCriteria.getCriteriaContent());
            } else if (!(searchCriteria instanceof RasSearchCriteriaUser) && !(searchCriteria instanceof RasSearchCriteriaRequestor)) {
                addConditionToQuery(and, searchCriteria.getCriteriaName(), searchCriteria.getCriteriaContent());
            }
        }

        // Add requestor/user criteria to the query builder separately as the behaviour
        // depends on whether requestor, user, or both are provided as query parameters.
        applyRequestorUserCriteria(and, searchCriterias);

        return selector;
    }

    private void addQueuedFromToQuery(JsonArray and, IRasSearchCriteria searchCriteria) {
        RasSearchCriteriaQueuedFrom sFrom = (RasSearchCriteriaQueuedFrom) searchCriteria;

        JsonObject criteria = new JsonObject();
        JsonObject jFrom = new JsonObject();
        jFrom.addProperty("$gte", sFrom.getFrom().toString());
        criteria.add("queued", jFrom);
        and.add(criteria);
    }

    private void addQueuedToToQuery(JsonArray and, IRasSearchCriteria searchCriteria) {
        RasSearchCriteriaQueuedTo sTo = (RasSearchCriteriaQueuedTo) searchCriteria;

        JsonObject criteria = new JsonObject();
        JsonObject jTo = new JsonObject();
        jTo.addProperty("$lt", sTo.getTo().toString());
        criteria.add("queued", jTo);
        and.add(criteria);
    }

    private void applyRequestorUserCriteria(JsonArray existingQuery, IRasSearchCriteria... searchCriterias) {
        RasSearchCriteriaUser userCriteria = null;
        RasSearchCriteriaRequestor requestorCriteria = null;

        for (IRasSearchCriteria criteria : searchCriterias) {
            if (criteria instanceof RasSearchCriteriaUser) {
                userCriteria = (RasSearchCriteriaUser) criteria;
            } else if (criteria instanceof RasSearchCriteriaRequestor) {
                requestorCriteria = (RasSearchCriteriaRequestor) criteria;
            }
        }

        if (userCriteria != null && requestorCriteria != null) {
            // Both provided - strict match on both.
            addConditionToQuery(existingQuery, "user", userCriteria.getCriteriaContent());
            addConditionToQuery(existingQuery, "requestor", requestorCriteria.getCriteriaContent());
        } else if (userCriteria != null) {
            // Only user provided - match on either, so create an $or part of the query.
            JsonArray orArray = new JsonArray();
            JsonObject orObject = new JsonObject();
            orObject.add("$or", orArray);
            existingQuery.add(orObject);
            addConditionToQuery(orArray, "user", userCriteria.getCriteriaContent());
            addConditionToQuery(orArray, "requestor", userCriteria.getCriteriaContent());
        } else if (requestorCriteria != null) {
            // Only requestor provided - strict match.
            addConditionToQuery(existingQuery, "requestor", requestorCriteria.getCriteriaContent());
        }
    }

    private void addConditionToQuery(JsonArray existingQuery, String fieldName, String[] fieldContent) {
        if (fieldContent != null) {
            if (fieldContent.length == 1) {
                // Only one value provided, so add a condition in the following format so that CouchDB checks equality:
                // { "fieldName" : "content" }
                JsonObject equalsCondition = new JsonObject();
                equalsCondition.addProperty(fieldName, fieldContent[0]);
                existingQuery.add(equalsCondition);
            } else {
                addInArrayConditionToQuery(existingQuery, fieldName, fieldContent);
            }
        }
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
