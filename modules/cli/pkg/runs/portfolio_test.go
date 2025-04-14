/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runs

import (
	"testing"

	"github.com/galasa-dev/cli/pkg/files"
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/stretchr/testify/assert"
)

func createTestPortfolioFile(t *testing.T, fs spi.FileSystem, portfolioFilePath string, bundleName string, className string, stream string, obr string) *Portfolio {
	portfolio := NewPortfolio()

	testSelection := new(TestSelection)
	testSelection.Classes = make([]TestClass, 0)

	testSelection.Classes = append(testSelection.Classes, TestClass{
		Bundle: bundleName,
		Class:  className,
		Stream: stream,
		Obr:    obr,
	})

	testOverrides := make(map[string]string)

	AddClassesToPortfolio(testSelection, &testOverrides, portfolio)

	err := WritePortfolio(fs, portfolioFilePath, portfolio)

	assert.Nil(t, err)

	return portfolio
}

func TestCanWriteAndReadAPortfolio(t *testing.T) {
	fs := files.NewMockFileSystem()
	portfolio := createTestPortfolioFile(t, fs, "my.portfolio", "myBundle", "myClass", "myStream", "myObr")

	portfolioGotBack, err := ReadPortfolio(fs, "my.portfolio")
	assert.Nil(t, err)
	assert.Equal(t, 1, len(portfolio.Classes))
	assert.Equal(t, "myBundle", portfolioGotBack.Classes[0].Bundle)
	assert.Equal(t, "myClass", portfolioGotBack.Classes[0].Class)
	assert.Equal(t, "myStream", portfolioGotBack.Classes[0].Stream)
	assert.Equal(t, "myObr", portfolioGotBack.Classes[0].Obr)
}
