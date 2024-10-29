/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

public class ProductVersion implements Comparable<ProductVersion> {

    protected static final Pattern patternVersion = Pattern.compile("(\\d+)(\\.(\\d+)(\\.(\\d+))?)?");

    private final int version;
    private final int release;
    private final int modification;

    protected ProductVersion(int version, int release, int modification) {
        this.version = version;
        this.release = release;
        this.modification = modification;
    }

    protected ProductVersion(ProductVersion productVersion) {
        this.version = productVersion.version;
        this.release = productVersion.release;
        this.modification = productVersion.modification;
    }

    public static ProductVersion v(int version) {
        return new ProductVersion(version, 0, 0);
    }

    public ProductVersion r(int release) {
        return new ProductVersion(this.version, release, 0);
    }

    public ProductVersion m(int modification) {
        return new ProductVersion(this.version, this.release, modification);
    }

    @Override
    public String toString() {
        return Integer.toString(this.version) + "." + Integer.toString(this.release) + "."
                + Integer.toString(this.modification);
    }

    @Override
    public boolean equals(@NotNull Object other) {
        if (!(other instanceof ProductVersion)) {
            return false;
        }

        ProductVersion o = (ProductVersion) other;

        if (this.version != o.version || this.release != o.release || this.modification != o.modification) {
            return false;
        }

        return true;
    }

    @Override
    public int compareTo(@NotNull ProductVersion o) {
        int c = this.version - o.version;
        if (c != 0) {
            return c;
        } 

        c = this.release - o.release;
        if (c != 0) {
            return c;
        } 

        return this.modification - o.modification;
    }

    public boolean isEarlierThan(@NotNull ProductVersion o) {
        return (compareTo(o) < 0);
    }

    public boolean isLaterThan(@NotNull ProductVersion o) {
        return (compareTo(o) > 0);
    }

    public static ProductVersion parse(@NotNull String versionString) throws ManagerException {
        Matcher matcherVersion = patternVersion.matcher(versionString);
        if (!matcherVersion.matches()) {
            throw new ManagerException("Invalid product version string '" + versionString + "'");
        }

        String v = matcherVersion.group(1);
        String r = matcherVersion.group(3);
        String m = matcherVersion.group(5);

        ProductVersion pVersion = ProductVersion.v(Integer.parseInt(v));

        if (r != null) {
            pVersion = pVersion.r(Integer.parseInt(r));
        }

        if (m != null) {
            pVersion = pVersion.m(Integer.parseInt(m));
        }

        return pVersion;
    }

}