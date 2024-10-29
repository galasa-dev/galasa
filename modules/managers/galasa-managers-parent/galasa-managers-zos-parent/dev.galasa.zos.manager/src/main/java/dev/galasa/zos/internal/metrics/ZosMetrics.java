/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal.metrics;

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IMetricsProvider;
import dev.galasa.framework.spi.IMetricsServer;
import dev.galasa.framework.spi.MetricsServerException;
import io.prometheus.client.Gauge;

@Component(service= {IMetricsProvider.class})
public class ZosMetrics implements IMetricsProvider, Runnable {
    private final Log                          logger = LogFactory.getLog(getClass());    
    private IFramework                         framework;
    private IMetricsServer                     metricsServer;
    private IDynamicStatusStoreService         dss;

    private Gauge                              noSlots;

    @Override
    public boolean initialise(IFramework framework, IMetricsServer metricsServer) throws MetricsServerException {
        this.framework = framework;
        this.metricsServer = metricsServer;
        try {
            this.dss = this.framework.getDynamicStatusStoreService("zos");
        } catch (Exception e) {
            throw new MetricsServerException("Unable to initialise zOS Metrics", e);
        }

        this.noSlots = Gauge.build()
                .name("galasa_zos_insufficent_slots_total")
                .help("How many times insufficent slots has occurred")
                .register();

        return true;
    }

    @Override
    public void start() {

        this.metricsServer.getScheduledExecutorService().scheduleWithFixedDelay(this, 
                1, 
                10, 
                TimeUnit.SECONDS);
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void run() {
        logger.info("zOS Poll");;

        try {
            //*** Insufficent Slots
            String sNoSlots = AbstractManager.nulled(dss.get("metrics.slots.insufficent"));
            if (sNoSlots == null) {
                this.noSlots.set(0.0);
            } else {
                this.noSlots.set(Double.parseDouble(sNoSlots));
            }


            this.metricsServer.metricsPollSuccessful();
        } catch(Exception e) {
            logger.error("Problem with zOS poll",e);
        }

    }


}
