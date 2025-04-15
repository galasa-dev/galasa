/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runs

import (
	"log"
	"regexp"
	"strings"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/launcher"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/spf13/cobra"
)

// A test selection is a collection of tests extracted either from a portfolio, or
// built using explicit parameters.
// Much of this code is used by the `runs prepare` and the `runs submit` code.
// Flags to control the test selection are added to both commands.

type TestSelection struct {
	Classes []TestClass
}

type TestClass struct {
	Bundle string
	Class  string

	// The stream will be set for ecosystem runs.
	Stream string

	// The obr will be set for local runs.
	Obr string

	//Gherkin URL for local runs
	GherkinUrl string
}

func NewTestSelectionFlagValues() *utils.TestSelectionFlagValues {
	flags := new(utils.TestSelectionFlagValues)
	flags.Bundles = new([]string)
	flags.Packages = new([]string)
	flags.Tests = new([]string)
	flags.Tags = new([]string)
	flags.Classes = new([]string)
	flags.RegexSelect = new(bool)
	flags.GherkinUrl = new([]string)
	return flags
}

type TestSelectionFlagValidator interface {
	Validate(flags *utils.TestSelectionFlagValues) error
}

type StreamBasedValidator struct {
}

func NewStreamBasedValidator() TestSelectionFlagValidator {
	return new(StreamBasedValidator)
}

func (*StreamBasedValidator) Validate(flags *utils.TestSelectionFlagValues) error {
	var err error
	if flags.Stream == "" {
		if len(*flags.Packages) > 0 || len(*flags.Bundles) > 0 || len(*flags.Tests) > 0 || len(*flags.Classes) > 0 {
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_STREAM_FLAG_REQUIRED)
		}
	}
	return err
}

type ObrBasedValidator struct {
}

func NewObrBasedValidator() TestSelectionFlagValidator {
	return new(ObrBasedValidator)
}

func (*ObrBasedValidator) Validate(flags *utils.TestSelectionFlagValues) error {
	var err error
	return err
}

// Adds a ton of flags to a cobra command like 'runs prepare' or 'runs submit'.
// The flags are consistently added as a result.
func AddCommandFlags(command *cobra.Command, flags *utils.TestSelectionFlagValues) {
	flags.Packages = command.Flags().StringSlice("package", make([]string, 0), "packages of which tests will be selected from, packages are selected if the name contains this string, or if --regex is specified then matches the regex")
	flags.Bundles = command.Flags().StringSlice("bundle", make([]string, 0), "bundles of which tests will be selected from, bundles are selected if the name contains this string, or if --regex is specified then matches the regex")
	flags.Tests = command.Flags().StringSlice("test", make([]string, 0), "test names which will be selected if the name contains this string, or if --regex is specified then matches the regex")
	flags.Tags = command.Flags().StringSlice("tag", make([]string, 0), "tags of which tests will be selected from, tags are selected if the name contains this string, or if --regex is specified then matches the regex")

	command.Flags().StringVarP(&flags.Stream, "stream", "s", "", "test stream to extract the tests from")
	flags.RegexSelect = command.Flags().Bool("regex", false, "Test selection is performed by using regex")

	AddClassFlag(command, flags, false, "test class names to run from the specified stream or portfolio."+
		" The format of each entry is {osgi-bundle-name}/{java-class-name}. "+
		" Multiple values can be supplied using a comma-separated list of values, or by using multiple instances of the --class flag."+
		" Java class names are fully qualified. No .class suffix is needed.")

	AddGherkinFlag(command, flags, false, "Gherkin feature file URL. Should start with 'file://'. ")
}

func AddClassFlag(command *cobra.Command, flags *utils.TestSelectionFlagValues, isRequired bool, helpText string) {
	flags.Classes = command.Flags().StringSlice("class", make([]string, 0), helpText)
	if isRequired {
		command.MarkFlagRequired("class")
	}
}

func AddGherkinFlag(command *cobra.Command, flags *utils.TestSelectionFlagValues, isRequired bool, helpText string) {
	flags.GherkinUrl = command.Flags().StringSlice("gherkin", make([]string, 0), helpText)
	if isRequired {
		command.MarkFlagRequired("gherkin")
	}
}

