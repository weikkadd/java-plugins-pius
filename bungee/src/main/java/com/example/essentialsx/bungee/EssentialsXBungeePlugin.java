package com.example.essentialsx.bungee;

import com.example.essentialsx.common.EssentialsXService;
import com.example.essentialsx.common.PlatformLogger;
import net.md_5.bungee.api.plugin.Plugin;

public final class EssentialsXBungeePlugin extends Plugin {
    private EssentialsXService service;

    @Override
    public void onEnable() {
        service = new EssentialsXService("BungeeCord", new BungeeLogger());
        service.start();
    }

    @Override
    public void onDisable() {
        if (service != null) {
            service.stop();
        }
    }

    private final class BungeeLogger implements PlatformLogger {
        @Override
        public void info(String message) {
            getLogger().info(message);
        }

        @Override
        public void warn(String message) {
            getLogger().warning(message);
        }

        @Override
        public void error(String message, Throwable throwable) {
            getLogger().severe(message);
            if (throwable != null) {
                throwable.printStackTrace();
            }
        }
    }
}
