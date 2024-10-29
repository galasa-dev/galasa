/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.docker.controller;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Container;

import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IRun;

public class RunDeleted implements Runnable {
    private final Log                 logger = LogFactory.getLog(getClass());

    private final Settings            settings;
    private final DockerClient        dockerClient;
    private final IFrameworkRuns      runs;

    public RunDeleted(Settings settings, DockerClient dockerClient, IFrameworkRuns runs) {
        this.settings = settings;
        this.dockerClient = dockerClient;
        this.runs = runs;
    }

    @Override
    public void run() {
        logger.info("Starting Deleted runs scan");

        try {
            List<Container> containers = RunPoll.getContainers(dockerClient, settings);
            RunPoll.filterTerminated(containers);

            for (Container container : containers) {
                String runName = container.getLabels().get("galasaRun");
                if (runName == null) {
                    continue;
                }

                IRun run = runs.getRun(runName);
                if (run != null) {
                    continue;
                }

                logger.info("Deleting container " + container.getNames()[0] + " as run has been deleted");
                deleteContainer(container);
            }

        } catch (Exception e) {
            logger.error("Problem with Deleted runs scan", e);
        }

    }

    private void deleteContainer(Container container) {
        try {
            logger.info("Deleting container " + container.getNames()[0] + " id " + container.getId());
            dockerClient.removeContainerCmd(container.getId()).withForce(true).exec();
        } catch (NotFoundException e) {
        } catch (Exception e) {
            logger.error("Failed to delete engine pod", e);
        }
    }

}
