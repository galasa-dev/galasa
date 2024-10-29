/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cmd

import (
	"bytes"
	"fmt"
	"io/ioutil"
	"text/template"

	"github.com/spf13/cobra"
	"gopkg.in/yaml.v3"

	"galasa.dev/buildUtilities/pkg/galasayaml"
)

var (
	templateFile        string
	releaseMetadata     *[]string
	outputFile          string
	requireObr          bool
	requireBom          bool
	requireMvp          bool
	requireIsolated     bool
	requireJavadoc      bool
	requireManagerdoc   bool
	requireCodeCoverage bool

	templateCmd = &cobra.Command{
		Use:   "template",
		Short: "generates files from a template",
		Long:  "Generates files from a template using the Galasa release metadata file",
		Run:   templateExecute,
	}

	release galasayaml.Release
)

type templateData struct {
	Release     string
	Artifacts   []artifact
	BootRelease string
}
type artifact struct {
	GroupId    string
	ArtifactId string
	Version    string
	Type       string
}

func init() {
	templateCmd.PersistentFlags().StringVarP(&templateFile, "template", "t", "", "template file")
	releaseMetadata = templateCmd.PersistentFlags().StringArrayP("releaseMetadata", "r", nil, "release metadata files")
	templateCmd.PersistentFlags().StringVarP(&outputFile, "output", "o", "", "output file")

	templateCmd.PersistentFlags().BoolVarP(&requireObr, "obr", "", false, "require maven artifacts for OBR")
	templateCmd.PersistentFlags().BoolVarP(&requireBom, "bom", "", false, "require maven artifacts for BOM")
	templateCmd.PersistentFlags().BoolVarP(&requireMvp, "mvp", "", false, "require maven artifacts for mvp zip")
	templateCmd.PersistentFlags().BoolVarP(&requireIsolated, "isolated", "", false, "require maven artifacts for isolated zip")
	templateCmd.PersistentFlags().BoolVarP(&requireJavadoc, "javadoc", "", false, "require maven artifacts for javadoc")
	templateCmd.PersistentFlags().BoolVarP(&requireManagerdoc, "managerdoc", "", false, "require maven artifacts for manager docs")
	templateCmd.PersistentFlags().BoolVarP(&requireCodeCoverage, "codecoverage", "", false, "require maven artifacts for code coverage processing")

	rootCmd.AddCommand(templateCmd)
}

