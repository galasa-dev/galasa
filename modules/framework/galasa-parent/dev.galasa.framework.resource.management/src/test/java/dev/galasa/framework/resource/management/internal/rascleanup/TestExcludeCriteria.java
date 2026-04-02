/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal.rascleanup;

import static org.assertj.core.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import dev.galasa.framework.spi.ras.IRasSearchCriteria;
import dev.galasa.framework.spi.ras.RasSearchCriteriaGroup;
import dev.galasa.framework.spi.ras.RasSearchCriteriaTags;
import dev.galasa.framework.spi.ras.RasSearchCriteriaUser;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class TestExcludeCriteria {

    @Test
    public void testGetFromStringReturnsTagsCriteria() {
        // When...
        ExcludeCriteria criteria = ExcludeCriteria.getFromString("tags");

        // Then...
        assertThat(criteria).as("Should return TAGS criteria").isEqualTo(ExcludeCriteria.TAGS);
    }

    @Test
    public void testGetFromStringReturnsUserCriteria() {
        // When...
        ExcludeCriteria criteria = ExcludeCriteria.getFromString("user");

        // Then...
        assertThat(criteria).as("Should return USER criteria").isEqualTo(ExcludeCriteria.USER);
    }

    @Test
    public void testGetFromStringReturnsGroupCriteria() {
        // When...
        ExcludeCriteria criteria = ExcludeCriteria.getFromString("group");

        // Then...
        assertThat(criteria).as("Should return GROUP criteria").isEqualTo(ExcludeCriteria.GROUP);
    }

    @Test
    public void testGetFromStringReturnsRequestorCriteria() {
        // When...
        ExcludeCriteria criteria = ExcludeCriteria.getFromString("requestor");

        // Then...
        assertThat(criteria).as("Should return REQUESTOR criteria").isEqualTo(ExcludeCriteria.REQUESTOR);
    }

    @Test
    public void testGetFromStringIsCaseInsensitive() {
        // When...
        ExcludeCriteria upperCase = ExcludeCriteria.getFromString("TAGS");
        ExcludeCriteria mixedCase = ExcludeCriteria.getFromString("TaGs");
        ExcludeCriteria lowerCase = ExcludeCriteria.getFromString("tags");

        // Then...
        assertThat(upperCase).as("Uppercase should work").isEqualTo(ExcludeCriteria.TAGS);
        assertThat(mixedCase).as("Mixed case should work").isEqualTo(ExcludeCriteria.TAGS);
        assertThat(lowerCase).as("Lowercase should work").isEqualTo(ExcludeCriteria.TAGS);
    }

    @Test
    public void testGetFromStringTrimsWhitespace() {
        // When...
        ExcludeCriteria criteria = ExcludeCriteria.getFromString("  tags  ");

        // Then...
        assertThat(criteria).as("Should trim whitespace").isEqualTo(ExcludeCriteria.TAGS);
    }

    @Test
    public void testGetFromStringReturnsNullForUnknownCriteria() {
        // When...
        ExcludeCriteria criteria = ExcludeCriteria.getFromString("unknown");

        // Then...
        assertThat(criteria).as("Should return null for unknown criteria").isNull();
    }

    @Test
    public void testGetFromStringReturnsNullForEmptyString() {
        // When...
        ExcludeCriteria criteria = ExcludeCriteria.getFromString("");

        // Then...
        assertThat(criteria).as("Should return null for empty string").isNull();
    }

    @Test
    public void testToStringReturnsCorrectName() {
        // When/Then...
        assertThat(ExcludeCriteria.TAGS.toString()).isEqualTo("tags");
        assertThat(ExcludeCriteria.USER.toString()).isEqualTo("user");
        assertThat(ExcludeCriteria.GROUP.toString()).isEqualTo("group");
        assertThat(ExcludeCriteria.REQUESTOR.toString()).isEqualTo("requestor");
        assertThat(ExcludeCriteria.RESULT.toString()).isEqualTo("result");
    }

    @Test
    public void testCreateCriteriaForTagsReturnsRasSearchCriteriaTags() throws Exception {
        // Given...
        String[] tags = {"production", "critical"};

        // When...
        IRasSearchCriteria criteria = ExcludeCriteria.TAGS.createCriteria(tags);

        // Then...
        assertThat(criteria).as("Should return RasSearchCriteriaTags instance").isInstanceOf(RasSearchCriteriaTags.class);
        assertThat(criteria.getCriteriaContent()).as("Should contain the tags").isEqualTo(tags);
    }

    @Test
    public void testCreateCriteriaForUserReturnsRasSearchCriteriaUser() throws Exception {
        // Given...
        String[] users = {"admin", "system"};

        // When...
        IRasSearchCriteria criteria = ExcludeCriteria.USER.createCriteria(users);

        // Then...
        assertThat(criteria).as("Should return RasSearchCriteriaUser instance").isInstanceOf(RasSearchCriteriaUser.class);
        assertThat(criteria.getCriteriaContent()).as("Should contain the users").isEqualTo(users);
    }

    @Test
    public void testCreateCriteriaForGroupReturnsRasSearchCriteriaGroup() throws Exception {
        // Given...
        String[] groups = {"dev.galasa.important.tests"};

        // When...
        IRasSearchCriteria criteria = ExcludeCriteria.GROUP.createCriteria(groups);

        // Then...
        assertThat(criteria).as("Should return RasSearchCriteriaGroup instance").isInstanceOf(RasSearchCriteriaGroup.class);
        assertThat(criteria.getCriteriaContent()).as("Should contain the groups").isEqualTo(groups);
    }

    @Test
    public void testShouldRunBeKeptReturnsTrueWhenTagMatches() throws Exception {
        // Given...
        TestStructure testStructure = new TestStructure();
        Set<String> tags = new HashSet<>();
        tags.add("production");
        testStructure.setTags(tags);

        String[] excludeValues = {"production", "critical"};

        // When...
        boolean shouldKeep = ExcludeCriteria.TAGS.shouldRunBeKept(testStructure, excludeValues);

        // Then...
        assertThat(shouldKeep).as("Should return true when tag matches").isTrue();
    }

    @Test
    public void testShouldRunBeKeptReturnsFalseWhenTagDoesNotMatch() throws Exception {
        // Given...
        TestStructure testStructure = new TestStructure();
        Set<String> tags = new HashSet<>();
        tags.add("test");
        testStructure.setTags(tags);

        String[] excludeValues = {"production", "critical"};

        // When...
        boolean shouldKeep = ExcludeCriteria.TAGS.shouldRunBeKept(testStructure, excludeValues);

        // Then...
        assertThat(shouldKeep).as("Should return false when tag does not match").isFalse();
    }

    @Test
    public void testShouldRunBeKeptReturnsTrueWhenUserMatches() throws Exception {
        // Given...
        TestStructure testStructure = new TestStructure();
        testStructure.setRequestor("admin");

        String[] excludeValues = {"admin", "system"};

        // When...
        boolean shouldKeep = ExcludeCriteria.USER.shouldRunBeKept(testStructure, excludeValues);

        // Then...
        assertThat(shouldKeep).as("Should return true when user matches").isTrue();
    }

    @Test
    public void testShouldRunBeKeptReturnsFalseWhenUserDoesNotMatch() throws Exception {
        // Given...
        TestStructure testStructure = new TestStructure();
        testStructure.setRequestor("developer");

        String[] excludeValues = {"admin", "system"};

        // When...
        boolean shouldKeep = ExcludeCriteria.USER.shouldRunBeKept(testStructure, excludeValues);

        // Then...
        assertThat(shouldKeep).as("Should return false when user does not match").isFalse();
    }

    @Test
    public void testShouldRunBeKeptReturnsTrueWhenGroupMatches() throws Exception {
        // Given...
        TestStructure testStructure = new TestStructure();
        testStructure.setGroup("dev.galasa.important.tests");

        String[] excludeValues = {"dev.galasa.important.tests"};

        // When...
        boolean shouldKeep = ExcludeCriteria.GROUP.shouldRunBeKept(testStructure, excludeValues);

        // Then...
        assertThat(shouldKeep).as("Should return true when group matches").isTrue();
    }

    @Test
    public void testShouldRunBeKeptReturnsFalseWhenGroupDoesNotMatch() throws Exception {
        // Given...
        TestStructure testStructure = new TestStructure();
        testStructure.setGroup("dev.galasa.other.tests");

        String[] excludeValues = {"dev.galasa.important.tests"};

        // When...
        boolean shouldKeep = ExcludeCriteria.GROUP.shouldRunBeKept(testStructure, excludeValues);

        // Then...
        assertThat(shouldKeep).as("Should return false when group does not match").isFalse();
    }

    @Test
    public void testShouldRunBeKeptReturnsFalseWhenExcludeValuesIsNull() throws Exception {
        // Given...
        TestStructure testStructure = new TestStructure();
        Set<String> tags = new HashSet<>();
        tags.add("production");
        testStructure.setTags(tags);

        // When...
        boolean shouldKeep = ExcludeCriteria.TAGS.shouldRunBeKept(testStructure, null);

        // Then...
        assertThat(shouldKeep).as("Should return false when exclude values is null").isFalse();
    }

    @Test
    public void testShouldRunBeKeptReturnsFalseWhenExcludeValuesIsEmpty() throws Exception {
        // Given...
        TestStructure testStructure = new TestStructure();
        Set<String> tags = new HashSet<>();
        tags.add("production");
        testStructure.setTags(tags);

        String[] excludeValues = {};

        // When...
        boolean shouldKeep = ExcludeCriteria.TAGS.shouldRunBeKept(testStructure, excludeValues);

        // Then...
        assertThat(shouldKeep).as("Should return false when exclude values is empty").isFalse();
    }

    @Test
    public void testAllEnumValuesAreAccessible() {
        // When...
        ExcludeCriteria[] values = ExcludeCriteria.values();

        // Then...
        assertThat(values).as("Should have 3 enum values").hasSize(5);
        assertThat(values).as("Should contain TAGS").contains(ExcludeCriteria.TAGS);
        assertThat(values).as("Should contain USER").contains(ExcludeCriteria.USER);
        assertThat(values).as("Should contain GROUP").contains(ExcludeCriteria.GROUP);
        assertThat(values).as("Should contain REQUESTOR").contains(ExcludeCriteria.REQUESTOR);
        assertThat(values).as("Should contain RESULT").contains(ExcludeCriteria.RESULT);
    }

    @Test
    public void testValueOfReturnsCorrectEnum() {
        // When/Then...
        assertThat(ExcludeCriteria.valueOf("TAGS")).as("valueOf TAGS").isEqualTo(ExcludeCriteria.TAGS);
        assertThat(ExcludeCriteria.valueOf("USER")).as("valueOf USER").isEqualTo(ExcludeCriteria.USER);
        assertThat(ExcludeCriteria.valueOf("GROUP")).as("valueOf GROUP").isEqualTo(ExcludeCriteria.GROUP);
        assertThat(ExcludeCriteria.valueOf("REQUESTOR")).as("valueOf REQUESTOR").isEqualTo(ExcludeCriteria.REQUESTOR);
        assertThat(ExcludeCriteria.valueOf("RESULT")).as("valueOf RESULT").isEqualTo(ExcludeCriteria.RESULT);
    }

    @Test
    public void testValueOfThrowsExceptionForInvalidName() {
        // When/Then...
        assertThatThrownBy(() -> ExcludeCriteria.valueOf("INVALID"))
            .as("Should throw IllegalArgumentException for invalid name")
            .isInstanceOf(IllegalArgumentException.class);
    }
}
