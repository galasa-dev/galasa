/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package launcher

import (
	"io"
	"os/exec"
)

//----------------------------------------------------------------------------------
// Interfaces
//----------------------------------------------------------------------------------

// ProcessFactory is something which can create new processes.
// This allows us to supply a process factory whcih creates mock instances of processes
// for unit testing.
type ProcessFactory interface {
	NewProcess() Process
}

// A process is something which can be started, and waited upon.
type Process interface {

	// Start the process, giving it a command with arguments, and somewhere
	// into which it can write to stdout and stderr.
	Start(cmd string, args []string, stdOut io.Writer, stdErr io.Writer) error

	// Wait for the process to complete. This is a blocking call.
	Wait() error
}

//----------------------------------------------------------------------------------
// Implementation which creates real processes on the native operating system
//----------------------------------------------------------------------------------

// The process factory which can create real child processes on the operating system
type realProcessFactory struct {
}

// A real process.
type realProcess struct {
	process *exec.Cmd
}

// NewRealProcessFactory used to create a factory which can create real processes.
func NewRealProcessFactory() ProcessFactory {
	var factory = new(realProcessFactory)
	return factory
}

// Create the process.
func (*realProcessFactory) NewProcess() Process {
	result := new(realProcess)
	return result
}

// Start the process.
func (proc *realProcess) Start(cmd string, args []string, stdOut io.Writer, stdErr io.Writer) error {
	proc.process = exec.Command(cmd, args...)
	proc.process.Stdout = stdOut
	proc.process.Stderr = stdErr

	err := proc.process.Start()
	return err
}

// Wait for the process to complete.
func (proc *realProcess) Wait() error {
	err := proc.process.Wait()
	return err
}

// ----------------------------------------------------------------------------------
// A mock implementation which creates mock processes for use in unit testing.
// ----------------------------------------------------------------------------------
type mockProcessFactory struct {
	mockToServeUp *mockProcess
}

// A mock process factory returns a mock process.
func NewMockProcessFactory(mockToServeUp *mockProcess) ProcessFactory {
	mockFactory := new(mockProcessFactory)
	mockFactory.mockToServeUp = mockToServeUp
	return mockFactory
}

// A mock process holds some test data on how it should behave.
type mockProcess struct {
	stdOut io.Writer
	stdErr io.Writer
	cmd    string
	args   []string
}

// Create a new mock process.
func (factory *mockProcessFactory) NewProcess() Process {
	return factory.mockToServeUp
}

func NewMockProcess() *mockProcess {
	return new(mockProcess)
}

// Wait for the mock process to end.
// The real equivalent would wait for a but probably.
// The mock returns immediately without blocking.
func (mockProcess *mockProcess) Wait() error {

	// Waiting for the mock process causes it to simulate a shut-down.
	mockProcess.stdOut.Write([]byte("d.g.f.Framework - Framework shutdown\n"))
	return nil
}

func (mockProcess *mockProcess) Start(cmd string, args []string, stdOut io.Writer, stdErr io.Writer) error {

	// Store the values received by the mock so they can be examined.
	mockProcess.stdOut = stdOut
	mockProcess.stdErr = stdErr
	mockProcess.cmd = cmd
	mockProcess.args = args

	// Simulate some tracing which gets parsed.
	mockProcess.stdOut.Write([]byte("Mock Process starting up.\n"))
	mockProcess.stdOut.Write([]byte("Allocated Run Name L12345 to this run\n"))
	mockProcess.stdOut.Write([]byte("Result Archive Stores are [/temp/ras]\n"))

	return nil
}
