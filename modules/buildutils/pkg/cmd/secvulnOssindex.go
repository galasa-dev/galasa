/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"encoding/json"
	"encoding/xml"
	"fmt"
	"io"
	"io/fs"
	"os"
	"path/filepath"
	"regexp"
	"strings"

	"github.com/spf13/cobra"
	"gopkg.in/yaml.v3"
)

var (
	secvulnOssindexCmd = &cobra.Command{
		Use:   "ossindex",
		Short: "Extract OSS Index reports",
		Long:  "Extract OSS Index reports",
		Run:   secvulnOssindexExecute,
	}

	secvulnOssindexParentDir string
	secvulnOssindexOutput    string

	cveInfoMap = make(map[string]map[string]interface{})

	depChainMap = make(map[string]map[string][]string)

	projectHierarchyMap = make(map[string]map[string][]string)

	processedProjectCount int
)

func init() {
	secvulnOssindexCmd.PersistentFlags().StringVar(&secvulnOssindexParentDir, "parent", "", "Parent project directory")
	secvulnOssindexCmd.PersistentFlags().StringVar(&secvulnOssindexOutput, "output", "", "Output yaml extract")

	secvulnOssindexCmd.MarkPersistentFlagRequired("parent")
	secvulnOssindexCmd.MarkPersistentFlagRequired("output")

	secvulnCmd.AddCommand(secvulnOssindexCmd)
}

func secvulnOssindexExecute(cmd *cobra.Command, args []string) {
	fmt.Printf("Galasa Build - Security Vulnerability OSS Index - version %v\n", rootCmd.Version)

	// Find all target directories in the structure of the provided parent directory
	findAuditReports(secvulnOssindexParentDir)

	// Create the yaml report of all vulnerabilities found
	secVulnYamlReport := createReport()

	// Export to provided location
	exportReport(*secVulnYamlReport)
}

func findAuditReports(directory string) {
	err := filepath.Walk(directory, walkFunc)
	if err != nil {
		fmt.Printf("Error walking the path %s, %v\n", directory, err)
		panic(err)
	}
}

func walkFunc(path string, info fs.FileInfo, err error) error {
	if err != nil {
		fmt.Printf("Could not access path %s, %v\n", path, err)
		return err
	}

	if info.Name() == "audit-report.json" {
		// OSS Index report found
		auditReport, _ := os.ReadFile(path)
		newPath := strings.Replace(path, "/target/audit-report.json", "", -1)
		// Pass the audit report for scanning
		// and the path two dirs up to find the pom and digraph later
		scanAuditReportForVulnerabilities(auditReport, newPath)
		return nil
	}
	return nil
}

