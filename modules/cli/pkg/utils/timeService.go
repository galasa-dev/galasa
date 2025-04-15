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

type timeService struct {
	interruptEventChannel chan string
}

func NewRealTimeService() spi.TimeService {
	service := timeService{
		// The interrupt channel has enough capacity for 100 events before anything blocks.
		interruptEventChannel: make(chan string, 100),
	}
	log.Printf("timeService: %v created\n", service)

	return &service
}

// Retrieves the current time, with the location set to UTC.
func (ts *timeService) Now() time.Time {
	return time.Now().UTC()
}

// Sleeps for a given duration
func (ts *timeService) Sleep(duration time.Duration) {
	time.Sleep(duration)
}
