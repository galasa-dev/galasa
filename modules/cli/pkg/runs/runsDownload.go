/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runs

import (
	"bufio"
	"context"
	"fmt"
	"io"
	"log"
	"net/http"
	"os"
	"path/filepath"
	"strconv"
	"strings"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/embedded"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/images"
	"github.com/galasa-dev/cli/pkg/spi"
)

// DownloadArtifacts - performs all the logic to implement the `galasactl runs download` command,
// but in a unit-testable manner.
func DownloadArtifacts(
	runName string,
	forceDownload bool,
	fileSystem spi.FileSystem,
	timeService spi.TimeService,
	console spi.Console,
	commsClient api.APICommsClient,
	runDownloadTargetFolder string,
) error {

	var err error
	var runs []galasaapi.Run

	if runName != "" {
		err = ValidateRunName(runName)
	}
	if err == nil {
		requestorParameter := ""
		resultParameter := ""
		group := ""
		fromAgeHours := 0
		toAgeHours := 0
		shouldGetActive := false
		runs, err = GetRunsFromRestApi(runName, requestorParameter, resultParameter, fromAgeHours, toAgeHours, shouldGetActive, timeService, commsClient, group)
		if err == nil {
			if len(runs) > 1 {
				// get list of runs that are reRuns - get list of runs that are reRuns of each other
				// create a map of lists of reRuns - key is queued time, value is the run
				reRunsByQueuedTime := createMapOfReRuns(runs)

				err = downloadReRunArtfifacts(
					reRunsByQueuedTime,
					forceDownload,
					fileSystem,
					commsClient,
					console,
					timeService,
					runDownloadTargetFolder,
				)

			} else if len(runs) == 1 {
				var folderName string
				folderName, err = nameDownloadFolder(runs[0], runName, timeService)
				if err == nil {
					err = downloadArtifactsAndRenderImagesToDirectory(commsClient, folderName, runs[0], fileSystem, forceDownload, console, runDownloadTargetFolder)
				}
			} else {
				log.Printf("No artifacts to download for run: '%s'\n", runName)
				err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_NO_ARTIFACTS_TO_DOWNLOAD, runName)
			}
		}
	}

	return err
}

func downloadReRunArtfifacts(
	reRunsByQueuedTime map[string][]galasaapi.Run,
	forceDownload bool,
	fileSystem spi.FileSystem,
	commsClient api.APICommsClient,
	console spi.Console,
	timeService spi.TimeService,
	runDownloadTargetFolder string,
) error {
	var err error
	for _, reRunsList := range reRunsByQueuedTime {
		if err == nil {
			for reRunIndex, reRun := range reRunsList {
				if err == nil {
					directoryName := nameReRunArtifactDownloadDirectory(reRun, reRunIndex, timeService)
					err = downloadArtifactsAndRenderImagesToDirectory(
						commsClient,
						directoryName,
						reRun,
						fileSystem,
						forceDownload,
						console,
						runDownloadTargetFolder,
					)
				}
			}
		}
	}
	return err
}

func createMapOfReRuns(runs []galasaapi.Run) map[string][]galasaapi.Run {
	// key = queued time
	// value = []runs
	// This function creates a map of queued times. Each time has a list of runs with that queued time.
	// helpful if we had two unrelated tests with the same runname, which both had a set of reruns.
	// e.g.
	// "2023-05-10T06:00:13.043037Z" = [reRun1, reRun2, reRun3]
	// "2023-01-10T16:10:13.043037Z" = [reRunA, reRunB, reRunC]
	reRunsByQueuedTime := make(map[string][]galasaapi.Run, 0)
	queuedTime := ""
	for _, run := range runs {
		queuedTime = run.TestStructure.GetQueued()
		runsWithSameQueuedTime := reRunsByQueuedTime[queuedTime]
		runsWithSameQueuedTime = append(runsWithSameQueuedTime, run)
		reRunsByQueuedTime[queuedTime] = runsWithSameQueuedTime
	}
	return reRunsByQueuedTime
}

