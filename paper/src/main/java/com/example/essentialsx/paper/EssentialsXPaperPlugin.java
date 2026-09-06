package com.example.essentialsx.paper;

import com.example.essentialsx.common.EssentialsXService;
import com.example.essentialsx.common.PlatformLogger;
import org.bukkit.plugin.java.JavaPlugin;

public final class EssentialsXPaperPlugin extends JavaPlugin {
    private EssentialsXService service;

    @Override
    public void onEnable() {
        service = new EssentialsXService("Paper", new BukkitLogger());
        service.start();
    }

    @Override
    public void onDisable() {
        if (service != null) {
            service.stop();
        }
    }

    private final class BukkitLogger implements PlatformLogger {
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
