/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package files

import (
	"bytes"
	"io"
	"log"
	"math"
	"math/rand"
	"os"
	"strconv"
	"strings"
	"sync"

	"github.com/galasa-dev/cli/pkg/spi"
)

// ------------------------------------------------------------------------------------
// The implementation of the file system interface built on an in-memory map.
// ------------------------------------------------------------------------------------
type Node struct {
	content []byte
	isDir   bool
}

type MockFileSystem struct {
	// Used so we can keep the logic inside thread-safe.
	mutexLock *sync.Mutex

	// Where the in-memory data is kept.
	data map[string]*Node

	// A source of random numbers. So things are reproduceable.
	random *rand.Rand

	// Collects warnings messages
	warningMessageBuffer *bytes.Buffer

	executableExtension string

	filePathSeparator string

	// The mock struct contains methods which can be over-ridden on a per-test basis.
	// The New
	VirtualFunction_MkdirAll             func(targetFolderPath string) error
	VirtualFunction_WriteTextFile        func(targetFilePath string, desiredContents string) error
	VirtualFunction_ReadBinaryFile       func(filePath string) ([]byte, error)
	VirtualFunction_ReadTextFile         func(filePath string) (string, error)
	VirtualFunction_Exists               func(path string) (bool, error)
	VirtualFunction_DirExists            func(path string) (bool, error)
	VirtualFunction_GetUserHomeDirPath   func() (string, error)
	VirtualFunction_WriteBinaryFile      func(targetFilePath string, desiredContents []byte) error
	VirtualFunction_OutputWarningMessage func(string) error
	VirtualFunction_MkTempDir            func() (string, error)
	VirtualFunction_DeleteDir            func(path string)
	VirtualFunction_DeleteFile           func(path string)
	VirtualFunction_Create               func(path string) (io.WriteCloser, error)
}

// NewMockFileSystem creates an implementation of the thin file system layer which delegates
// to a memory map. This uses the default behaviour for all the virtual functions in our
// MockFileSystem
func NewMockFileSystem() spi.FileSystem {
	mockFileSystem := NewOverridableMockFileSystem()
	return mockFileSystem
}

