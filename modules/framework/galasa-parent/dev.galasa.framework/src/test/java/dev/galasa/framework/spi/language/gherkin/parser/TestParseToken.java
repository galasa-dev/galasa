/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin.parser;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

public class TestParseToken {
    @Test
    public void testOneParseTokenEqualsTheOther() throws Exception {
        ParseToken token1 = new ParseToken(TokenType.DATA_LINE,"abc",12);
        ParseToken token2 = new ParseToken(TokenType.DATA_LINE,"abc",12);
        assertThat(token1).isEqualTo(token2);
    }
    
    @Test
    public void testOneParseTokenAgainstNullDoesNotBlowUp() throws Exception {
        ParseToken token1 = new ParseToken(TokenType.DATA_LINE,"abc",12);
        if (token1.equals(null)) {
            fail("Could not compare token with null");
        }
    }

    @Test
    public void testOneParseTokenAgainstStringDoesNotBlowUp() throws Exception {
        ParseToken token1 = new ParseToken(TokenType.DATA_LINE,"abc",12);
        if (token1.equals("asdasd")) {
            fail("Could not compare token with string");
        }
    }

    @Test
    public void testOneParseTokenHashCodeEqualsTheOther() throws Exception {
        ParseToken token1 = new ParseToken(TokenType.DATA_LINE,"abc",12);
        ParseToken token2 = new ParseToken(TokenType.DATA_LINE,"abc",12);
        assertThat(token1.hashCode()).isEqualTo(token2.hashCode());
    }
    
    @Test
    public void testOneParseTokenWithTheOtherTypesDiffer() throws Exception {
        ParseToken token1 = new ParseToken(TokenType.DATA_LINE,"abc",12);
        ParseToken token2 = new ParseToken(TokenType.SCENARIO_START,"abc",12);
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    public void testOneParseTokenWithTheOtherTextDiffers() throws Exception {
        ParseToken token1 = new ParseToken(TokenType.DATA_LINE,"abc",12);
        ParseToken token2 = new ParseToken(TokenType.DATA_LINE,"abcd",12);
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    public void testOneParseTokenWithTheOtherLineNumbersDiffer() throws Exception {
        ParseToken token1 = new ParseToken(TokenType.DATA_LINE,"abc",12);
        ParseToken token2 = new ParseToken(TokenType.DATA_LINE,"abc",13);
        assertThat(token1).isNotEqualTo(token2);
    }
}
