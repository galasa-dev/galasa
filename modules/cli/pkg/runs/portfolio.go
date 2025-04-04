/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runs

import (
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/spi"
	"gopkg.in/yaml.v3"
)

type Portfolio struct {
	APIVersion string            `yaml:"apiVersion"`
	Kind       string            `yaml:"kind"`
	Metadata   PortfolioMetadata `yaml:"metadata"`

	Classes []PortfolioClass `yaml:"classes"`
}

type PortfolioMetadata struct {
	Name string `yaml:"name"`
}

type PortfolioClass struct {
	Bundle     string            `yaml:"bundle"`
	Class      string            `yaml:"class"`
	Stream     string            `yaml:"stream"`
	Obr        string            `yaml:"obr"`
	Overrides  map[string]string `yaml:"overrides"`
	GherkinUrl string            `yaml:"gherkin"`
}

func NewPortfolio() *Portfolio {
	portfolio := Portfolio{
		APIVersion: "v1alpha",
		Kind:       "galasa.dev/testPortfolio",
		Metadata:   PortfolioMetadata{Name: "adhoc"},
	}

	portfolio.Classes = make([]PortfolioClass, 0)

	return &portfolio
}

const (
	// Inside the portfolio file, it must carry a format field with this value inside.
	PORTOLIO_DECLARED_FORMAT_VERSION = "v1alpha"

	// Inside the portfolio file, it should claim to be a resource of this kind.
	PORTFOLIO_DECLARED_RESOURCE_KIND = "galasa.dev/testPortfolio"
)

func AddClassesToPortfolio(testSelection *TestSelection, testOverrides *map[string]string, portfolio *Portfolio) {

	for _, selectedClass := range testSelection.Classes {
		portfolioClass := PortfolioClass{
			Bundle:     selectedClass.Bundle,
			Class:      selectedClass.Class,
			Stream:     selectedClass.Stream,
			Obr:        selectedClass.Obr,
			Overrides:  *testOverrides,
			GherkinUrl: selectedClass.GherkinUrl,
		}
		portfolio.Classes = append(portfolio.Classes, portfolioClass)
	}
}

func WritePortfolio(fileSystem spi.FileSystem, filename string, portfolio *Portfolio) error {
	bytes, err := yaml.Marshal(&portfolio)
	if err == nil {
		err = fileSystem.WriteBinaryFile(filename, bytes)
	}
	return err
}

func ReadPortfolio(fileSystem spi.FileSystem, filename string) (*Portfolio, error) {

	var portfolio Portfolio

	text, err := fileSystem.ReadTextFile(filename)
	if err != nil {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_OPEN_PORTFOLIO_FILE_FAILED, filename, err.Error())
		return nil, err
	}

	err = yaml.Unmarshal([]byte(text), &portfolio)
	if err != nil {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_PORTFOLIO_BAD_FORMAT, filename, err.Error())
		return nil, err
	}

	// Check the portfolio file claims to be the correct format version.
	if portfolio.APIVersion != PORTOLIO_DECLARED_FORMAT_VERSION {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_PORTFOLIO_BAD_FORMAT_VERSION, filename, PORTOLIO_DECLARED_FORMAT_VERSION)
		return nil, err
	}

	if portfolio.Kind != PORTFOLIO_DECLARED_RESOURCE_KIND {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_PORTFOLIO_BAD_RESOURCE_KIND, filename, PORTFOLIO_DECLARED_RESOURCE_KIND)
		return nil, err
	}

	return &portfolio, nil
}
