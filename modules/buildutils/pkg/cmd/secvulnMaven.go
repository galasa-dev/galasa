/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"encoding/xml"
	"errors"
	"fmt"
	"io"
	"net/http"
	"os"
	"sort"
	"strings"

	"github.com/spf13/cobra"
)

var (
	secvulnMavenCmd = &cobra.Command{
		Use:   "maven",
		Short: "Generate psuedo maven project for security vulnerability scanning",
		Long:  "Generate psuedo maven project for security vulnerability scanning",
		Run:   secvulnMavenExecute,
	}

	secvulnMavenParentDir string
	secvulnMavenPomUrls   *[]string
	secvulnMavenPomRepos  *[]string
	secvulnGalasaBranch   string

	completedProjects []Dependency
	toDoProjects      []Dependency
)

func init() {
	secvulnMavenCmd.PersistentFlags().StringVar(&secvulnMavenParentDir, "parent", "", "Parent project directory")
	secvulnMavenPomUrls = secvulnMavenCmd.PersistentFlags().StringArray("pom", nil, "Component Pom URLs")
	secvulnMavenPomRepos = secvulnMavenCmd.PersistentFlags().StringArray("repo", nil, "Repos to look for Poms")
	secvulnMavenCmd.PersistentFlags().StringVar(&secvulnGalasaBranch, "galasabranch", "main", "Branch of Galasa to scan")

	secvulnMavenCmd.MarkPersistentFlagRequired("parent")
	secvulnMavenCmd.MarkPersistentFlagRequired("pom")
	secvulnMavenCmd.MarkPersistentFlagRequired("repo")

	secvulnCmd.AddCommand(secvulnMavenCmd)
}

func secvulnMavenExecute(cmd *cobra.Command, args []string) {
	fmt.Printf("Galasa Build - Security Vulnerability Maven - version %v\n", rootCmd.Version)

	for _, pom := range *secvulnMavenPomUrls {
		startScanningPom(pom)
	}

	updateParent()
	fmt.Printf("Security scanning project pom.xml created with %v modules\n", len(completedProjects))

	// settings.xml needed to store credentials to authenticate to OSS Index plugin
	createSettings()
}

func startScanningPom(mainPomUrl string) {

	fmt.Printf("Starting the scanning at main pom %v\n", mainPomUrl)

	mainPom, err := readPomFromUrl(mainPomUrl)
	if err != nil {
		fmt.Printf("Unable to find the main pom at %v\n", mainPomUrl)
		panic(err)
	}

	createPseudoMavenProject(mainPom)
	fmt.Printf("Pseudo maven project created for: %v\n", mainPom.ArtifactId)

	// Repeat the process for all projects in this dependency chain if the groupId is dev.galasa
	fmt.Printf("Creating pseudo maven projects for all dependencies of %v:\n", mainPom.ArtifactId)

	for _, project := range toDoProjects {

		// If this project has already been processed then continue
		if checkIfCompleted(project) {
			continue
		}

		// Get the current pom for this project from the repo to use to create the stripped down pom for the pseudo maven project
		currentPom, err := readPomFromRepos(project.ArtifactId, project.GroupId, project.Version)
		if err != nil {
			fmt.Printf("Could not find pom for artifact %s\n", project)
			panic(err)
		}

		createPseudoMavenProject(currentPom)

		fmt.Printf("- %s\n", project.ArtifactId)

	}

}

func readPomFromUrl(url string) (Pom, error) {

	var pom Pom

	response, err := http.Get(url)
	if err != nil {
		return pom, err
	}

	defer response.Body.Close()

	body, err := io.ReadAll(response.Body)
	if err != nil {
		return pom, err
	}

	err = xml.Unmarshal(body, &pom)
	if err != nil {
		return pom, err
	}

	return pom, nil

}