func scanAuditReportForVulnerabilities(file []byte, directory string) {

	// Audit report is unstructered JSON so cannot use structs
	var auditReport map[string]interface{}
	err := json.Unmarshal([]byte(file), &auditReport)
	if err != nil {
		fmt.Printf("Unable to unmarshal the audit report in %s, %v\n", directory, err)
	}

	vulnerableArtifacts := auditReport["vulnerable"]

	// If this audit report has a vulnerable section, iterate through the vulnerable artifacts
	if vulnerableArtifacts != nil {

		for vulnerableArtifact, artifactDetails := range vulnerableArtifacts.(map[string]interface{}) {

			// Each vulnerable artifact might have more than one CVE so iterate through them
			vulnerabilities := artifactDetails.(map[string]interface{})["vulnerabilities"]

			for _, vulnerability := range vulnerabilities.([]interface{}) {

				/*
					Get the information needed for the Yaml report
					- the CVE
					- the CVSS Score
					- the name of the vulnerable external artifact
					- the name of the Galasa artifact first in the dependency chain (fix point)
					- the dependency chain
					- other Galasa artifacts indirectly affected by this
				*/

				var cve string
				if vulnerability.(map[string]interface{})["cve"] != nil {
					cve = vulnerability.(map[string]interface{})["cve"].(string)
				} else {
					cve = vulnerability.(map[string]interface{})["id"].(string)
				}

				cvssScore := vulnerability.(map[string]interface{})["cvssScore"].(float64)

				ref := vulnerability.(map[string]interface{})["reference"].(string)
				array := strings.Split(ref, "?component")
				reference := array[0]

				if cveInfoMap[cve] == nil {
					cveInfoMap[cve] = make(map[string]interface{})
					cveInfoMap[cve]["cvssScore"] = cvssScore
					cveInfoMap[cve]["reference"] = reference
					// There may be multiple vulnerable artifacts affected by this CVE
					var vulnArtifacts []string
					vulnArtifacts = append(vulnArtifacts, vulnerableArtifact)
					cveInfoMap[cve]["vulnerableArtifacts"] = vulnArtifacts
				} else {
					if arrayContainsString(vulnerableArtifact, cveInfoMap[cve]["vulnerableArtifacts"].([]string)) == false {
						cveInfoMap[cve]["vulnerableArtifacts"] = append(cveInfoMap[cve]["vulnerableArtifacts"].([]string), vulnerableArtifact)
					}
				}

				// Get the Galasa artifact name as it is not included in the audit-report.json
				galasaArtifactString := getGalasaArtifactString(directory)

				digraph := getDigraph(directory)

				// Process the digraph to determine the dependency chains between Galasa artifacts and
				// vulnerabilities, and find out if Galasa artifacts are directly or indirectly affected
				processDigraph(cve, digraph, vulnerableArtifact, galasaArtifactString)
			}
		}
	}

	processedProjectCount++
}

func processDigraph(cve, digraph, vulnerability, galasaArtifactString string) {

	// Regex for all lines in the digraph with two artifacts separated by ->
	// First capture group is the artifact before the arrow, second is the artifact after the arrow
	regex := "([a-zA-Z0-9.:_-]+)\"\\s->\\s\"([a-zA-Z0-9.:_-]+)"
	re := regexp.MustCompile(regex)

	submatches := re.FindAllStringSubmatch(digraph, -1)

	dependencyChain := processDependencyChain(submatches, cve, galasaArtifactString, vulnerability)

	// Form the dependency chain string for the yaml report by reversing the array
	dependencyChainString := getDependencyChainAsString(dependencyChain)

	galasaArtifact := getGroupAndArtifact(galasaArtifactString)
	vulnerableArtifact := getGroupArtifactVersion(vulnerability)
	addToDepChainMap(galasaArtifact, vulnerableArtifact, dependencyChainString)

}

func getDependencyChainAsString(dependencyChain []string) string {
	var dependencyChainString string
	for i := len(dependencyChain) - 2; i > -1; i-- {
		if i == 0 {
			dependencyChainString += dependencyChain[i]
		} else {
			dependencyChainString += dependencyChain[i] + " -> "
		}
	}
	return dependencyChainString
}

