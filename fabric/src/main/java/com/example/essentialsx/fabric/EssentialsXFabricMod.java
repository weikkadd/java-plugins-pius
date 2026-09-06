package com.example.essentialsx.fabric;

import com.example.essentialsx.common.EssentialsXService;
import com.example.essentialsx.common.PlatformLogger;
import net.fabricmc.api.ModInitializer;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class EssentialsXFabricMod implements ModInitializer {
    private static final Logger LOGGER = Logger.getLogger("EssentialsX");
    private EssentialsXService service;

    @Override
    public void onInitialize() {
        service = new EssentialsXService("Fabric", new FabricLogger());
        service.start();
        Runtime.getRuntime().addShutdownHook(new Thread(service::stop, "essentialsx-fabric-shutdown"));
    }

    private static final class FabricLogger implements PlatformLogger {
        @Override
        public void info(String message) {
            LOGGER.info(message);
        }

        @Override
        public void warn(String message) {
            LOGGER.warning(message);
        }

        @Override
        public void error(String message, Throwable throwable) {
            LOGGER.log(Level.SEVERE, message, throwable);
        }
    }
}