func AreSelectionFlagsProvided(flags *utils.TestSelectionFlagValues) bool {
	if len(*flags.Bundles) > 0 {
		return true
	}

	if len(*flags.Packages) > 0 {
		return true
	}

	if len(*flags.Tests) > 0 {
		return true
	}

	if len(*flags.Tags) > 0 {
		return true
	}

	if len(*flags.Classes) > 0 {
		return true
	}

	if len(*flags.GherkinUrl) > 0 {
		return true
	}

	if flags.Stream != "" {
		return true
	}

	return false
}

func SelectTests(launcherInstance launcher.Launcher, flags *utils.TestSelectionFlagValues) (TestSelection, error) {

	var testSelection TestSelection
	var err error

	var testCatalog launcher.TestCatalog

	if flags.Stream != "" {
		var availableStreams []string
		availableStreams, err = GetStreams(launcherInstance)
		if err == nil {

			err = ValidateStream(availableStreams, flags.Stream)
			if err == nil {

				testCatalog, err = launcherInstance.GetTestCatalog(flags.Stream)
				if err == nil {
					log.Println("Test catalog retrieved")
				}
			}
		}
	}

	if err == nil {
		testSelection = TestSelection{Classes: make([]TestClass, 0)}

		err = selectTestsByBundle(testCatalog, &testSelection, flags)

		if err == nil {
			err = selectTestsByPackage(testCatalog, &testSelection, flags)
		}
		if err == nil {
			err = selectTestsByTest(testCatalog, &testSelection, flags)
		}
		if err == nil {
			err = selectTestsByTag(testCatalog, &testSelection, flags)
		}
		if err == nil {
			err = selectTestsByClass(testCatalog, &testSelection, flags)
		}
		if err == nil {
			err = selectTestsByGherkin(&testSelection, flags)
		}
	}

	return testSelection, err
}

func selectTestsByGherkin(testSelection *TestSelection, flags *utils.TestSelectionFlagValues) error {
	var err error

	if len(*flags.GherkinUrl) < 1 {
		return err
	}
	gherkinUrls := *flags.GherkinUrl
	for _, gherkin := range gherkinUrls {

		newSelectedClass := TestClass{
			GherkinUrl: gherkin,
		}

		testSelection.Classes = append(testSelection.Classes, newSelectedClass)
	}

	return err
}

func selectTestsByBundle(testCatalog launcher.TestCatalog, testSelection *TestSelection, flags *utils.TestSelectionFlagValues) error {

	var err error

	if len(*flags.Bundles) < 1 {
		return err
	}

	if testCatalog["classes"] == nil {
		return err
	}
	var regexPatterns *[]*regexp.Regexp

	regexPatterns, err = convertRegex(flags.Bundles, *flags.RegexSelect)
	if err == nil {
		availableClasses := testCatalog["classes"].(map[string]interface{})

		for _, oclassDef := range availableClasses {
			classDef := oclassDef.(map[string]interface{})
			for _, regexBundle := range *regexPatterns {
				if regexBundle.MatchString(classDef["bundle"].(string)) {
					selectClassByCatalog(testSelection, classDef, flags)
					break
				}
			}
		}
	}
	return err
}

func selectTestsByPackage(testCatalog launcher.TestCatalog, testSelection *TestSelection, flags *utils.TestSelectionFlagValues) error {
	var err error

	if len(*flags.Packages) < 1 {
		return err
	}

	if testCatalog["classes"] == nil {
		return err
	}

	var regexPatterns *[]*regexp.Regexp
	regexPatterns, err = convertRegex(flags.Packages, *flags.RegexSelect)
	if err == nil {
		availableClasses := testCatalog["classes"].(map[string]interface{})

		for _, oclassDef := range availableClasses {
			classDef := oclassDef.(map[string]interface{})
			for _, regexBundle := range *regexPatterns {
				if regexBundle.MatchString(classDef["package"].(string)) {
					selectClassByCatalog(testSelection, classDef, flags)
					break
				}
			}
		}
	}
	return err
}