func readPomFromRepos(artifactName, groupId, version string) (Pom, error) {

	var pom Pom

	var url string
	// Iterate through the provided repos with the --repo tag
	for _, repo := range *secvulnMavenPomRepos {
		// Use the groupId, artifactName and version to build up the url of the pom
		group := strings.Replace(groupId, ".", "/", -1)

		url = fmt.Sprintf("%s/%s/%s/%s/%s-%s.pom", repo, group, artifactName, version, artifactName, version)

		pom, _ = readPomFromUrl(url)
		if pom.ArtifactId == artifactName {
			return pom, nil
		}
	}

	return pom, errors.New("Pom not found in any of the provided repos")

}

func createPseudoMavenProject(pom Pom) {

	createDirectory(pom.ArtifactId)

	// Using the current pom from the repo, create a stripped down pom for the pseudo maven project
	createPom(pom)

	// Add this project to the list of completed projects so we don't reprocess and duplicate
	var completedProject = &Dependency{
		GroupId:    pom.GroupId,
		ArtifactId: pom.ArtifactId,
		Version:    pom.Version,
	}
	completedProjects = append(completedProjects, *completedProject)
}

func createDirectory(artifactName string) {
	_, err := os.Stat(fmt.Sprintf("%s/%s", secvulnMavenParentDir, artifactName))
	// If the sub-directory does not exist already, attempt to make it
	if os.IsNotExist(err) {
		if err := os.Mkdir(fmt.Sprintf("%s/%s", secvulnMavenParentDir, artifactName), os.ModePerm); err != nil {
			fmt.Printf("Unable to create directory for artifact %s - %v\n", artifactName, err)
			panic(err)
		}
	}
}

func createPom(pom Pom) {
	newPom := &Pom{}

	newPom.ModelVersion = pom.ModelVersion
	if pom.GroupId == "" {
		newPom.GroupId = "dev.galasa"
	} else {
		newPom.GroupId = pom.GroupId
	}
	newPom.ArtifactId = pom.ArtifactId
	newPom.Version = pom.Version
	newPom.Packaging = "jar"

	newPom.Parent = &Parent{
		GroupId:    "dev.galasa",
		ArtifactId: "security-scanning",
		Version:    "0.0.1",
	}

	var dependencies []Dependency

	for _, dep := range pom.Dependencies.Dependencies {
		groupId := dep.GroupId
		artifactId := dep.ArtifactId
		version := dep.Version
		dependency := &Dependency{
			GroupId:    groupId,
			ArtifactId: artifactId,
			Version:    version,
		}
		dependencies = append(dependencies, *dependency)

		// If a pseudo maven project hasn't been made for this dependency, and it is dev.galasa, add to the to do list for processing
		if checkIfCompleted(*dependency) == false && groupId == "dev.galasa" {
			var toDoProject = &Dependency{
				GroupId:    groupId,
				ArtifactId: artifactId,
				Version:    version,
			}
			toDoProjects = append(toDoProjects, *toDoProject)
		}
	}

	if len(dependencies) > 0 {
		newPom.Dependencies = &Dependencies{
			Dependencies: dependencies,
		}
	}

	filename := fmt.Sprintf("%s/%s/%s", secvulnMavenParentDir, pom.ArtifactId, "pom.xml")
	file, err := os.Create(filename)
	if err != nil {
		fmt.Printf("Unable to create pom.xml for artifact %s\n", pom.ArtifactId)
		panic(err)
	}

	xmlWriter := io.Writer(file)

	enc := xml.NewEncoder(xmlWriter)
	enc.Indent("  ", "    ")
	err = enc.Encode(newPom)
	if err != nil {
		fmt.Printf("Unable to encode the pom.xml for artifact %s\n", pom.ArtifactId)
		panic(err)
	}
}