func nameReRunArtifactDownloadDirectory(reRun galasaapi.Run, reRunIndex int, timeService spi.TimeService) string {
	result := reRun.TestStructure.GetResult()
	runName := reRun.TestStructure.GetRunName()
	var directoryName string
	if result == "" {
		// Add timestamp of download to folder name
		downloadedTime := timeService.Now().Format("2006-01-02_15:04:05")

		directoryName = runName + "-" + strconv.Itoa(reRunIndex+1) + "-" + downloadedTime
	} else {
		directoryName = runName + "-" + strconv.Itoa(reRunIndex+1)
	}
	return directoryName
}

func nameDownloadFolder(run galasaapi.Run, runName string, timeService spi.TimeService) (string, error) {
	directoryName := runName
	var err error
	result := run.TestStructure.GetResult()
	if result == "" {
		downloadedTime := timeService.Now().Format("2006-01-02_15:04:05")
		directoryName = runName + "-" + downloadedTime
	}
	return directoryName, err
}

func downloadArtifactsAndRenderImagesToDirectory(
	commsClient api.APICommsClient,
	directoryName string,
	run galasaapi.Run,
	fileSystem spi.FileSystem,
	forceDownload bool,
	console spi.Console,
	runDownloadTargetFolder string,
) error {
	var err error

	// We want to base the directory we download to on the destination folder.
	// If the destination folder is "." (current folder/relative)
	// then ignore, as prefixing with a "." just adds noise when we log and
	// print out the path.
	if runDownloadTargetFolder != "." {
		directoryName = filepath.Join(runDownloadTargetFolder, directoryName)
	}

	var filePathsCreated []string
	filePathsCreated, err = downloadArtifactsToDirectory(commsClient, directoryName, run, fileSystem, forceDownload, console)

	if err == nil {
		renderImages(fileSystem, filePathsCreated, forceDownload)
	}
	return err
}

func renderImages(fileSystem spi.FileSystem, filePathsCreated []string, forceOverwriteExistingFiles bool) error {
	var err error

	embeddedFileSystem := embedded.GetReadOnlyFileSystem()
	renderer := images.NewImageRenderer(embeddedFileSystem)
	expander := images.NewImageExpander(fileSystem, renderer, forceOverwriteExistingFiles)

	for _, filePath := range filePathsCreated {
		err = expander.ExpandImage(filePath)
		if err != nil {
			break
		}
	}

	if err == nil {
		// Write out a status string to the console about how many files were rendered.
		count := expander.GetExpandedImageFileCount()
		log.Printf("Expanded a total of %d image files.\n", count)
	}

	return err
}

func downloadArtifactsToDirectory(
	commsClient api.APICommsClient,
	directoryName string,
	run galasaapi.Run,
	fileSystem spi.FileSystem,
	forceDownload bool,
	console spi.Console,
) (filePathsCreated []string, err error) {

	runId := run.GetRunId()
	filePathsCreated = make([]string, 0)

	filesWrittenOkCount := 0

	artifactPaths, err := GetArtifactPathsFromRestApi(runId, commsClient)
	if err == nil {
		for _, artifactPath := range artifactPaths {
			if err == nil {
				var artifactData io.Reader
				var httpResponse *http.Response
				var isArtifactDataEmpty bool
				artifactData, isArtifactDataEmpty, httpResponse, err = GetFileFromRestApi(runId, strings.TrimPrefix(artifactPath, "/"), commsClient)
				if err == nil {
					if !isArtifactDataEmpty {

						targetFilePath := filepath.Join(directoryName, artifactPath)

						err = WriteArtifactToFileSystem(fileSystem, targetFilePath, artifactPath, artifactData, forceDownload, console)
						if err == nil {
							filesWrittenOkCount += 1
							filePathsCreated = append(filePathsCreated, targetFilePath)
						}
					}
				}

				closeErr := httpResponse.Body.Close()
				// The first error is most important so needs preserving...
				if closeErr != nil && err == nil {
					err = galasaErrors.NewGalasaErrorWithHttpStatusCode(httpResponse.StatusCode, galasaErrors.GALASA_ERROR_HTTP_RESPONSE_CLOSE_FAILED, closeErr.Error())
				}
			}
		}
	}

	// Write out the number of files downloaded to the folder xxx
	if filesWrittenOkCount > 0 {
		msg := fmt.Sprintf(
			galasaErrors.GALASA_INFO_FOLDER_DOWNLOADED_TO.Template,
			filesWrittenOkCount,
			directoryName,
		)
		consoleErr := console.WriteString(msg)
		// Console error is not as important to report as the original error if there was one.
		if consoleErr != nil && err == nil {
			err = consoleErr
		}
	}

	return filePathsCreated, err
}

