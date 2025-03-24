/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;


/**
 * A ProductVersion represents a {version}.{release}.{modification} level way of semantic numbering.
 * where {version} {release} and {modification} are integers, joined with a period.
 * 
 * This class allows you to create versions which can then be compared.
 * 
 */
public class ProductVersion implements Comparable<ProductVersion> {

    protected static final Pattern patternVersion = Pattern.compile("(\\d+)(\\.(\\d+)(\\.(\\d+))?)?");

    private final int version;
    private final int release;
    private final int modification;

    /**
     * @param version
     * @param release
     * @param modification
     * @throws ManagerException When any of the numbers are negative.
     */
    public ProductVersion(int version, int release, int modification) throws ManagerException {
        if (version < 0 || release < 0 || modification < 0 ) {
            throw new ManagerException(
                MessageFormat.format(
                    "Invalid product version string '{0}.{1},{2}' - all parts must be non-negative integers.",
                    Integer.toString(version), 
                    Integer.toString(release), 
                    Integer.toString(modification)
                )
            );
        }
        this.version = version;
        this.release = release;
        this.modification = modification;
    }

    public ProductVersion(ProductVersion productVersion) {
        this.version = productVersion.version;
        this.release = productVersion.release;
        this.modification = productVersion.modification;
    }


    /**
     * Note. Using this method is less efficient than using the class constructor.
     * @param version A version number. Negative numbers are treated as 0.
     * @return A ProductVersion which has the specified version, but the release and modification are zero.
     */
    public static ProductVersion v(int version) {
        ProductVersion result = null;
        try {
            result = new ProductVersion( Math.max(version,0), 0, 0);
        } catch(ManagerException ex) {
            // Ignored. Not possible.
        }
        return result ;
    }

    /**
     * Note. Using this method is less efficient than using the class constructor.
     * @param release A version number. Negative numbers are treated as 0.
     * @return A ProductVersion which has the the specified release
     */
    public ProductVersion r(int release) {
        ProductVersion result = null;
        try {
            result = new ProductVersion( version, Math.max(release, 0), 0);
        } catch(ManagerException ex) {
            // Ignored. Not possible.
        }
        return result ;
    }

    /**
     * Note. Using this method is less efficient than using the class constructor.
     * @param modification A version number. Negative numbers are treated as 0.
     * @return A ProductVersion which has the the specified modification level
     */
    public ProductVersion m(int modification) {
        ProductVersion result = null;
        try {
            result =  new ProductVersion( version, release, Math.max(modification, 0));
        } catch(ManagerException ex) {
            // Ignored. Not possible.
        }
        return result ;    
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + version ;
        result = prime * result + release ;
        result = prime * result + modification ;
        return result;
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

        int v = extractValue(matcherVersion,1);
        int r = extractValue(matcherVersion,3);
        int m = extractValue(matcherVersion,5);

        ProductVersion pVersion = new ProductVersion(v, r, m);
        return pVersion;
    }

    private static int extractValue(Matcher  matcherVersion , int groupIndex) {
        String partStr = matcherVersion.group(groupIndex);
        int partValue = 0 ;
        if (partStr != null) {
            partValue = Integer.parseInt(partStr);
        }
        return partValue ;
    }

}