func updateParent() {
	securityScanningPom := &Pom{}

	securityScanningPom.ModelVersion = "4.0.0"
	securityScanningPom.GroupId = "dev.galasa"
	securityScanningPom.ArtifactId = "security-scanning"
	securityScanningPom.Version = "0.0.1"
	securityScanningPom.Packaging = "pom"

	var array []string
	for _, project := range completedProjects {
		array = append(array, project.ArtifactId)
	}
	sort.Strings(array)

	securityScanningPom.Modules = &Modules{
		Module: array,
	}

	// Add Plugins
	securityScanningPom.Build = &Build{}

	var pluginArray []Plugin

	// OSS Index Maven plugin
	configuration := &Configuration{
		AuthId:     "ossindex-auth",
		ReportFile: "${project.build.directory}/audit-report.json",
		Fail:       "false",
	}
	ossindexPlugin := makePlugin(*configuration, "org.sonatype.ossindex.maven", "ossindex-maven-plugin", "3.1.0", "audit-dependencies", "validate", "audit")
	pluginArray = append(pluginArray, ossindexPlugin)

	// Dependency:tree plugin
	configuration2 := &Configuration{
		OutputType: "dot",
		OutputFile: "${project.build.directory}/deps.txt",
	}
	deptreePlugin := makePlugin(*configuration2, "org.apache.maven.plugins", "maven-dependency-plugin", "3.3.0", "dependency-tree", "validate", "tree")
	pluginArray = append(pluginArray, deptreePlugin)

	securityScanningPom.Build.Plugins.Plugins = pluginArray

	// Add Galasa repo to pom for OSS Index plugin to search in
	var repositories []Repository
	repo := &Repository{
		Id:  "galasa.repo",
		Url: fmt.Sprintf("https://development.galasa.dev/%s/maven-repo/obr", secvulnGalasaBranch), // New maven repo on external cluster
	}
	repositories = append(repositories, *repo)

	securityScanningPom.Repositories = &Repositories{
		Repositories: repositories,
	}

	filename := fmt.Sprintf("%s/%s", secvulnMavenParentDir, "pom.xml")
	file, err := os.Create(filename)
	if err != nil {
		fmt.Printf("Unable to create pom.xml for security scanning project\n")
		panic(err)
	}

	xmlWriter := io.Writer(file)

	enc := xml.NewEncoder(xmlWriter)
	enc.Indent("  ", "    ")
	err = enc.Encode(securityScanningPom)
	if err != nil {
		fmt.Printf("Unable to encode the pom.xml for security scanning project\n")
		panic(err)
	}

}

func makePlugin(configuration Configuration, group, artifact, version, id, phase, goal string) Plugin {
	goals := &Goals{
		Goal: goal,
	}
	execution := &Execution{
		Id:    id,
		Phase: phase,
		Goals: *goals,
	}
	executions := &Executions{
		Execution: *execution,
	}
	plugin := &Plugin{
		GroupId:       group,
		ArtifactId:    artifact,
		Version:       version,
		Executions:    *executions,
		Configuration: configuration,
	}

	return *plugin
}

func createSettings() {

	var serverArray []Server
	server := &Server{
		Id:       "ossindex-auth",
		Username: "${username}",
		Password: "${password}",
	}
	serverArray = append(serverArray, *server)

	servers := &Servers{
		Servers: serverArray,
	}

	settings := &Settings{
		Servers: *servers,
	}

	filename := fmt.Sprintf("%s/%s", secvulnMavenParentDir, "settings.xml")
	file, err := os.Create(filename)
	if err != nil {
		fmt.Printf("Unable to create settings.xml for security scanning project\n")
		panic(err)
	}

	xmlWriter := io.Writer(file)

	enc := xml.NewEncoder(xmlWriter)
	enc.Indent("  ", "    ")
	err = enc.Encode(settings)
	if err != nil {
		fmt.Printf("Unable to encode the settings.xml for security scanning project\n")
		panic(err)
	}
}

func checkIfCompleted(a Dependency) bool {
	for _, b := range completedProjects {
		if b == a {
			return true
		}
	}
	return false
}
