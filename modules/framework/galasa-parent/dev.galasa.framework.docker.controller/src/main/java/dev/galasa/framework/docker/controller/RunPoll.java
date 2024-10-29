/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.docker.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.exception.ConflictException;
import com.github.dockerjava.api.model.Container;

import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IRun;
import io.prometheus.client.Counter;

public class RunPoll implements Runnable {
    private final Log                        logger           = LogFactory.getLog(getClass());

    private final Settings                   settings;
    private final DockerClient               dockerClient;
    private final IDynamicStatusStoreService dss;
    private final IFrameworkRuns             runs;
    private final QueuedComparator           queuedComparator = new QueuedComparator();

    private Counter                          submittedRuns;

    public RunPoll(IDynamicStatusStoreService dss, Settings settings, DockerClient dockerClient,
            IFrameworkRuns runs) {
        this.settings = settings;
        this.dockerClient = dockerClient;
        this.runs = runs;
        this.dss = dss;

        // *** Create metrics

        this.submittedRuns = Counter.build().name("galasa_docker_controller_submitted_runs")
                .help("The number of runs submitted by the Docker controller").register();

    }

    @Override
    public void run() {
        logger.info("Looking for new runs");

        try {
            // *** No we are not, get all the queued runs
            List<IRun> queuedRuns = this.runs.getQueuedRuns();
            // TODO filter by capability

            // *** Remove all the local runs
            Iterator<IRun> queuedRunsIterator = queuedRuns.iterator();
            while (queuedRunsIterator.hasNext()) {
                IRun run = queuedRunsIterator.next();
                if (run.isLocal()) {
                    queuedRunsIterator.remove();
                }
            }

            if (queuedRuns.isEmpty()) {
                logger.info("There are no queued runs");
                return;
            }

            while (true) {
                // *** Check we are not at max engines
                List<Container> pods = getContainers(this.dockerClient, this.settings);
                filterActiveRuns(pods);
                logger.info("Active runs=" + pods.size() + ",max=" + settings.getMaxEngines());

                int currentActive = pods.size();
                if (currentActive >= settings.getMaxEngines()) {
                    logger.info(
                            "Not looking for runs, currently at maximim engines (" + settings.getMaxEngines() + ")");
                    return;
                }

                // List<IRun> activeRuns = this.runs.getActiveRuns();

                // TODO Create the group algorithim same as the galasa scheduler

                // *** Build pool lists
                // HashMap<String, Pool> queuePools = getPools(queuedRuns);
                // HashMap<String, Pool> activePools = getPools(activeRuns);

                // *** cheat for the moment
                Collections.sort(queuedRuns, queuedComparator);

                IRun selectedRun = queuedRuns.remove(0);

                startPod(selectedRun);

                if (!queuedRuns.isEmpty()) {
                    Thread.sleep((long) settings.getRunPollRecheck() * 1000l); // *** Slight delay to allow Docker to
                    // catch up
                } else {
                    return;
                }
            }
        } catch (Exception e) {
            logger.error("Unable to poll for new runs", e);
        }

        return;
    }

