/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;

public class RasSortField {

    private String fieldName;
    private String sortDirection;

    public RasSortField(String fieldName, String sortOrder) {
        this.fieldName = fieldName;
        this.sortDirection = sortOrder;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortOrder) {
        this.sortDirection = sortOrder;
    }
}
