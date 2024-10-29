/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	"bytes"
	"io"
	"io/fs"
	"log"
	"math"
	"math/rand"
	"os"
	"path/filepath"
	"strconv"
	"strings"
)

// ------------------------------------------------------------------------------------
// The implementation of the file system interface built on an in-memory map.
// ------------------------------------------------------------------------------------
type Node struct {
	content []byte
	isDir   bool
}

type MockFileSystem struct {
	// Where the in-memory data is kept.
	data map[string]*Node

	// A source of random numbers. So things are reproduceable.
	random *rand.Rand

	// Collects warnings messages
	warningMessageBuffer *bytes.Buffer

	executableExtension string

	filePathSeparator string

	fileReadCloser *MockFile

	// The mock struct contains methods which can be over-ridden on a per-test basis.
	VirtualFunction_MkdirAll             func(targetFolderPath string) error
	VirtualFunction_WriteTextFile        func(targetFilePath string, desiredContents string) error
	VirtualFunction_ReadTextFile         func(filePath string) (string, error)
	VirtualFunction_Exists               func(path string) (bool, error)
	VirtualFunction_DirExists            func(path string) (bool, error)
	VirtualFunction_GetUserHomeDir       func() (string, error)
	VirtualFunction_WriteBinaryFile      func(targetFilePath string, desiredContents []byte) error
	VirtualFunction_OutputWarningMessage func(string) error
	VirtualFunction_MkTempDir            func() (string, error)
	VirtualFunction_DeleteDir            func(path string)
	VirtualFunction_ReadDir              func(path string) ([]os.DirEntry, error)
	VirtualFunction_Open                 func(fileName string) (io.ReadCloser, error)
	VirtualFunction_WalkDir              func(root string, walkDirFunc fs.WalkDirFunc) error
}

// NewMockFileSystem creates an implementation of the thin file system layer which delegates
// to a memory map. This uses the default behaviour for all the virtual functions in our
// MockFileSystem
func NewMockFileSystem() FileSystem {
	mockFileSystem := NewOverridableMockFileSystem()
	return mockFileSystem
}

// NewOverridableMockFileSystem creates an implementation of the thin file system layer which
// delegates to a memory map, but because the MockFileSystem is returned (rather than a FileSystem)
// it means the caller can set up different virtual functions, to change the behaviour.
func NewOverridableMockFileSystem() *MockFileSystem {

	// Allocate the structure
	mockFileSystem := MockFileSystem{
		data: make(map[string]*Node)}

	mockFileSystem.warningMessageBuffer = &bytes.Buffer{}

	mockFileSystem.executableExtension = ""

	mockFileSystem.filePathSeparator = "/"

	mockFileSystem.fileReadCloser = NewOverridableMockFile()

	// Set up functions inside the structure to call the basic/default mock versions...
	// These can later be over-ridden on a test-by-test basis.
	mockFileSystem.VirtualFunction_MkdirAll = func(targetFolderPath string) error {
		return mockFSMkdirAll(mockFileSystem, targetFolderPath)
	}
	mockFileSystem.VirtualFunction_WriteTextFile = func(targetFilePath string, desiredContents string) error {
		return mockFSWriteTextFile(mockFileSystem, targetFilePath, desiredContents)
	}
	mockFileSystem.VirtualFunction_ReadTextFile = func(filePath string) (string, error) {
		return mockFSReadTextFile(mockFileSystem, filePath)
	}
	mockFileSystem.VirtualFunction_Exists = func(path string) (bool, error) {
		return mockFSExists(mockFileSystem, path)
	}
	mockFileSystem.VirtualFunction_DirExists = func(path string) (bool, error) {
		return mockFSDirExists(mockFileSystem, path)
	}
	mockFileSystem.VirtualFunction_GetUserHomeDir = func() (string, error) {
		return mockFSGetUserHomeDir()
	}
	mockFileSystem.VirtualFunction_WriteBinaryFile = func(path string, content []byte) error {
		return mockFSWriteBinaryFile(mockFileSystem, path, content)
	}
	mockFileSystem.VirtualFunction_OutputWarningMessage = func(message string) error {
		return mockFSOutputWarningMessage(mockFileSystem, message)
	}

	mockFileSystem.VirtualFunction_MkTempDir = func() (string, error) {
		return mockFSMkTempDir(mockFileSystem)
	}

	mockFileSystem.VirtualFunction_DeleteDir = func(pathToDelete string) {
		mockFSDeleteDir(mockFileSystem, pathToDelete)
	}

	mockFileSystem.VirtualFunction_ReadDir = func(path string) ([]os.DirEntry, error) {
		return mockFSReadDir(mockFileSystem, path)
	}
	mockFileSystem.VirtualFunction_Open = func(path string) (io.ReadCloser, error) {
		return mockFSOpenFile(mockFileSystem, path)
	}
	mockFileSystem.VirtualFunction_WalkDir = func(root string, walkDirFunc fs.WalkDirFunc) error {
		return mockFSWalkDir(mockFileSystem, root, walkDirFunc)
	}

	randomSource := rand.NewSource(13)
	mockFileSystem.random = rand.New(randomSource)

	return &mockFileSystem
}