// Retrieves the paths of all artifacts for a given test run using its runId.
func GetArtifactPathsFromRestApi(runId string, commsClient api.APICommsClient) ([]string, error) {

	var err error
	var artifactPaths []string
	log.Println("Retrieving artifact paths for the given run")

	var restApiVersion string
	restApiVersion, err = embedded.GetGalasactlRestApiVersion()

	if err == nil {
		err = commsClient.RunAuthenticatedCommandWithRateLimitRetries(func(apiClient *galasaapi.APIClient) error {
			var err error
			var httpResponse *http.Response
			var artifactsList []galasaapi.ArtifactIndexEntry
	
			artifactsList, httpResponse, err = apiClient.ResultArchiveStoreAPIApi.
				GetRasRunArtifactList(context.Background(), runId).
				ClientApiVersion(restApiVersion).
				Execute()
	
			var statusCode int
			if httpResponse != nil {
				defer httpResponse.Body.Close()
				statusCode = httpResponse.StatusCode
			}
	
			if err != nil {
				err = galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_RETRIEVING_ARTIFACTS_FAILED, err.Error())
			} else {
				for _, artifact := range artifactsList {
					artifactPaths = append(artifactPaths, artifact.GetPath())
				}
			}
			log.Printf("%v artifact path(s) found\n", len(artifactPaths))
			return err
		})
	}

	return artifactPaths, err
}

// Writes an artifact to the host's file system, creating a new directory for the run's artifacts
// to be written to. Existing files are only overwritten if the --force option is used as part of
// the "runs download" command.
func WriteArtifactToFileSystem(
	fileSystem spi.FileSystem,
	targetFilePath string,
	artifactPath string,
	fileDownloaded io.Reader,
	shouldOverwrite bool,
	console spi.Console) error {

	var err error

	pathParts := strings.Split(artifactPath, "/")
	fileName := pathParts[len(pathParts)-1]

	// Check if a new file should be created or if an existing one should be overwritten.
	var fileExists bool
	fileExists, err = fileSystem.Exists(targetFilePath)
	if err == nil {
		if fileExists && !shouldOverwrite {

			// The --force flag was not provided, throw an error.
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_CANNOT_OVERWRITE_FILE, targetFilePath)
		} else {
			// Set up the directory structure and artifact file on the host's file system
			var newFile io.WriteCloser
			newFile, err = CreateEmptyArtifactFile(fileSystem, targetFilePath)
			if err == nil {
				defer newFile.Close()

				log.Printf("Writing artifact '%s' to '%s' on local file system\n", fileName, targetFilePath)

				err = TransferContent(fileDownloaded, newFile, targetFilePath)
				if err == nil {
					log.Printf("Artifact '%s' written to '%s' OK\n", fileName, targetFilePath)
				} else {
					log.Printf("Artifact '%s' write to '%s' failed. '%s'\n", fileName, targetFilePath, err.Error())
				}
			}
		}
	}
	return err
}

