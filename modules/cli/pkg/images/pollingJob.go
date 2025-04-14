/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package images

import (
	"log"
	"sync"
	"time"
)

// A go routine which runs a background thread and coordinates it.

type PollingJob interface {
	Start()
	Stop()
}

type PollingJobImpl struct {
	jobName                  string
	milliSecondsBetweenPolls int
	ticker                   *time.Ticker
	doneEventChannel         chan bool
	functionToCall           func() error
	mutexLock                sync.Mutex
}

const (
	// 20 seconds
	DEFAULT_MILLISECS_BETWEEN_POLLS = 20 * 1000
)

func NewPollingJob(jobName string, milliSecondsBetweenPolls int, functionToCall func() error) PollingJob {
	job := new(PollingJobImpl)
	job.milliSecondsBetweenPolls = milliSecondsBetweenPolls
	job.functionToCall = functionToCall
	job.jobName = jobName
	return job
}

func (job *PollingJobImpl) Start() {
	log.Printf("Job '%s' starting.\n", job.jobName)
	duration := time.Millisecond * time.Duration(job.milliSecondsBetweenPolls)
	job.ticker = time.NewTicker(duration)

	job.doneEventChannel = make(chan bool)
	go func() {
		for { // loop forever
			select {
			case <-job.doneEventChannel:
				// something has issued a done event to the channel.
				log.Printf("Job '%s' is complete\n", job.jobName)
				return // Exit the go routine.
			case t := <-job.ticker.C:
				// A tick has happened.
				log.Println("Tick at", t)
				err := job.functionToCall()
				if err != nil {
					log.Printf("Job '%s' - Error returned by job function. %v\n", job.jobName, err)
				}
			}
		}
	}()
	log.Printf("Job '%s' Started\n", job.jobName)
}

func (job *PollingJobImpl) Stop() {
	log.Printf("Job '%s' stopping.\n", job.jobName)

	// Get a mutex lock so if we've got two thread stopping the job at once it can cope with it.
	// The first one in wins the race.
	job.mutexLock.Lock()

	if job.doneEventChannel != nil {
		// The job has not already been stopped.

		// Stop any further ticks from happening.
		job.ticker.Stop()

		// Signal the go routine attached to the ticker to stop running in the background.
		job.doneEventChannel <- true

		// null-out the event channel so it can't be used any more.
		// And also mark the fact that the job has now been stopped.
		job.doneEventChannel = nil

		log.Printf("Job '%s' is stopped.\n", job.jobName)

		log.Printf("Job '%s' invoking the polling function one last time", job.jobName)
		err := job.functionToCall()
		if err != nil {
			log.Printf("Error returned by job function. %v\n", err)
		}

	} else {
		log.Printf("Job '%s' is already stopped. Doing nothing extra.\n", job.jobName)
	}
	job.mutexLock.Unlock()
}
