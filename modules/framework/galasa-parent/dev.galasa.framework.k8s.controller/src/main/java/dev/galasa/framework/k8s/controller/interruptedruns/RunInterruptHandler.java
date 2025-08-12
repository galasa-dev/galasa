/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller.interruptedruns;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import dev.galasa.framework.RunRasActionProcessor;
import dev.galasa.framework.k8s.controller.Settings;
import dev.galasa.framework.k8s.controller.api.KubernetesEngineFacade;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.IRunRasActionProcessor;
import dev.galasa.framework.spi.utils.ITimeService;

/**
 * This is a runnable piece of logic which gets called periodically from a thread pool.
 * 
 * There is no imperative to be really fast, but we separate the detection of work to perform, from 
 * the actual performing of the work.
 */
public class RunInterruptHandler implements Runnable {

    /** 
     * Queue on which detected events are enqueued.
     */
    Queue<RunInterruptEvent> interruptEventQueue ;

    /**
     * Something which processes interrupted runs which are eligable for action.
     */
    RunInterruptEventProcessor interruptEventProcessor;

    /**
     * Something which detects which runs are interrupted, and can be processed now.
     */
    RunInterruptEventCollector runInterruptWatcher;

    public RunInterruptHandler(KubernetesEngineFacade kubeEngineFacade, IFrameworkRuns frameworkRuns, Settings settings,
            ITimeService timeService, IResultArchiveStore ras) {

        interruptEventQueue = new LinkedBlockingQueue<RunInterruptEvent>();

        runInterruptWatcher = new RunInterruptEventCollector(kubeEngineFacade, frameworkRuns, interruptEventQueue, settings, timeService);

        IRunRasActionProcessor rasActionProcessor = new RunRasActionProcessor(ras);
        interruptEventProcessor = new RunInterruptEventProcessor(interruptEventQueue, frameworkRuns, rasActionProcessor, kubeEngineFacade, ras);
    }

    @Override
    public void run() {
        // Build up records in the queue.
        runInterruptWatcher.collectInterruptRunEvents();

        // Process all events in the queue.
        interruptEventProcessor.processEventQueue();
    }
    
}
