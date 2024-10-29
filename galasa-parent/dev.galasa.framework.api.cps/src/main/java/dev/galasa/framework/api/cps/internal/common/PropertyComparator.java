/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.common;

import java.io.Serializable;
import java.util.*;

import dev.galasa.framework.api.common.resources.GalasaPropertyName;

public class PropertyComparator implements Comparator<GalasaPropertyName>, Serializable {

    private static final long serialVersionUID = 1L;

    public PropertyComparator() {
    }

    @Override
    public int compare(GalasaPropertyName propA, GalasaPropertyName propB) {
        return compare(propA.getFullyQualifiedName(), propB.getFullyQualifiedName());
    }

    public int compare(String a, String b) {

        int result ;

        // Need to be able to cope with nulls in either/both
        if (a==null) {
            if (b==null) {
                // Both null
                result = 0;
            } else {
                // a less important than b
                result = 1;
            }
        } else {
            if (b==null) {
                // a not null, b is null, a is more important
                result = -1;
            } else {
                // neither a nor b is null. Look closer.
                result = compareTwoNonNullValues(a,b);
            }
        }
        return result;
    }

    // Neither a nor b are null.
    private int compareTwoNonNullValues(String a, String b) {
        int result ;

        String[] aParts = a.split("[.]");
        String[] bParts = b.split("[.]");

        // If a is more important, then result is more negative.
        result = bParts.length - aParts.length ;
        if (result == 0) {
            // The two strings have the same number of parts.

            // Walk through each part array comparing each item separately.
            for (int i=0;i<aParts.length;i+=1) {
                
                // Alphabetical compare two parts
                result = aParts[i].compareTo(bParts[i]);
                if (result != 0) {
                    // The strings don't match... so we know which one comes first.
                    break;
                }
            }
        }
        return result ;
    }


}