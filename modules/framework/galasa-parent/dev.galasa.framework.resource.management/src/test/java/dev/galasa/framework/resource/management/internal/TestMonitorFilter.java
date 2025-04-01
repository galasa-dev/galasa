/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.Test;

import dev.galasa.framework.spi.FrameworkException;

public class TestMonitorFilter {

    @Test
    public void testIsClassAllowedWithMatchingIncludesReturnsTrue() throws Exception {
        // Given...
        List<String> includes = List.of("dev.galasa.*");
        List<String> excludes = List.of();

        ClassNameFilter filter = new ClassNameFilter(includes, excludes);

        String className = "dev.galasa.MyClass";

        // When...
        boolean isAllowed = filter.isClassAcceptedByFilter(className);

        // Then...
        assertThat(isAllowed).isTrue();
    }

    @Test
    public void testIsClassAllowedWithMatchingExcludesReturnsFalse() throws Exception {
        // Given...
        List<String> includes = List.of("*");
        List<String> excludes = List.of("dev.galasa*");

        ClassNameFilter filter = new ClassNameFilter(includes, excludes);

        String className = "dev.galasa.MyClass";

        // When...
        boolean isAllowed = filter.isClassAcceptedByFilter(className);

        // Then...
        assertThat(isAllowed).isFalse();
    }

    @Test
    public void testIsClassAllowedWithExactMatchingIncludesReturnsTrue() throws Exception {
        // Given...
        List<String> includes = List.of("dev.galasa.MyClass");
        List<String> excludes = List.of();

        ClassNameFilter filter = new ClassNameFilter(includes, excludes);

        String className = "dev.galasa.MyClass";

        // When...
        boolean isAllowed = filter.isClassAcceptedByFilter(className);

        // Then...
        assertThat(isAllowed).isTrue();
    }

    @Test
    public void testIsClassAllowedWithMultipleIncludesAndExcludesReturnsTrue() throws Exception {
        // Given...
        List<String> includes = List.of("my.company*", "*myOtherClass", "*MyClass");
        List<String> excludes = List.of("my.company.exclude*", "my.other.excludes");

        ClassNameFilter filter = new ClassNameFilter(includes, excludes);

        String className = "dev.galasa.MyClass";

        // When...
        boolean isAllowed = filter.isClassAcceptedByFilter(className);

        // Then...
        assertThat(isAllowed).isTrue();
    }

    @Test
    public void testIsClassAllowedWithMultipleIncludesAndMatchingExcludesReturnsFalse() throws Exception {
        // Given...
        List<String> includes = List.of("my.company*", "*myOtherClass", "*MyClass");
        List<String> excludes = List.of("*exclude*", "my.other.excludes");

        ClassNameFilter filter = new ClassNameFilter(includes, excludes);

        String className = "my.company.exclude.this.class";

        // When...
        boolean isAllowed = filter.isClassAcceptedByFilter(className);

        // Then...
        assertThat(isAllowed).isFalse();
    }

    @Test
    public void testIsClassAllowedWithQuestionMarkIncludeMatchesOneCharacterOK() throws Exception {
        // Given...
        List<String> includes = List.of("company.?.include*", "*myOtherClass", "*MyClass");
        List<String> excludes = List.of("*exclude*", "my.other.excludes");

        ClassNameFilter filter = new ClassNameFilter(includes, excludes);

        String className = "company.a.include.me";

        // When...
        boolean isAllowed = filter.isClassAcceptedByFilter(className);

        // Then...
        assertThat(isAllowed).isTrue();
    }

    @Test
    public void testIsClassAllowedWithInvalidIncludePatternThrowsCorrectError() throws Exception {
        // Given...
        List<String> includes = List.of("[% not A valid ^^ $ pattern!");
        List<String> excludes = List.of("*exclude*", "my.other.excludes");

        // When...
        FrameworkException thrown = catchThrowableOfType(() -> {
            new ClassNameFilter(includes, excludes);
        }, FrameworkException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Unsupported glob pattern character provided");
    }

    @Test
    public void testIsClassAllowedWithInvalidExcludePatternThrowsCorrectError() throws Exception {
        // Given...
        List<String> includes = List.of("*");
        List<String> excludes = List.of("*exclude*", "[% not A valid ^^ $ pattern!");

        // When...
        FrameworkException thrown = catchThrowableOfType(() -> {
            new ClassNameFilter(includes, excludes);
        }, FrameworkException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Unsupported glob pattern character provided");
    }
}
