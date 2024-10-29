/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language;

import dev.galasa.framework.spi.language.gherkin.GherkinTest;

public class GalasaTest {

    private GalasaLanguage language;
    
    private Class<?> javaTestClass;
    private GherkinTest gherkinTest;

    public GalasaTest(Class<?> klass) {
        this.javaTestClass = klass;
        this.language = GalasaLanguage.java;
    }

    public GalasaTest(GherkinTest test) {
        this.gherkinTest = test;
        this.language = GalasaLanguage.gherkin;
    }

    public GalasaLanguage getLanguage() {
        return this.language;
    }

    public Boolean isJava() {
        return this.language == GalasaLanguage.java;
    }

    public Boolean isGherkin() {
        return this.language == GalasaLanguage.gherkin;
    }

    public Class<?> getJavaTestClass() {
        return this.javaTestClass;
    }

    public GherkinTest getGherkinTest() {
        return this.gherkinTest;
    }
    
}