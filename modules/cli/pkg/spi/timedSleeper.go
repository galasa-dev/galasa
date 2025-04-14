/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package spi

import "time"

// An encapsulation of code where one thread sleeps waiting for an event, but will timeout,
// and another thread interrupts the sleeper.
// This allows unit tests to simulate the interrupt easier in mock code without using separate threads.
type TimedSleeper interface {
	Sleep(duration time.Duration)
	Interrupt(message string)
}
