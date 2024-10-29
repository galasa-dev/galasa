/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd


// Structure of galasabld ossindex output
type SecVulnYamlReport struct {
	Vulnerabilities []Vulnerability `yaml:"cves"`
}

type Vulnerability struct {
	Cve                 string               `yaml:"cve"`
	CvssScore           float64              `yaml:"cvssScore"`
	Reference           string               `yaml:"reference"`
	VulnerableArtifacts []VulnerableArtifact `yaml:"vulnerableArtifacts"`
}

type VulnerableArtifact struct {
	VulnerableArtifact string          `yaml:"vulnerableArtifact"`
	DirectProjects     []DirectProject `yaml:"directlyAffectedProjects"`
}

type DirectProject struct {
	ProjectName       string             `yaml:"name"`
	DependencyChain   string             `yaml:"dependencyChain"`
	TransientProjects []TransientProject `yaml:"indirectlyAffectedProjects,omitempty"`
}

type TransientProject struct {
	ProjectName     string `yaml:"name"`
	DependencyChain string `yaml:"dependencyChain,omitempty"`
}

// Structure of acceptance report (written by approvers)
type AcceptanceYamlReport struct {
	Cves []AcceptanceCve `yaml:"cves"`
}

type AcceptanceCve struct {
	Cve        string `yaml:"cve"`
	Comment    string `yaml:"comment"`
	ReviewDate string `yaml:"reviewDate"`
}
