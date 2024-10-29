/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"fmt"
	"io"
	"io/fs"
	"log"
	"net/http"
	"net/url"
	"os"
	"path"
	"strings"

	"galasa.dev/buildUtilities/pkg/utils"
	"github.com/spf13/cobra"
)

var (
	mavenDeployCmd = &cobra.Command{
		Use:   "deploy",
		Short: "Deploy local maven repository to remote one",
		Long:  "Deploy local maven artifacts at the set version to a remote maven repository",
		Run:   executeMavenDeploy,
	}

	mavenDeployDirectory string
	mavenDeployGroup     string
	mavenDeployVersion   string
)

func init() {
	mavenDeployCmd.PersistentFlags().StringVarP(&mavenDeployDirectory, "local", "", "", "local repository")
	mavenDeployCmd.PersistentFlags().StringVarP(&mavenDeployGroup, "group", "", "", "groupId to deploy")
	mavenDeployCmd.PersistentFlags().StringVarP(&mavenDeployVersion, "version", "", "", "version to deploy")

	mavenDeployCmd.MarkPersistentFlagRequired("local")
	mavenDeployCmd.MarkPersistentFlagRequired("group")
	mavenDeployCmd.MarkPersistentFlagRequired("version")

	mavenCmd.AddCommand(mavenDeployCmd)
}

func executeMavenDeploy(cmd *cobra.Command, args []string) {
	var exitCode = 0

	fmt.Printf("executeMavenDeploy - Galasa Build - Maven Deploy - version %v\n", rootCmd.Version)

	basicAuth, err := mavenGetBasicAuth()
	if err != nil {
		exitCode = 1
		fmt.Println(err.Error())
	} else {
		fileSystem := utils.NewOSFileSystem()

		mavenRepositoryUrl = strings.TrimRight(mavenRepositoryUrl, "/")

		err = mavenDeploy(fileSystem, mavenRepositoryUrl, mavenDeployDirectory, mavenDeployGroup, mavenDeployVersion, basicAuth)
		if err != nil {
			exitCode = 1
			fmt.Println(err.Error())
		}
	}

	os.Exit(exitCode)
}

// Checks the given local repository for artifacts and deploys the identified artifacts to the remote Maven repository
func mavenDeploy(
	fileSystem utils.FileSystem,
	mavenRepositoryUrl string,
	mavenDeployDirectory string,
	mavenDeployGroup string,
	mavenDeployVersion string,
	basicAuth string) error {

	var err error
	var artifactDirectories []fs.DirEntry
	var mavenMetadataExists bool
	var versionDirectoryExists bool

	groupDir := strings.ReplaceAll(mavenDeployGroup, ".", string(os.PathSeparator))
	mavenBaseDirectory := path.Join(mavenDeployDirectory, groupDir)

	artifactDirectories, err = fileSystem.ReadDir(mavenBaseDirectory)
	if err == nil {

		// Create a map of artifacts. Keys correspond to artifact names and values correspond to the paths to
		// the artifacts' version directories
		artifacts := make(map[string]string)

		for _, potentialArtifact := range artifactDirectories {

			// Check if this is an artifact directory and not a subgroup
			artifactName := potentialArtifact.Name()
			artifactDirectory := path.Join(mavenBaseDirectory, artifactName)
			mavenMetadataFileName := "maven-metadata.xml"

			mavenMetadataExists, err = fileSystem.Exists(path.Join(artifactDirectory, mavenMetadataFileName))
			log.Printf("mavenDeploy - mavenMetadata '%v' exists is:%v", path.Join(artifactDirectory, mavenMetadataFileName), mavenMetadataExists)
			if !mavenMetadataExists {
				mavenMetadataPath := matchFileInDirectory(fileSystem, artifactDirectory, mavenMetadataFileName)

				// No maven-metadata.xml file found within artifact directory, move on to the next artifact
				if mavenMetadataPath == "" {
					log.Printf("mavenDeploy - mavenMetadataPath not found for artifact: %v", potentialArtifact.Name())
					continue
				}

			} else if err != nil {
				log.Printf("mavenDeploy - ERROR when checking if mavenMetadataExists - %v", err.Error())
				continue
			}

			// Check if this artifact is at the correct version
			artifactVersionPath := path.Join(artifactDirectory, mavenDeployVersion)
			versionDirectoryExists, err = fileSystem.DirExists(artifactVersionPath) //doens't return an error if not a dir
			log.Printf("mavenDeploy - artifactVersionPath '%v' versionDirectoryExists: %v", artifactVersionPath, versionDirectoryExists)
			if !versionDirectoryExists {
				artifactVersionPath = matchFileInDirectory(fileSystem, artifactDirectory, mavenDeployVersion)

				// No version directory found within the artifact directory, move on to the next artifact
				if artifactVersionPath == "" {
					log.Printf("mavenDeploy - artifactVersionPath not found for artifact: %v", potentialArtifact.Name())
					continue
				}

			} else if err != nil {
				log.Printf("mavenDeploy - ERROR when checking if versionDirectoryExists - %v", err.Error())
				continue
			}

			artifacts[artifactName] = artifactVersionPath
		}

		if len(artifacts) < 1 {
			fmt.Println("No artifacts found to deploy")
			return err
		}

		log.Printf("mavenDeploy - artifacts collected - %v", artifacts)

		// Now deploy the contents of the artifact version directories
		err = deployArtifacts(fileSystem, mavenRepositoryUrl, mavenDeployGroup, mavenDeployVersion, artifacts, basicAuth)

	}

	return err
}

