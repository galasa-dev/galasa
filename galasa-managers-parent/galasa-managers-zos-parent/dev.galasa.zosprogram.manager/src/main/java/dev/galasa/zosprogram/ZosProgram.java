/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosprogram;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;
import dev.galasa.zosprogram.internal.ZosProgramManagerField;

/**
 * z/OS Program
 * 
 * @galasa.annotation
 * 
 * @galasa.description The <code>{@literal @}ZosProgram</code> annotation requests the z/OS Program Manager to Compile and Bind a
 * program on a z/OS image. 
 * The test can request multiple z/OS Program instances
 * 
 * @galasa.examples 
 * <code>
 * {@literal @}ZosImage(imageTag="A")<br>
 * public IZosImage zosImageA;<br>
 * {@literal @}ZosProgram(imageTag="A")<br>
 * public IZosProgram zosProgramA;<br>
 * </code>
 * 
 * @galasa.extra
 * The <code>IZosProgram</code> interface has a number of methods to manage the zOS Program.
 * 
 * @see ZosProgram
 * @see IZosProgram
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@ZosProgramManagerField
@ValidAnnotatedFields({ IZosProgram.class })
public @interface ZosProgram {
    
    /**
     * The program name
     */
    String name();

    /**
     * Path to the location of the program source in the Galasa test bundle. This can be either the full path including the file name
     * or the directory containing the source with the name specified in the name attribute with the extension specified in the language attribute. 
     */
    String location() default "resources";
    
    /**
     * The programming language. See <a href="https://javadoc-snapshot.galasa.dev/dev/galasa/zosprogram/ZosProgram.Language.html" target="_blank" rel="noopener noreferrer">ZosProgram.Language</a>. <br><br> 
     */
    Language language();
    
    /**
     * Is a CICS program and requires the CICS translator.
     */
    boolean cics() default false;

    /**
     * The load module data set name
     */
    String loadlib() default "";
    
    /**
     * The <code>imageTag</code> is used to identify the z/OS image.
     */
    String imageTag() default "primary";
    
    /**
     * Compile this zOS program.
     */
    boolean compile() default true;

    /**
     * Enumeration of supported languages
     */
    public enum Language {
        /**
         * COBOL program with ".cbl" file extension 
         */
        COBOL(".cbl"),
        /**
         * C program with ".c" file extension 
         */
        C(".c"),
        /**
         * PL1 program with ".pl1" file extension 
         */
        PL1(".pl1"),
        /**
         * Assembler program with ".asm" file extension 
         */
        ASSEMBLER(".asm"),
        /**
         * Do Not Use 
         */
        INVALID("");
        
        private final String extension;
        
        private Language(String extension) {
            this.extension = extension;
        }
        
        public String getFileExtension(){
            return extension;
        }
        
        public static Language fromExtension(String extension) {
            
            for (Language l : values()) {
                if (l.extension.equalsIgnoreCase(extension)) {
                    return l;
                }
            }
            
            throw new IllegalArgumentException("Extension " + extension + " does not match supported languages");
        }
    }
}
