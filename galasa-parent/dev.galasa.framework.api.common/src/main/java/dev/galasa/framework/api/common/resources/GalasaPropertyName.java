/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

public class GalasaPropertyName {
    String namespaceName;
    String simpleName;

    public GalasaPropertyName( String fullyQualifiedName ) {
        String[] parts = fullyQualifiedName.split("[.]",2);
        this.namespaceName = parts[0];
        this.simpleName = parts[1];
    }

    public GalasaPropertyName( String namespaceName , String simpleName ) {
        this.namespaceName = namespaceName;
        this.simpleName = simpleName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof GalasaPropertyName)) {
            return false;
        }

        GalasaPropertyName other = (GalasaPropertyName)o;


        boolean isNamespaceSame = isNamespaceFieldsMatching(other);
        boolean isSimpleNameSame = isSimpleNameFieldMatching(other);
        
        return (isSimpleNameSame && isNamespaceSame);
    }

    private boolean isSimpleNameFieldMatching(GalasaPropertyName other) {
        boolean isSimpleNameSame = false ;
        if (this.simpleName == null ) {
            if (other.simpleName == null ) {
                isSimpleNameSame = true;
            }
        } else {
            if (other.simpleName != null ) {
                if (this.simpleName.equals(other.simpleName)) {
                    isSimpleNameSame = true ;
                }
            }
        }

        return isSimpleNameSame ;
    }

    private boolean isNamespaceFieldsMatching(GalasaPropertyName other) {
        boolean isNamespaceSame = false ;
        if (this.namespaceName==null) {
            if (other.namespaceName==null) {
                // They are the same. both null.
                isNamespaceSame = true;
            } 
        } else {
            if (other.namespaceName!=null) {
                if (this.namespaceName.equals(other.namespaceName)) {
                    isNamespaceSame = true;
                }
            }
        }
        return isNamespaceSame;
    }

    @Override
    public final int hashCode() {
        int result = 17;
        if (this.simpleName != null) {
            result = 31 * result + this.simpleName.hashCode();
        }
        if (this.namespaceName != null) {
            result = 31 * result + this.namespaceName.hashCode();
        }
        return result;
    }

    public String getFullyQualifiedName() {
        return this.namespaceName+"."+this.simpleName;
    }

    public String getShortestSuffix() {
        String name = this.simpleName;
        return name.substring(name.lastIndexOf(".") + 1);
    }

    public String getLongestPrefix() {
        String name = this.simpleName;
        return name.substring(0, name.lastIndexOf("."));
    }
    
    public String getSimpleName() {
        return this.simpleName;
    }
    public String getNamespaceName() {
        return this.namespaceName;
    }
}