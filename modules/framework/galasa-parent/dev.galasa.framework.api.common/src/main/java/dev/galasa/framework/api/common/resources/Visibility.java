 /*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;
 
 public enum Visibility{
        NORMAL("normal"),
        SECURE("secure"),
        HIDDEN("hidden")
        ;
        private String value;

        private Visibility(String type){
            this.value = type;
        }

        public static Visibility getfromString(String typeAsString){
            Visibility match = null;
            for (Visibility type : Visibility.values()){
                if (type.toString().equalsIgnoreCase(typeAsString)){
                    match = type;
                }
            }
            return match;
        }

        @Override
        public String toString(){
            return value;
        }

    }