func selectTestsByTest(testCatalog launcher.TestCatalog, testSelection *TestSelection, flags *utils.TestSelectionFlagValues) error {

	var err error

	if len(*flags.Tests) < 1 {
		return err
	}

	if testCatalog["classes"] == nil {
		return err
	}
	var regexPatterns *[]*regexp.Regexp
	regexPatterns, err = convertRegex(flags.Tests, *flags.RegexSelect)
	if err == nil {
		availableClasses := testCatalog["classes"].(map[string]interface{})

		for _, oclassDef := range availableClasses {
			classDef := oclassDef.(map[string]interface{})
			for _, regexBundle := range *regexPatterns {
				if regexBundle.MatchString(classDef["name"].(string)) {
					selectClassByCatalog(testSelection, classDef, flags)
					break
				}
			}
		}
	}
	return err
}

func selectTestsByClass(
	testCatalog launcher.TestCatalog, // Shouldn't this be used ?
	testSelection *TestSelection,
	flags *utils.TestSelectionFlagValues,
) error {

	var err error
	if len(*flags.Classes) < 1 {
		return err
	}

	for _, class := range *flags.Classes {
		pos := strings.Index(class, "/")
		if pos < 1 {
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_CLASS_FORMAT, class)
			break
		}

		bundle := class[:pos]
		name := class[pos+1:]

		if name == "" {
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_CLASS_NAME_BLANK, class)
			break
		}

		selectClass(testSelection, bundle, name, flags)
	}
	return err
}

func selectTestsByTag(testCatalog launcher.TestCatalog, testSelection *TestSelection, flags *utils.TestSelectionFlagValues) error {

	var err error
	if len(*flags.Tags) < 1 {
		return err
	}

	if testCatalog["classes"] == nil {
		return err
	}

	var regexPatterns *[]*regexp.Regexp
	regexPatterns, err = convertRegex(flags.Tags, *flags.RegexSelect)
	availableClasses := testCatalog["classes"].(map[string]interface{})

classSearch:
	for _, oclassDef := range availableClasses {
		classDef := oclassDef.(map[string]interface{})
		oclassTags := classDef["tags"]
		if oclassTags != nil {
			for _, itag := range oclassTags.([]interface{}) {
				tag := itag.(string)

				for _, regexBundle := range *regexPatterns {
					if regexBundle.MatchString(tag) {
						selectClassByCatalog(testSelection, classDef, flags)
						continue classSearch
					}
				}
			}
		}
	}
	return err
}

func selectClassByCatalog(testSelection *TestSelection, appendClass map[string]interface{}, flags *utils.TestSelectionFlagValues) {

	bundle := appendClass["bundle"].(string)
	name := appendClass["name"].(string)

	selectClass(testSelection, bundle, name, flags)
}

func selectClass(testSelection *TestSelection, bundle string, name string, flags *utils.TestSelectionFlagValues) {

	for _, selectedClass := range testSelection.Classes {
		if bundle == selectedClass.Bundle &&
			name == selectedClass.Class &&
			flags.Stream == selectedClass.Stream {

			log.Printf("    Test class '%v/%v' is already selected.\n", bundle, name)
			return // already selected
		}
	}

	newSelectedClass := TestClass{
		Bundle: bundle,
		Class:  name,
		Stream: flags.Stream,
	}

	testSelection.Classes = append(testSelection.Classes, newSelectedClass)

	log.Printf("    Selected test class '%v/%v'\n", bundle, name)
}

func convertRegex(patterns *[]string, regexSelect bool) (*[]*regexp.Regexp, error) {

	var err error
	regexPatterns := make([]*regexp.Regexp, 0)
	var r *regexp.Regexp
	if regexSelect {
		// Create patterns of actual regex
		for _, selection := range *patterns {
			r, err = regexp.Compile(selection)
			if err != nil {
				err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_SELECTION_REGEX_ERROR, selection, err.Error())
				break
			}
			regexPatterns = append(regexPatterns, r)
		}
	} else {
		// Create patterns of quoted regex
		for _, selection := range *patterns {
			r, err = regexp.Compile("\\Q" + selection + "\\E")
			if err != nil {
				err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_SELECTION_REGEX_QUOTED_ERROR, selection, err.Error())
				break
			} else {
				regexPatterns = append(regexPatterns, r)
			}
		}
	}

	return &regexPatterns, err
}