    private void startPod(IRun run) {
        String runName = run.getName();
        String engineName = this.settings.getEngineLabel() + "_" + runName.toLowerCase();

        logger.info("Received run " + runName);

        String containerId = null;
        try {
            // *** First attempt to allocate the run to this controller
            HashMap<String, String> props = new HashMap<>();
            props.put("run." + runName + ".controller", settings.getPodName());
            if (!this.dss.putSwap("run." + runName + ".status", "queued", "allocated", props)) {
                logger.info("run allocated by another controller");
                return;
            }

            String choosenEngineName = engineName;

            boolean successful = false;
            int retry = 0;

            while (!successful) {
                try {
                    CreateContainerCmd cmd = dockerClient.createContainerCmd(choosenEngineName);
                    cmd.withName(choosenEngineName);
                    cmd.withImage(settings.getEngineImage());

                    HashMap<String, String> labels = new HashMap<>();
                    labels.put("galasaEngineController", this.settings.getEngineLabel());
                    labels.put("galasaRun", runName);
                    cmd.withLabels(labels);

                    ArrayList<String> cmds = new ArrayList<>();
                    cmds.add("java");
                    cmds.add("-jar");
                    cmds.add("boot.jar");
                    cmds.add("--obr");
                    cmds.add("file:galasa.obr");
                    cmds.add("--bootstrap");
                    cmds.add(settings.getBootstrap());
                    cmds.add("--run");
                    cmds.add(runName);
                    if (run.isTrace()) {
                        cmds.add("--trace");
                    }
                    cmd.withCmd(cmds); 

                    CreateContainerResponse response = cmd.exec();
                    containerId = response.getId();
                    logger.info("Engine Container " + engineName + " created with id " + containerId);
                    successful = true;
                    break;
                } catch(ConflictException e) {
                    retry++;
                    choosenEngineName = engineName + "_" + retry;
                    logger.info(
                            "Engine Pod " + engineName + " already exists, trying with " + choosenEngineName);
                } catch(Exception e) {
                    throw new DockerControllerException("Container create failed",e);
                }
                logger.info("Waiting 2 seconds before trying to create container again");
                Thread.sleep(2000);
            }

            // *** Now start the container

            successful = false;
            while (!successful) {
                try {
                    StartContainerCmd cmd = dockerClient.startContainerCmd(containerId);
                    cmd.exec();

                    logger.info("Engine Container " + choosenEngineName + " started with id " + containerId);
                    successful = true;
                    submittedRuns.inc();
                    break;
                } catch (Exception e) {
                    logger.error("Failed to start engine container", e);
                }
                logger.info("Waiting 2 seconds before trying to start container again");
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            logger.error("Failed to start new engine", e);
        }
        return;
    }

    // private HashMap<String, Pool> getPools(@NotNull List<IRun> runs) {
    // HashMap<String, Pool> pools = new HashMap<>();
    //
    // for(IRun run : runs) {
    // String poolid = getPoolId(run);
    // Pool pool = pools.get(poolid);
    // if (pool == null) {
    // pool = new Pool(poolid);
    // }
    // pool.runs.add(run);
    // }
    //
    // return pools;
    // }
    //
    // private String getPoolId(IRun run) {
    // if (settings.getRequestorsByGroup().contains(run.getRequestor())) {
    // return run.getRequestor() + "/" + run.getGroup();
    // }
    //
    // return run.getRequestor();
    // }

    public static @NotNull List<Container> getContainers(DockerClient dockerClient, Settings settings)
            throws DockerControllerException {


        try {
            ListContainersCmd cmd = dockerClient.listContainersCmd();
            cmd.withShowAll(true);

            ArrayList<String> labels = new ArrayList<>();
            labels.add("galasaEngineController=" + settings.getEngineLabel());
            cmd.withLabelFilter(labels);

            List<Container> possibleContainers = cmd.exec(); 

            ArrayList<Container> containers = new ArrayList<>(possibleContainers.size());

            if (possibleContainers != null) {
                for(Container container : possibleContainers) {
                    Map<String, String> containerLabels = container.getLabels();
                    if (containerLabels == null) {
                        continue;
                    }

                    if (containerLabels.containsKey("galasaRun")) {
                        containers.add(container);
                    }
                }
            }

            return containers;
        } catch(Exception e) {
            throw new DockerControllerException("Problem listing the engine containers",e);
        }
    }

    public static void filterActiveRuns(@NotNull List<Container> containers) {
        Iterator<Container> iContainer = containers.iterator();
        while (iContainer.hasNext()) {
            Container container = iContainer.next();
            if ("exited".equals(container.getState())) {
                iContainer.remove();
            }
        }
    }

    public static void filterTerminated(@NotNull List<Container> containers) {
        Iterator<Container> iContainer = containers.iterator();
        while (iContainer.hasNext()) {
            Container container = iContainer.next();

            if ("exited".equals(container.getState())) {
                continue;
            }
            iContainer.remove();
        }
    }

    // private static class Pool implements Comparable<Pool> {
    // private String id;
    // private ArrayList<IRun> runs = new ArrayList<>();
    //
    // public Pool(String id) {
    // this.id = id;
    // }
    //
    // @Override
    // public int compareTo(Pool o) {
    // return runs.size() - o.runs.size();
    // }
    //
    // }
    //
    //
    private static class QueuedComparator implements Comparator<IRun> {

        @Override
        public int compare(IRun o1, IRun o2) {
            return o1.getQueued().compareTo(o2.getQueued());
        }

    }

}