func templateExecute(cmd *cobra.Command, args []string) {
	fmt.Printf("Galasa Build - Template - version %v\n", rootCmd.Version)

	if releaseMetadata == nil {
		panic("Release metadata files have not been provided")
	}

	if templateFile == "" {
		panic("Template file has not been provided")
	}

	if outputFile == "" {
		fmt.Println("Output file has not been provided")
	}

	// Read in all the metadata files,
	initial := true
	for _, inputFile := range *releaseMetadata {
		var inputRelease galasayaml.Release

		b, err := ioutil.ReadFile(inputFile)
		if err != nil {
			panic(err)
		}

		err = yaml.Unmarshal(b, &inputRelease)
		if err != nil {
			panic(err)
		}

		if initial {
			release = inputRelease
			initial = false
		} else {
			if inputRelease.Release.Version != "" {
				release.Release.Version = inputRelease.Release.Version
			}

			for _, inputBundle := range inputRelease.Framework.Bundles {
				release.Framework.Bundles = append(release.Framework.Bundles, inputBundle)
			}

			for _, inputBundle := range inputRelease.Api.Bundles {
				release.Framework.Bundles = append(release.Framework.Bundles, inputBundle)
			}

			for _, inputBundle := range inputRelease.Managers.Bundles {
				release.Framework.Bundles = append(release.Framework.Bundles, inputBundle)
			}

			for _, inputBundle := range inputRelease.External.Bundles {
				release.Framework.Bundles = append(release.Framework.Bundles, inputBundle)
			}
		}

	}

	if release.Release.Version == "" {
		panic("Release version not provided")
	}

	var requires = 0
	if requireObr {
		requires++
		fmt.Println("OBR artifact type requested")
	}
	if requireBom {
		requires++
		fmt.Println("BOM artifact type requested")
	}
	if requireMvp {
		requires++
		fmt.Println("MVP artifact type requested")
	}
	if requireIsolated {
		requires++
		fmt.Println("Isolated artifact type requested")
	}
	if requireJavadoc {
		requires++
		fmt.Println("Javadoc artifact type requested")
	}
	if requireManagerdoc {
		requires++
		fmt.Println("Manager Docs artifact type requested")
	}

	if requireCodeCoverage {
		requires++
		fmt.Println("Code Coverage artifact type requested")
	}

	if requires == 0 {
		panic("Artifact type has not been provided")
	}

	if requires > 1 {
		panic("Too many artifact types have been requested")
	}

	t := templateData{}

	t.Release = release.Release.Version
	fmt.Printf("Release version is %v\n", t.Release)

	for _, bundle := range release.Framework.Bundles {
		if bundle.Group == "" {
			bundle.Group = "dev.galasa"
		}

		selected := false

		if requireObr {
			selected = bundle.Obr
		} else if requireBom {
			selected = bundle.Bom
		} else if requireMvp {
			selected = bundle.Mvp
		} else if requireIsolated {
			selected = true
		} else if requireJavadoc {
			selected = bundle.Javadoc
		} else if requireManagerdoc {
			selected = bundle.Managerdoc
		} else if requireCodeCoverage {
			selected = bundle.Codecoverage
		}

		if selected {
			artifact := artifact{
				GroupId:    bundle.Group,
				ArtifactId: bundle.Artifact,
				Version:    bundle.Version,
				Type:       bundle.Type,
			}

			t.Artifacts = append(t.Artifacts, artifact)

			fmt.Printf("    Added framework artifact %v:%v:%v\n", artifact.GroupId, artifact.ArtifactId, artifact.Version)
		}

		if bundle.Artifact == "galasa-boot" {
			t.BootRelease = bundle.Version
			fmt.Printf("    Set galasa-boot version to %v\n", bundle.Version)
		}
	}

	for _, bundle := range release.Api.Bundles {
		if bundle.Group == "" {
			bundle.Group = "dev.galasa"
		}

		selected := false

		if requireObr {
			selected = bundle.Obr
		} else if requireBom {
			selected = bundle.Bom
		} else if requireMvp {
			selected = bundle.Mvp
		} else if requireIsolated {
			selected = true
		} else if requireJavadoc {
			selected = bundle.Javadoc
		} else if requireManagerdoc {
			selected = bundle.Managerdoc
		} else if requireCodeCoverage {
			selected = bundle.Codecoverage
		}

		if selected {
			artifact := artifact{
				GroupId:    bundle.Group,
				ArtifactId: bundle.Artifact,
				Version:    bundle.Version,
				Type:       bundle.Type,
			}

			t.Artifacts = append(t.Artifacts, artifact)

			fmt.Printf("    Added framework artifact %v:%v:%v\n", artifact.GroupId, artifact.ArtifactId, artifact.Version)
		}
	}

	for _, bundle := range release.Managers.Bundles {
		if bundle.Group == "" {
			bundle.Group = "dev.galasa"
		}

		selected := false

		if requireObr {
			selected = bundle.Obr
		} else if requireBom {
			selected = bundle.Bom
		} else if requireMvp {
			selected = bundle.Mvp
		} else if requireIsolated {
			selected = true
		} else if requireJavadoc {
			selected = true
		} else if requireManagerdoc {
			selected = true
		} else if requireCodeCoverage {
			selected = bundle.Codecoverage
		}

		if selected {
			artifact := artifact{
				GroupId:    bundle.Group,
				ArtifactId: bundle.Artifact,
				Version:    bundle.Version,
				Type:       bundle.Type,
			}

			t.Artifacts = append(t.Artifacts, artifact)

			fmt.Printf("    Added framework artifact %v:%v:%v\n", artifact.GroupId, artifact.ArtifactId, artifact.Version)
		}
	}

	for _, bundle := range release.External.Bundles {
		if bundle.Group == "" {
			bundle.Group = "dev.galasa"
		}

		selected := false

		if requireObr {
			selected = bundle.Obr
		} else if requireBom {
			selected = bundle.Bom
		} else if requireMvp {
			selected = bundle.Mvp
		} else if requireIsolated {
			selected = bundle.Isolated
		} else if requireJavadoc {
			selected = false
		} else if requireManagerdoc {
			selected = false
		} else if requireCodeCoverage {
			selected = bundle.Codecoverage
		}

		if selected {
			artifact := artifact{
				GroupId:    bundle.Group,
				ArtifactId: bundle.Artifact,
				Version:    bundle.Version,
				Type:       bundle.Type,
			}

			t.Artifacts = append(t.Artifacts, artifact)

			fmt.Printf("    Added framework artifact %v:%v:%v\n", artifact.GroupId, artifact.ArtifactId, artifact.Version)
		}
	}

	b, err := ioutil.ReadFile(templateFile)
	if err != nil {
		panic(err)
	}

	templString := string(b)

	tmpl, err := template.New("convert").Parse(templString)
	if err != nil {
		panic(err)
	}

	var buf bytes.Buffer

	err = tmpl.Execute(&buf, t)
	if err != nil {
		panic(err)
	}

	ioutil.WriteFile(outputFile, buf.Bytes(), 0644)

}