func processDependencyChain(submatches [][]string, cve, galasaArtifactString string, vulnerability string) []string {

	// Start forming the dependency chain
	var dependencyChain []string
	dependencyChain = append(dependencyChain, vulnerability)

	// Start looking for the vulnerability first then work backwards to the Galasa artifact
	targetString := vulnerability

	// For finding the first dev.galasa artifact in the chain
	firstArtifactFound := false
	firstArtifact := ""

	maxLoops := 100
	count := 0
	for targetString != galasaArtifactString {

		if count == maxLoops {
			msg := fmt.Sprintf("Too many attempts to parse dependency chain from %s to %s\n", galasaArtifactString, vulnerability)
			fmt.Printf(msg)
			panic(msg)
		}

		for _, submatch := range submatches {

			// If the second capture group is the current target string, change target string to the first capture group and repeat
			// Compare the artifact names only as versions might be different
			if getGroupAndArtifact(submatch[2]) == getGroupAndArtifact(targetString) {

				dependencyChain = append(dependencyChain, submatch[1])
				targetString = submatch[1]

				/* Find first dev.galasa artifact that this vulnerability is found in and
				determine which dev.galasa artifacts are indirectly affected by it
				*/
				if strings.HasPrefix(submatch[1], "dev.galasa") {

					// Add to dep chain map, as this vulnerability/galasa artifact pair might not have been processed
					depChainString := getDependencyChainAsString(dependencyChain)
					addToDepChainMap(getGroupAndArtifact(submatch[1]), getGroupAndArtifact(vulnerability), depChainString)

					if firstArtifactFound == false {
						firstArtifact = submatch[1]
						firstArtifactFound = true
						if projectHierarchyMap[vulnerability] != nil {
							if projectHierarchyMap[vulnerability][firstArtifact] == nil {
								var artifactArray []string
								projectHierarchyMap[vulnerability][firstArtifact] = artifactArray
							}
						} else {
							projectHierarchyMap[vulnerability] = make(map[string][]string)
							var artifactArray []string
							projectHierarchyMap[vulnerability][firstArtifact] = artifactArray
						}
					} else if firstArtifactFound == true {
						if arrayContainsString(submatch[1], projectHierarchyMap[vulnerability][firstArtifact]) == false {
							projectHierarchyMap[vulnerability][firstArtifact] = append(projectHierarchyMap[vulnerability][firstArtifact], submatch[1])
						}
					}
				}
				break
			}

		}
		count++
	}

	return dependencyChain

}

func addToDepChainMap(galasaArtifact, vulnerableArtifact, dependencyChainString string) {
	if depChainMap[galasaArtifact] == nil {
		depChainMap[galasaArtifact] = make(map[string][]string)
		depChainMap[galasaArtifact][vulnerableArtifact] = append(depChainMap[galasaArtifact][vulnerableArtifact], dependencyChainString)
	} else {
		if arrayContainsString(dependencyChainString, depChainMap[galasaArtifact][vulnerableArtifact]) == false {
			depChainMap[galasaArtifact][vulnerableArtifact] = append(depChainMap[galasaArtifact][vulnerableArtifact], dependencyChainString)
		}
	}

}

func createReport() *SecVulnYamlReport {

	var yamlVulns []Vulnerability

	for cve, cveInfo := range cveInfoMap {

		var yamlVulnArtifacts []VulnerableArtifact

		vulnerableArtifacts := cveInfo["vulnerableArtifacts"].([]string)

		for _, vulnerableArtifact := range vulnerableArtifacts {

			var yamlDirectProjects []DirectProject

			directlyAffectedProjects := projectHierarchyMap[vulnerableArtifact]

			for directProject, innerProjects := range directlyAffectedProjects {

				var yamlTransientProjs []TransientProject

				for _, innerProject := range innerProjects {

					var depChain string
					if len(depChainMap[getGroupAndArtifact(innerProject)][getGroupArtifactVersion(vulnerableArtifact)]) == 1 {
						depChain = depChainMap[getGroupAndArtifact(innerProject)][getGroupArtifactVersion(vulnerableArtifact)][0]
					} else if len(depChainMap[getGroupAndArtifact(innerProject)][getGroupArtifactVersion(vulnerableArtifact)]) > 1 {
						msg := fmt.Sprintf("Multiple dependency chains found from %s to %s\n", innerProject, vulnerableArtifact)
						fmt.Printf(msg)
						panic(msg)
					} else if len(depChainMap[getGroupAndArtifact(innerProject)][getGroupArtifactVersion(vulnerableArtifact)]) == 0 {
						msg := fmt.Sprintf("Unable to find dependency chain from %s to %s\n", innerProject, vulnerableArtifact)
						fmt.Printf(msg)
						panic(msg)
					}

					transientProj := &TransientProject{
						ProjectName:     innerProject,
						DependencyChain: depChain,
					}
					yamlTransientProjs = append(yamlTransientProjs, *transientProj)

				}

				var directDepChain string
				if len(depChainMap[getGroupAndArtifact(directProject)][getGroupArtifactVersion(vulnerableArtifact)]) == 1 {
					directDepChain = depChainMap[getGroupAndArtifact(directProject)][getGroupArtifactVersion(vulnerableArtifact)][0]
				} else if len(depChainMap[getGroupAndArtifact(directProject)][getGroupArtifactVersion(vulnerableArtifact)]) > 1 {
					msg := fmt.Sprintf("Multiple dependency chains found from %s to %s\n", directProject, vulnerableArtifact)
					fmt.Printf(msg)
					panic(msg)
				} else if len(depChainMap[getGroupAndArtifact(directProject)][getGroupArtifactVersion(vulnerableArtifact)]) == 0 {
					msg := fmt.Sprintf("Unable to find dependency chain from %s to %s\n", directProject, vulnerableArtifact)
					fmt.Printf(msg)
					panic(msg)
				}

				directProject := &DirectProject{
					ProjectName:       directProject,
					DependencyChain:   directDepChain,
					TransientProjects: yamlTransientProjs,
				}
				yamlDirectProjects = append(yamlDirectProjects, *directProject)

			}

			vulnArtifact := &VulnerableArtifact{
				VulnerableArtifact: vulnerableArtifact,
				DirectProjects:     yamlDirectProjects,
			}

			yamlVulnArtifacts = append(yamlVulnArtifacts, *vulnArtifact)

		}

		vuln := &Vulnerability{
			Cve:                 cve,
			CvssScore:           cveInfo["cvssScore"].(float64),
			Reference:           cveInfo["reference"].(string),
			VulnerableArtifacts: yamlVulnArtifacts,
		}

		yamlVulns = append(yamlVulns, *vuln)

	}

	yamlReport := &SecVulnYamlReport{
		Vulnerabilities: yamlVulns,
	}

	return yamlReport

}

