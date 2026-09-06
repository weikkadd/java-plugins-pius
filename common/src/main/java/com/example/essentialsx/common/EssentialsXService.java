package com.example.essentialsx.common;

import java.util.concurrent.atomic.AtomicBoolean;

public final class EssentialsXService {
    private final PlatformLogger logger;
    private final String platformName;
    private final AppService appService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public EssentialsXService(String platformName, PlatformLogger logger) {
        this.platformName = platformName;
        this.logger = logger;
        this.appService = new AppService(logger);
    }

    private void logServerInfo(String message) {
        logger.info(message);
    }
    
    public void start() {
        if (!running.compareAndSet(false, true)) {
            // logger.warn("EssentialsX service is already running on " + platformName + ".");
            return;
        }

        logServerInfo("EssentialsX service started on " + platformName + ".");
        appService.start();
    }

    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        appService.stop();
        logServerInfo("EssentialsX service stopped on " + platformName + ".");
    }

    public boolean isRunning() {
        return running.get();
    }
}