// NewOverridableMockFileSystem creates an implementation of the thin file system layer which delegates
// to delegates to a memory map, but because the MockFileSystem is returned (rather than a FileSystem)
// it means the caller can set up different virtual functions, to change the behaviour.
func NewOverridableMockFileSystem() *MockFileSystem {

	// Allocate the structure
	mockFileSystem := MockFileSystem{
		data: make(map[string]*Node)}

	mockFileSystem.warningMessageBuffer = &bytes.Buffer{}

	mockFileSystem.executableExtension = ""

	mockFileSystem.filePathSeparator = "/"

	mockFileSystem.mutexLock = &sync.Mutex{}

	// Set up functions inside the structure to call the basic/default mock versions...
	// These can later be over-ridden on a test-by-test basis.

	mockFileSystem.VirtualFunction_Create = func(path string) (io.WriteCloser, error) {
		return mockFSCreate(mockFileSystem, path)
	}

	mockFileSystem.VirtualFunction_MkdirAll = func(targetFolderPath string) error {
		return mockFSMkdirAll(mockFileSystem, targetFolderPath)
	}
	mockFileSystem.VirtualFunction_WriteTextFile = func(targetFilePath string, desiredContents string) error {
		return mockFSWriteTextFile(mockFileSystem, targetFilePath, desiredContents)
	}
	mockFileSystem.VirtualFunction_ReadBinaryFile = func(filePath string) ([]byte, error) {
		return mockFSReadBinaryFile(mockFileSystem, filePath)
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
	mockFileSystem.VirtualFunction_GetUserHomeDirPath = func() (string, error) {
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

	mockFileSystem.VirtualFunction_DeleteFile = func(pathToDelete string) {
		mockFSDeleteFile(mockFileSystem, pathToDelete)
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

func (fs *MockFileSystem) Create(path string) (io.WriteCloser, error) {
	// log.Printf("Create entered")
	// defer log.Printf("Create exited")
	return fs.VirtualFunction_Create(path)
}

func (fs *MockFileSystem) GetFilePathSeparator() string {
	return fs.filePathSeparator
}

func (fs *MockFileSystem) GetExecutableExtension() string {
	return fs.executableExtension
}

func (fs *MockFileSystem) DeleteDir(pathToDelete string) {
	// Call the virtual function.
	// log.Printf("DeleteDir entered")
	// defer log.Printf("DeleteDir exited")
	fs.mutexLock.Lock()
	defer fs.mutexLock.Unlock()
	fs.VirtualFunction_DeleteDir(pathToDelete)
}

func (fs *MockFileSystem) DeleteFile(pathToDelete string) {
	log.Printf("DeleteFile entered")
	defer log.Printf("DeleteFile exited")
	fs.mutexLock.Lock()
	defer fs.mutexLock.Unlock()
	// Call the virtual function.
	fs.VirtualFunction_DeleteFile(pathToDelete)
}

func (fs *MockFileSystem) MkTempDir() (string, error) {
	// log.Printf("MkTempDir entered")
	// defer log.Printf("MkTempDir exited")
	// Call the virtual function.
	return fs.VirtualFunction_MkTempDir()
}

func (fs *MockFileSystem) MkdirAll(targetFolderPath string) error {
	// log.Printf("MkdirAll entered")
	// defer log.Printf("MkdirAll exited")
	fs.mutexLock.Lock()
	defer fs.mutexLock.Unlock()
	// Call the virtual function.
	return fs.VirtualFunction_MkdirAll(targetFolderPath)
}

func (fs *MockFileSystem) WriteBinaryFile(targetFilePath string, desiredContents []byte) error {
	log.Printf("WriteBinaryFile entered")
	defer log.Printf("WriteBinaryFile exited")
	fs.mutexLock.Lock()
	defer fs.mutexLock.Unlock()
	return fs.VirtualFunction_WriteBinaryFile(targetFilePath, desiredContents)
}

// WriteTextFile writes a string to a text file
func (fs *MockFileSystem) WriteTextFile(targetFilePath string, desiredContents string) error {
	// log.Printf("WriteTextFile entered")
	// defer log.Printf("WriteTextFile exited")
	// Call the virtual function.
	fs.mutexLock.Lock()
	defer fs.mutexLock.Unlock()
	return fs.VirtualFunction_WriteTextFile(targetFilePath, desiredContents)
}

func (fs *MockFileSystem) ReadBinaryFile(filePath string) ([]byte, error) {
	// log.Printf("ReadBinaryFile entered")
	// defer log.Printf("ReadBinaryFile exited")
	// Call the virtual function.
	fs.mutexLock.Lock()
	defer fs.mutexLock.Unlock()
	return fs.VirtualFunction_ReadBinaryFile(filePath)
}

func (fs *MockFileSystem) ReadTextFile(filePath string) (string, error) {
	// log.Printf("ReadTextFile entered")
	// defer log.Printf("ReadTextFile exited")
	// Call the virtual function.
	fs.mutexLock.Lock()
	defer fs.mutexLock.Unlock()
	return fs.VirtualFunction_ReadTextFile(filePath)
}

func (fs *MockFileSystem) Exists(path string) (bool, error) {
	// log.Printf("Exists entered")
	// defer log.Printf("Exists exited")
	// Call the virtual function.
	fs.mutexLock.Lock()
	defer fs.mutexLock.Unlock()
	return fs.VirtualFunction_Exists(path)
}

func (fs *MockFileSystem) DirExists(path string) (bool, error) {
	// log.Printf("DirExists entered")
	// defer log.Printf("DirExists exited")
	// Call the virtual function.
	fs.mutexLock.Lock()
	defer fs.mutexLock.Unlock()
	return fs.VirtualFunction_DirExists(path)
}

func (fs *MockFileSystem) GetUserHomeDirPath() (string, error) {
	// log.Printf("GetUserHomeDirPath entered")
	// defer log.Printf("GetUserHomeDirPath exited")
	fs.mutexLock.Lock()
	defer fs.mutexLock.Unlock()
	return fs.VirtualFunction_GetUserHomeDirPath()
}

func (fs MockFileSystem) OutputWarningMessage(message string) error {
	return fs.VirtualFunction_OutputWarningMessage(message)
}

// ------------------------------------------------------------------------------------
// Default implementations of the methods...
// ------------------------------------------------------------------------------------

func mockFSCreate(fs MockFileSystem, path string) (io.WriteCloser, error) {
	nodeToAdd := Node{content: nil, isDir: false}
	fs.data[path] = &nodeToAdd
	writer := NewOverridableMockFile(&fs, path)
	return writer, nil
}

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

func mockFSDeleteFile(fs MockFileSystem, pathToDelete string) {
	delete(fs.data, pathToDelete)
}

func mockFSMkTempDir(fs MockFileSystem) (string, error) {
	tempFolderPath := "/tmp" + strconv.Itoa(fs.random.Intn(math.MaxInt))
	err := fs.MkdirAll(tempFolderPath)
	return tempFolderPath, err
}

func mockFSMkdirAll(fs MockFileSystem, targetFolderPath string) error {
	nodeToAdd := Node{content: []byte(""), isDir: true}
	fs.data[targetFolderPath] = &nodeToAdd
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

func mockFSReadBinaryFile(fs MockFileSystem, filePath string) ([]byte, error) {
	bytes := make([]byte, 0)
	var err error
	node := fs.data[filePath]
	if node == nil {
		err = os.ErrNotExist
	} else {
		bytes = []byte(node.content)
	}
	return bytes, err
}

func mockFSReadTextFile(fs MockFileSystem, filePath string) (string, error) {
	text := ""
	var err error
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
	var err error
	node := fs.data[path]
	if node == nil {
		isExists = false
	}
	return isExists, err
}

func mockFSDirExists(fs MockFileSystem, path string) (bool, error) {
	var isDirExists bool // set to true by default.
	var err error
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

//------------------------------------------------------------------------------------
// Extra methods on the mock to allow unit tests to get data out of the mock object.
//------------------------------------------------------------------------------------

func (fs MockFileSystem) GetAllWarningMessages() string {
	messages := fs.warningMessageBuffer.String()
	log.Printf("Mock reading back previously collected warnings messages: %s", messages)
	return messages
}

func (fs *MockFileSystem) GetAllFilePaths(rootPath string) ([]string, error) {
	// log.Printf("GetAllFilePaths entered")
	// defer log.Printf("GetAllFilePaths exited")
	fs.mutexLock.Lock()
	defer fs.mutexLock.Unlock()

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
