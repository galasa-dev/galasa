/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	"time"

	"github.com/galasa-dev/cli/pkg/spi"
)

type MockTimeService struct {
	MockNow time.Time
}

func NewMockTimeServiceAsMock(now time.Time) *MockTimeService {
	return &MockTimeService{MockNow: now}
}

func NewMockTimeService() *MockTimeService {
	return NewMockTimeServiceAsMock(time.Now())
}

func NewOverridableMockTimeService(now time.Time) spi.TimeService {
	return NewMockTimeServiceAsMock(now)
}

func (ts *MockTimeService) AdvanceClock(duration time.Duration) {
	ts.MockNow = ts.MockNow.Add(duration)
}

func (ts *MockTimeService) Now() time.Time {
	return ts.MockNow
}

func (ts *MockTimeService) Sleep(duration time.Duration) {
	ts.AdvanceClock(duration)
}