func (fs *MockFileSystem) SetFilePathSeparator(newSeparator string) {
	fs.filePathSeparator = newSeparator
}

func (fs *MockFileSystem) SetExecutableExtension(newExtension string) {
	fs.executableExtension = newExtension
}

//------------------------------------------------------------------------------------
// Interface methods...
//------------------------------------------------------------------------------------

func (fs *MockFileSystem) GetFilePathSeparator() string {
	return fs.filePathSeparator
}

func (fs *MockFileSystem) GetExecutableExtension() string {
	return fs.executableExtension
}

func (fs *MockFileSystem) DeleteDir(pathToDelete string) {
	// Call the virtual function.
	fs.VirtualFunction_DeleteDir(pathToDelete)
}

func (fs *MockFileSystem) MkTempDir() (string, error) {
	// Call the virtual function.
	return fs.VirtualFunction_MkTempDir()
}

func (fs *MockFileSystem) MkdirAll(targetFolderPath string) error {
	// Call the virtual function.
	return fs.VirtualFunction_MkdirAll(targetFolderPath)
}

func (fs *MockFileSystem) WriteBinaryFile(targetFilePath string, desiredContents []byte) error {
	return fs.VirtualFunction_WriteBinaryFile(targetFilePath, desiredContents)
}

// WriteTextFile writes a string to a text file
func (fs *MockFileSystem) WriteTextFile(targetFilePath string, desiredContents string) error {
	// Call the virtual function.
	return fs.VirtualFunction_WriteTextFile(targetFilePath, desiredContents)
}

func (fs *MockFileSystem) ReadTextFile(filePath string) (string, error) {
	// Call the virtual function.
	return fs.VirtualFunction_ReadTextFile(filePath)
}

func (fs *MockFileSystem) Exists(path string) (bool, error) {
	// Call the virtual function.
	return fs.VirtualFunction_Exists(path)
}

func (fs *MockFileSystem) DirExists(path string) (bool, error) {
	// Call the virtual function.
	return fs.VirtualFunction_DirExists(path)
}

func (fs *MockFileSystem) GetUserHomeDir() (string, error) {
	return fs.VirtualFunction_GetUserHomeDir()
}

func (fs MockFileSystem) OutputWarningMessage(message string) error {
	return fs.VirtualFunction_OutputWarningMessage(message)
}

func (fs *MockFileSystem) ReadDir(dirPath string) ([]os.DirEntry, error) {
	return fs.VirtualFunction_ReadDir(dirPath)
}

func (fs *MockFileSystem) Open(fileName string) (io.ReadCloser, error) {
	return fs.VirtualFunction_Open(fileName)
}

func (fs *MockFileSystem) WalkDir(root string, walkDirFunc fs.WalkDirFunc) error {
	return fs.VirtualFunction_WalkDir(root, walkDirFunc)
}

// ------------------------------------------------------------------------------------
// Default implementations of the methods...
// ------------------------------------------------------------------------------------
func mockFSDeleteDir(fs MockFileSystem, pathToDelete string) {

	// Figure out which entries we are going to delete.
	var keysToRemove []string = make([]string, 0)
	for key := range fs.data {
		if strings.HasPrefix(key, pathToDelete) {
			keysToRemove = append(keysToRemove, key)
		}
	}

	// Delete the entries we want to
	for _, keyToRemove := range keysToRemove {
		delete(fs.data, keyToRemove)
	}
}

