/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

// Section 1
type MdCveStruct struct {
	Cve                 string
	CvssScore           float64
	Severity            string
	Link                string
	Comment             string
	ReviewDate          string
	VulnerableArtifacts []MdVulnArtifact
}

type MdVulnArtifact struct {
	VulnName string
	Projects []MdProject
}

type MdProject struct {
	Name            string
	DependencyChain []string
}

// Section 2
type MdProjectStruct struct {
	Name       string
	Dependents []string
	Cves       []MdCve
}

type MdCve struct {
	Cve             string
	CvssScore       float64
	Severity        string
	VulnArtifacts []MdCveVuln
}

type MdCveVuln struct {
	Artifact string
	DependencyChain []string
}

// For Summary section of Markdown
type CveSummary struct {
	Cve      string
	Link     string
	Severity string
	Amount   int
}

type ProjSummary struct {
	Project    string
	High       int
	Other      int
	Dependents int
}

// Whole report
type MarkdownReport struct {
	CveSummary []CveSummary
	CveStructs []MdCveStruct
	ProjectSummary []ProjSummary
	ProjectStructs []MdProjectStruct
}