func exportReport(yamlReport SecVulnYamlReport) {
	// Export the yaml report to the provided output directory
	file, err := os.Create(secvulnOssindexOutput)
	if err != nil {
		fmt.Printf("Unable to create the security vulnerability report, %v\n", err)
		panic(err)
	}

	yamlWriter := io.Writer(file)

	enc := yaml.NewEncoder(yamlWriter)
	err = enc.Encode(yamlReport)
	if err != nil {
		fmt.Printf("Unable to encode the security vulnerability report, %v\n", err)
		panic(err)
	}

	fmt.Printf("%v Galasa projects processed\n%v vulnerabilities found\nExported security vulnerability report to %s\n", processedProjectCount, len(yamlReport.Vulnerabilities), secvulnOssindexOutput)
}

func getPom(directory string) Pom {
	pomFile, err := os.ReadFile(fmt.Sprintf("%s/%s", directory, "pom.xml"))
	if err != nil {
		fmt.Printf("Unable to read the pom for directory %s, %v\n", directory, err)
	}

	var pom Pom
	err = xml.Unmarshal(pomFile, &pom)
	if err != nil {
		fmt.Printf("Unable to unmarshal the pom for directory %s, %v\n", directory, err)
	}

	return pom
}

func getGalasaArtifactString(directory string) string {
	pom := getPom(directory)

	group := pom.GroupId
	artifact := pom.ArtifactId
	packaging := pom.Packaging
	version := pom.Version

	galasaArtifactString := fmt.Sprintf("%s:%s:%s:%s", group, artifact, packaging, version)

	return galasaArtifactString
}

func getDigraph(directory string) string {
	digraphFile, err := os.ReadFile(fmt.Sprintf("%s/%s/%s", directory, "target", "deps.txt"))
	if err != nil {
		fmt.Printf("Unable to find the dependency chain digraph in %s, %v\n", directory, err)
	}

	digraph := string(digraphFile)

	return digraph
}

func removeDuplicates(startArray []string) []string {
	var resultArray []string
	for _, entry := range startArray {
		if arrayContainsString(entry, resultArray) == false {
			resultArray = append(resultArray, entry)
		}
	}
	return resultArray
}
