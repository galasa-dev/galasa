/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;


import dev.galasa.framework.spi.*;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

public class TestResourceNameValidator {

    private ResourceNameValidator validator = new ResourceNameValidator();

    private void assertValidNamespace(String namespaceToCheck) {
        Throwable thrown = catchThrowable(() -> {
            validator.assertNamespaceCharPatternIsValid(namespaceToCheck);
        });

        assertThat(thrown).isNull();
    }

    private void assertNotValidNamespace(String namespaceToCheck) {
        Throwable thrown = catchThrowable(() -> {
            validator.assertNamespaceCharPatternIsValid(namespaceToCheck);
        });

        assertThat(thrown).isNotNull();
    }

    @Test
    public void testNamespaceCanStartWithLetter() {
        assertValidNamespace("a");
        assertValidNamespace("f");
        assertValidNamespace("z");
    }

    @Test
    public void testNamespaceCanNotStartWithUpperCaseLetter() {
        assertNotValidNamespace("S");
        assertNotValidNamespace("A");
        assertNotValidNamespace("H");
    }

    @Test
    public void testNamespaceCannotStartWithANumber() {
        assertInvalidFirstCharacter("5");
    }

    @Test
    public void testNamespaceCannotStartWithAWerdCharacter() {
        assertInvalidFirstCharacter("_");
        assertInvalidFirstCharacter("-");
        assertInvalidFirstCharacter(".");
        assertInvalidFirstCharacter("%");
        assertInvalidFirstCharacter("@");
    }

    @Test
    public void testNamespaceCanContinueWithLowerCaseLetter() {
        assertValidNamespace("aa");
        assertValidNamespace("ad");
        assertValidNamespace("zz");
        assertValidNamespace("ze");
    }

    @Test
    public void testNamespaceCanNotContinueWithUpperCaseLetter() {
        assertNotValidNamespace("aA");
        assertNotValidNamespace("aD");
        assertNotValidNamespace("zJ");
        assertNotValidNamespace("zZ");
    }

    @Test
    public void testNamespaceCanContinueWithADigit() {
        assertValidNamespace("a0");
        assertValidNamespace("a9");
    }

    @Test
    public void testNamespaceCannotContinueWithAnyWeirdCharacter() {
        assertInvalidFollowingCharacter("a%");
        assertInvalidFollowingCharacter("a@");
        assertInvalidFollowingCharacter("a-a");
        assertInvalidFollowingCharacter("a_a");
    }

    private void assertInvalidFirstCharacter(String namespaceToCheck ) {

        Throwable thrown = catchThrowable(() -> {
            validator.assertNamespaceCharPatternIsValid(namespaceToCheck);
        });

        assertThat(thrown)
                .isNotNull()
                .isInstanceOf(FrameworkException.class)
                .hasMessageContaining("Invalid namespace name.", namespaceToCheck,
                                    "must not start with",
                                    "Allowable first characters are")
                ;
    }

    private void assertInvalidFollowingCharacter(String namespaceToCheck ) {

        Throwable thrown = catchThrowable(() -> {
            validator.assertNamespaceCharPatternIsValid(namespaceToCheck);
        });

        assertThat(thrown)
                .isNotNull()
                .isInstanceOf(FrameworkException.class)
                .hasMessageContaining("Invalid namespace name.", namespaceToCheck,
                        "must not contain",
                        "Valid characters after the first character in the name are")
        ;
    }


    @Test
    public void assertPropertyNameIsValid() {
        assertValidPropertyNameIsOk("a.A");
        assertValidPropertyNameIsOk("a.0");
        assertValidPropertyNameIsOk("a.b.c");
        assertValidPropertyNameIsOk("a.-b-c");
        assertValidPropertyNameIsOk("a_b_.c");

    }

    @Test
    public void assertPropertyNameWithInvalidFirstCharCausesError() {
        assertInvalidPropertyNameFirstCharacter("0");
        assertInvalidPropertyNameFirstCharacter(".a");
        assertInvalidPropertyNameFirstCharacter("_.0");
        assertInvalidPropertyNameFirstCharacter("!.B.C");
        assertInvalidPropertyNameFirstCharacter("~_B._C");
        assertInvalidPropertyNameFirstCharacter("-.B-C");
    }

    private void assertInvalidPropertyNameFirstCharacter(String propertyNameToCheck) {
        Throwable thrown = catchThrowable(() -> {
            validator.assertPropertyNameCharPatternIsValid(propertyNameToCheck);
        });
        assertThat(thrown)
                .isNotNull()
                .isInstanceOf(FrameworkException.class)
                .hasMessageContaining("Invalid property name.", propertyNameToCheck,
                        "must not contain",
                        "Valid first characters in the name are")
        ;
    }

    private void assertValidPropertyNameIsOk(String propertyNameToCheck) {
        Throwable thrown = catchThrowable(() -> {
            validator.assertPropertyNameCharPatternIsValid(propertyNameToCheck);
        });
        assertThat(thrown).isNull();
    }
}
