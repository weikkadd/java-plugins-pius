package com.example.essentialsx.velocity;

import com.example.essentialsx.common.EssentialsXService;
import com.example.essentialsx.common.PlatformLogger;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import org.slf4j.Logger;

@Plugin(
        id = "essentialsx",
        name = "EssentialsX",
        version = "1.21.11",
        description = "EssentialsX multiplatform Velocity entry.",
        authors = {"example"}
)
public final class EssentialsXVelocityPlugin {
    private final Logger logger;
    private EssentialsXService service;

    @Inject
    public EssentialsXVelocityPlugin(Logger logger) {
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        service = new EssentialsXService("Velocity", new VelocityLogger());
        service.start();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (service != null) {
            service.stop();
        }
    }

    private final class VelocityLogger implements PlatformLogger {
        @Override
        public void info(String message) {
            logger.info(message);
        }

        @Override
        public void warn(String message) {
            logger.warn(message);
        }

        @Override
        public void error(String message, Throwable throwable) {
            logger.error(message, throwable);
        }
    }
}