// Deploys the given artifacts to a given Maven repository
func deployArtifacts(
	fileSystem utils.FileSystem,
	mavenRepository string,
	mavenDeployGroup string,
	mavenDeployVersion string,
	artifacts map[string]string,
	basicAuth string) error {

	var err error = nil
	var versionArtifacts []fs.DirEntry
	var joinedUrl string
	var file io.ReadCloser
	client := &http.Client{}
	groupDir := strings.ReplaceAll(mavenDeployGroup, ".", string(os.PathSeparator))

	for artifactName, artifactVersionPath := range artifacts {
		fmt.Printf("deployArtifacts - Deploying %v/%v/%v\n", mavenDeployGroup, artifactName, mavenDeployVersion)

		versionArtifacts, err = fileSystem.ReadDir(artifactVersionPath) //doesn't return err if dir doesn't exist
		log.Printf("deployArtifacts - current dir is '%s'", artifactVersionPath)
		if err == nil {

			// Go through each file within the artifact's version directory and send a PUT request to deploy to the
			// remote Maven repository
			for _, artifactFile := range versionArtifacts {
				fmt.Printf("Artifact File:    %v\n", artifactFile.Name())
				artifactFilePath := path.Join(artifactVersionPath, artifactFile.Name())
				artifactPathFromGroup := artifactFilePath[strings.Index(artifactFilePath, groupDir):]

				joinedUrl, err = url.JoinPath(mavenRepository, artifactPathFromGroup)
				if err == nil {
					file, err = fileSystem.Open(artifactFilePath)
					if err == nil {
						err = putMavenArtifact(joinedUrl, file, client, basicAuth)
						if err != nil {
							log.Println("deployArtifacts - unable to PUT request")
							return err
						}
					}
				}
			}
		}
	}

	if len(artifacts) == 1 {
		fmt.Printf("Complete - 1 artifact deployed\n")
	} else {
		fmt.Printf("Complete - %v artifacts deployed\n", len(artifacts))
	}

	return err
}

// Sends a PUT request to a Maven repository to upload an artifact to it
func putMavenArtifact(
	mavenRepoUrl string,
	readCloser io.ReadCloser,
	client *http.Client,
	basicAuth string) error {

	defer readCloser.Close()

	// Create the PUT request
	req, err := http.NewRequest("PUT", mavenRepoUrl, readCloser)
	if err == nil {
		req.Header.Set("Authorization", basicAuth)

		// Send the PUT request
		resp, err := client.Do(req)
		if err == nil {
			defer resp.Body.Close()

			if resp.StatusCode != http.StatusCreated {
				return fmt.Errorf("put for artifact for url %v - status line - %v", mavenRepoUrl, resp.Status)
			}
			log.Printf("putMavenArtifact - HTTP response body - %v", resp.Body)
		}
	}

	return err
}

// Walks through a given directory, searching for a given file or directory name.
// Returns the path to the matching file or directory, or an empty string if no match was found.
func matchFileInDirectory(fileSystem utils.FileSystem, dirPath string, targetFileName string) string {
	var matchedPath string = ""
	_ = fileSystem.WalkDir(dirPath, func(path string, file os.DirEntry, err error) error {
		if file.Name() == targetFileName {
			log.Printf("matchFileInDirectory - filename '%s' is the same as target", file.Name())
			matchedPath = path

			// Match found, no need to continue walking through the directory
			return fs.SkipDir
		}

		return nil
	})

	if matchedPath != "" {
		log.Printf("matchFileInDirectory - matchedPath: - %s", matchedPath)
	}
	return matchedPath
}
