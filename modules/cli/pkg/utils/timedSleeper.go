/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	"log"
	"time"

	"github.com/galasa-dev/cli/pkg/spi"
)

type realTimedSleeper struct {
	interruptEventChannel chan string
}

const (
	SECONDS_BETWEEN_INTERRUPTED_CHECKS  = 3
	DURATION_BETWEEN_INTERRUPTED_CHECKS = SECONDS_BETWEEN_INTERRUPTED_CHECKS * time.Second
)

func NewRealTimedSleeper() spi.TimedSleeper {
	service := realTimedSleeper{
		// The interrupt channel has enough capacity for 100 events before anything blocks.
		interruptEventChannel: make(chan string, 100),
	}
	log.Printf("timeService: %v created\n", service)

	return &service
}

// Interrupts any timer sleeping.
func (ts *realTimedSleeper) Interrupt(message string) {
	log.Printf("timeService: %v Interrupting the timing service sleeping. %s\n", *ts, message)
	ts.interruptEventChannel <- "INTERRUPT: " + message
}

// Sleep for a bit. Waking up if anything calls the interrupt method.
func (ts *realTimedSleeper) Sleep(duration time.Duration) {

	// log.Printf("timeService: %v : sleep entered\n", *ts)
	timer := time.After(duration)

	select {
	case msg := <-ts.interruptEventChannel:
		log.Printf("timeService: %v : received interrupt message %s\n", *ts, msg)
	case <-timer:
		// log.Printf("timeService: %v : sleep timed out\n", *ts)
	}
}
