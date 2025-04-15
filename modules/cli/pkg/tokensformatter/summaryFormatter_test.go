/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tokensformatter

import (
	"testing"

	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/stretchr/testify/assert"
)

func CreateMockAuthToken(id string, creationTime string, LoginId string, description string) *galasaapi.AuthToken {
	var token = galasaapi.NewAuthToken()

	token.SetTokenId(id)
	token.SetCreationTime(creationTime)

	owner := galasaapi.NewUser()
	owner.SetLoginId(LoginId)
	token.SetOwner(*owner)

	token.SetDescription(description)

	return token
}

func TestTokenSummaryFormatterNoDataReturnsTotalCountAllZeros(t *testing.T) {
	//Given...
	formatter := NewTokenSummaryFormatter()
	// No data to format...
	tokens := make([]galasaapi.AuthToken, 0)

	// When...
	actualFormattedOutput, err := formatter.FormatTokens(tokens)

	//Then...
	assert.Nil(t, err)
	expectedFormattedOutput := "Total:0\n"
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestTokenSummaryFormatterSingleDataReturnsCorrectly(t *testing.T) {
	// Given...
	formatter := NewTokenSummaryFormatter()
	// No data to format...
	tokens := make([]galasaapi.AuthToken, 0)
	token1 := CreateMockAuthToken("098234980123-1283182389", "2023-12-03T18:25:43.511Z", "mcobbett", "So I can access ecosystem1 from my laptop.")
	tokens = append(tokens, *token1)

	// When...
	actualFormattedOutput, err := formatter.FormatTokens(tokens)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput :=
		`tokenid                 created(YYYY-MM-DD) user     description
098234980123-1283182389 2023-12-03          mcobbett So I can access ecosystem1 from my laptop.

Total:1
`
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestTokenSummaryFormatterMultipleDataSeperatesWithNewLine(t *testing.T) {
	// For..
	formatter := NewTokenSummaryFormatter()
	// No data to format...
	tokens := make([]galasaapi.AuthToken, 0)
	token1 := CreateMockAuthToken("098234980123-1283182389", "2023-12-03T18:25:43.511Z", "mcobbett", "So I can access ecosystem1 from my laptop.")
	token2 := CreateMockAuthToken("8218971d287s1-dhj32er2323", "2024-03-03T09:36:50.511Z", "mcobbett", "Automated build of example repo can change CPS properties")
	token3 := CreateMockAuthToken("87a6sd87ahq2-2y8hqwdjj273", "2023-08-04T23:00:23.511Z", "savvas", "CLI access from vscode")
	tokens = append(tokens, *token1, *token2, *token3)

	// When...
	actualFormattedOutput, err := formatter.FormatTokens(tokens)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput := `tokenid                   created(YYYY-MM-DD) user     description
098234980123-1283182389   2023-12-03          mcobbett So I can access ecosystem1 from my laptop.
8218971d287s1-dhj32er2323 2024-03-03          mcobbett Automated build of example repo can change CPS properties
87a6sd87ahq2-2y8hqwdjj273 2023-08-04          savvas   CLI access from vscode

Total:3
`
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}