func mockFSMkTempDir(fs MockFileSystem) (string, error) {
	tempFolderPath := "/tmp" + strconv.Itoa(fs.random.Intn(math.MaxInt))
	err := fs.MkdirAll(tempFolderPath)
	return tempFolderPath, err
}

func mockFSMkdirAll(fs MockFileSystem, targetFolderPath string) error {

	nodeToAdd := Node{content: []byte(""), isDir: true}

	for {
		if targetFolderPath == "" {
			break
		}
		fs.data[targetFolderPath] = &nodeToAdd
		index := strings.LastIndex(targetFolderPath, "/")
		if index != -1 {
			targetFolderPath = targetFolderPath[:index]
		} else {
			break
		}
	}

	return nil
}

func mockFSWriteBinaryFile(fs MockFileSystem, targetFilePath string, desiredContents []byte) error {
	nodeToAdd := Node{content: desiredContents, isDir: false}
	fs.data[targetFilePath] = &nodeToAdd
	return nil
}

func mockFSWriteTextFile(fs MockFileSystem, targetFilePath string, desiredContents string) error {
	nodeToAdd := Node{content: []byte(desiredContents), isDir: false}
	fs.data[targetFilePath] = &nodeToAdd
	return nil
}

func mockFSReadTextFile(fs MockFileSystem, filePath string) (string, error) {
	text := ""
	var err error = nil
	node := fs.data[filePath]
	if node == nil {
		err = os.ErrNotExist
	} else {
		text = string(node.content)
	}
	return text, err
}

func mockFSExists(fs MockFileSystem, path string) (bool, error) {
	isExists := true
	var err error = nil
	node := fs.data[path]
	if node == nil {
		isExists = false
	}
	return isExists, err
}

func mockFSDirExists(fs MockFileSystem, path string) (bool, error) {
	isDirExists := true
	var err error = nil
	node := fs.data[path]
	if node == nil {
		isDirExists = false
	} else {
		isDirExists = node.isDir
	}
	return isDirExists, err
}

func mockFSGetUserHomeDir() (string, error) {
	return "/User/Home/testuser", nil
}

func mockFSOutputWarningMessage(fs MockFileSystem, message string) error {
	log.Printf("Mock warning message collected: %s", message)
	fs.warningMessageBuffer.WriteString(message)
	return nil
}

func mockFSReadDir(fs MockFileSystem, dirPath string) ([]os.DirEntry, error) {
	var dirEntries []MockDirEntry
	for key := range fs.data {
		if strings.HasPrefix(key, dirPath) && key != dirPath {
			dirEntries = append(dirEntries, MockDirEntry{DirName: filepath.Base(key)})
		}
	}

	entriesToReturn := make([]os.DirEntry, len(dirEntries))
	for index, value := range dirEntries {
		entriesToReturn[index] = value
	}
	return entriesToReturn, nil
}

func mockFSOpenFile(fs MockFileSystem, filePath string) (io.ReadCloser, error) {
	fs.fileReadCloser.data = []byte("dummy data")
	return fs.fileReadCloser, nil
}

func mockFSWalkDir(fs MockFileSystem, dirPath string, walkDirFunc fs.WalkDirFunc) error {
	var err error = nil
	for path := range fs.data {
		if strings.HasPrefix(path, dirPath) {
			dirEntry := MockDirEntry{DirName: filepath.Base(path)}
			err = walkDirFunc(path, dirEntry, nil)
		}
	}
	return err
}

//------------------------------------------------------------------------------------
// Extra methods on the mock to allow unit tests to get data out of the mock object.
//------------------------------------------------------------------------------------

func (fs MockFileSystem) GetAllWarningMessages() string {
	messages := fs.warningMessageBuffer.String()
	log.Printf("Mock reading back previously collected warnings messages: %s", messages)
	return messages
}

func (fs *MockFileSystem) GetAllFilePaths(rootPath string) ([]string, error) {
	var collectedFilePaths []string
	var err error

	for path, node := range fs.data {
		if strings.HasPrefix(path, rootPath) {
			if node.isDir == false {
				// It's a file. Save it's path to return.
				collectedFilePaths = append(collectedFilePaths, path)
			}
		}
	}

	return collectedFilePaths, err
}
