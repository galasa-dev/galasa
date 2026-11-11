/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class TestZosBatchJobnames {

    // ---- exact: valid names

    @Test
    public void testExactReturnsNameUpperCased() throws Exception {
        IZosBatchJobname jobname = ZosBatchJobnames.exact("payroll");
        Assert.assertEquals("PAYROLL", jobname.getName());
        Assert.assertEquals("toString() should return the name", "PAYROLL", jobname.toString());
    }

    @Test
    public void testExactAcceptsOneCharacter() throws Exception {
        Assert.assertEquals("A", ZosBatchJobnames.exact("a").getName());
    }

    @Test
    public void testExactAcceptsEightCharacters() throws Exception {
        Assert.assertEquals("ABCDEFGH", ZosBatchJobnames.exact("ABCDEFGH").getName());
    }

    @Test
    public void testExactAcceptsNationalCharactersAndDigits() throws Exception {
        Assert.assertEquals("#A$B@123", ZosBatchJobnames.exact("#a$b@123").getName());
        Assert.assertEquals("$1", ZosBatchJobnames.exact("$1").getName());
        Assert.assertEquals("@1", ZosBatchJobnames.exact("@1").getName());
    }

    @Test
    public void testExactIsNotAffectedByDefaultLocale() throws Exception {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            Assert.assertEquals("ILLEGAL", ZosBatchJobnames.exact("illegal").getName());
        } finally {
            Locale.setDefault(original);
        }
    }

    // ---- exact: invalid names

    @Test
    public void testExactRejectsNull() {
        assertExactRejected(null);
    }

    @Test
    public void testExactRejectsEmpty() {
        assertExactRejected("");
    }

    @Test
    public void testExactRejectsTooLong() {
        assertExactRejected("ABCDEFGHI");
    }

    @Test
    public void testExactRejectsLeadingDigit() {
        assertExactRejected("1ABC");
    }

    @Test
    public void testExactRejectsInvalidCharacters() {
        assertExactRejected("MY-JOB");
        assertExactRejected("MY.JOB");
        assertExactRejected("MY JOB");
        assertExactRejected("MY_JOB");
        assertExactRejected("MY*");
    }

    @Test
    public void testExactRejectsSurroundingWhitespace() {
        assertExactRejected(" MYJOB");
        assertExactRejected("MYJOB ");
        assertExactRejected(" ");
    }

    @Test
    public void testExactRejectsNonAsciiCharacters() {
        assertExactRejected("J\u00d6B");
        assertExactRejected("\u0130JOB");
    }

    @Test
    public void testExactRejectionMessageNamesTheValue() {
        ZosBatchException e = Assert.assertThrows(ZosBatchException.class, () -> ZosBatchJobnames.exact("TOOLONGNAME"));
        Assert.assertTrue("message should contain the value, was: " + e.getMessage(), e.getMessage().contains("TOOLONGNAME"));
        Assert.assertTrue("message should contain the rule, was: " + e.getMessage(), e.getMessage().contains("between 1 and 8"));
    }

    // ---- randomWithPrefix: valid prefixes

    @Test
    public void testRandomWithPrefixIsEightCharactersStartingWithPrefix() throws Exception {
        IZosBatchJobname jobname = ZosBatchJobnames.randomWithPrefix("pay");
        Assert.assertEquals(8, jobname.getName().length());
        Assert.assertTrue("should start with the upper cased prefix, was: " + jobname.getName(), jobname.getName().startsWith("PAY"));
        // PAY, then exactly 5 upper case letters or digits to make 8 characters
        Assert.assertTrue("suffix should be upper case alphanumeric, was: " + jobname.getName(), jobname.getName().matches("^PAY[A-Z0-9]{5}$"));
    }

    @Test
    public void testRandomWithPrefixAcceptsOneCharacterPrefix() throws Exception {
        // A, then exactly 7 upper case letters or digits to make 8 characters
        Assert.assertTrue(ZosBatchJobnames.randomWithPrefix("A").getName().matches("^A[A-Z0-9]{7}$"));
    }

    @Test
    public void testRandomWithPrefixAcceptsSevenCharacterPrefix() throws Exception {
        // ABCDEFG, then exactly 1 upper case letter or digit to make 8 characters
        Assert.assertTrue(ZosBatchJobnames.randomWithPrefix("ABCDEFG").getName().matches("^ABCDEFG[A-Z0-9]$"));
    }

    @Test
    public void testRandomWithPrefixAcceptsNationalCharacters() throws Exception {
        // #$@ then exactly 5 upper case letters or digits. The $ in the prefix is escaped as it is a regex metacharacter
        Assert.assertTrue(ZosBatchJobnames.randomWithPrefix("#$@").getName().matches("^#\\$@[A-Z0-9]{5}$"));
    }

    @Test
    public void testRandomWithPrefixGeneratesDifferentNames() throws Exception {
        Set<String> names = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            names.add(ZosBatchJobnames.randomWithPrefix("GAL").getName());
        }
        // 36^5 combinations, so 50 samples being all different is overwhelmingly likely. Allow one collision to avoid flakiness
        Assert.assertTrue("names should be random, were: " + names, names.size() >= 49);
    }

    // ---- randomWithPrefix: invalid prefixes

    @Test
    public void testRandomWithPrefixRejectsNull() {
        assertRandomRejected(null);
    }

    @Test
    public void testRandomWithPrefixRejectsEmpty() {
        assertRandomRejected("");
    }

    @Test
    public void testRandomWithPrefixRejectsEightCharacters() {
        assertRandomRejected("ABCDEFGH");
    }

    @Test
    public void testRandomWithPrefixRejectsLeadingDigit() {
        assertRandomRejected("1AB");
    }

    @Test
    public void testRandomWithPrefixRejectsInvalidCharacters() {
        assertRandomRejected("MY-");
        assertRandomRejected("MY ");
    }

    private void assertExactRejected(String name) {
        Assert.assertThrows("name should be rejected: " + name, ZosBatchException.class, () -> ZosBatchJobnames.exact(name));
    }

    private void assertRandomRejected(String prefix) {
        Assert.assertThrows("prefix should be rejected: " + prefix, ZosBatchException.class, () -> ZosBatchJobnames.randomWithPrefix(prefix));
    }
}