// Writes the contents of a given source file into a given target file using a buffer
// to read and write the contents in chunks.
func TransferContent(sourceFile io.Reader, targetFile io.WriteCloser, targetFilePath string) error {

	log.Printf("TransferContent: Entered. targetFilePath: %s\n", targetFilePath)

	var err error

	// Set buffer capacity to 1KB
	bufferCapacity := 1024

	// Set up a byte buffer to gradually read the downloaded file to avoid out-of-memory issues.
	reader := bufio.NewReader(sourceFile)
	buffer := make([]byte, bufferCapacity)
	for {
		var bytesRead int
		bytesRead, err = reader.Read(buffer)
		if err != nil {
			log.Printf("TransferContent: Read(...) returned %s\n", err.Error())

			// Suppress end of file error, as we want to read until the end of the file.
			if err == io.EOF {
				// There was nothing else to read.
				err = nil
			}
			break
		}

		log.Printf("TransferContent: Read(...) returned %d bytes of data\n", bytesRead)

		_, err = targetFile.Write(buffer[:bytesRead])
		if err != nil {
			log.Printf("TransferContent: Write() failed. err=%s\n", err.Error())
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_FAILED_TO_WRITE_FILE, targetFilePath, err.Error())
			break
		}
	}

	if err != nil {
		log.Printf("TransferContent problem. File: %s. err: %s\n", targetFilePath, err.Error())
	}

	return err
}

// Creates an empty file representing an artifact that is being written. Any parent directories that do not exist
// will be created. Returns the empty file that was created or an error if any file creation operations failed.
func CreateEmptyArtifactFile(fileSystem spi.FileSystem, targetFilePath string) (io.WriteCloser, error) {

	var err error
	var newFile io.WriteCloser = nil

	targetDirectoryPath := filepath.Dir(targetFilePath)
	err = fileSystem.MkdirAll(targetDirectoryPath)
	if err != nil {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_FAILED_TO_CREATE_FOLDERS, targetDirectoryPath, err.Error())
	} else {
		newFile, err = fileSystem.Create(targetFilePath)
		if err != nil {
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_FAILED_TO_WRITE_FILE, targetFilePath, err.Error())
		}
	}
	return newFile, err
}

// GetFileFromRestApi Retrieves an artifact for a given test run using its runId from the ecosystem API.
// Note: The call leaves closing the http request as a responsibility of the caller.
func GetFileFromRestApi(runId string, artifactPath string, commsClient api.APICommsClient) (io.Reader, bool, *http.Response, error) {

	var err error
	isFileEmpty := false
	var httpResponse *http.Response
	var fileDownloaded *os.File
	log.Printf("Downloading artifact '%s' from API server\n", artifactPath)

	var restApiVersion string
	restApiVersion, err = embedded.GetGalasactlRestApiVersion()

	if err == nil {

		err = commsClient.RunAuthenticatedCommandWithRateLimitRetries(func(apiClient *galasaapi.APIClient) error {
			var err error
			fileDownloaded, httpResponse, err = apiClient.ResultArchiveStoreAPIApi.
				GetRasRunArtifactByPath(context.Background(), runId, artifactPath).
				ClientApiVersion(restApiVersion).
				Execute()
	
			var statusCode int
			if httpResponse != nil {
				statusCode = httpResponse.StatusCode
			}
	
			if err != nil {
				downloadErr := galasaErrors.NewGalasaErrorWithHttpStatusCode(statusCode, galasaErrors.GALASA_ERROR_DOWNLOADING_ARTIFACT_FAILED, artifactPath, err.Error())
				err = downloadErr

				// Close the response body if we're going to retry the download to avoid leaving responses open
				if httpResponse != nil && (downloadErr.IsReauthRequired() || downloadErr.IsRateLimitedRetryRequired()) {
					defer httpResponse.Body.Close()
				}

				log.Printf("Failed to download artifact. %s\n", err.Error())
			} else {
				log.Printf("Artifact '%s' http response from API server OK\n", artifactPath)
	
				if fileDownloaded == nil {
					log.Printf("Artifact '%s' http response returned nil file content.\n", artifactPath)
					isFileEmpty = true
				}
			}
			return err
		})
	}

	return fileDownloaded, isFileEmpty, httpResponse, err
}
