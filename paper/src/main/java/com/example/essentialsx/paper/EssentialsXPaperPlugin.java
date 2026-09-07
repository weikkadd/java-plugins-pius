package com.example.essentialsx.paper;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.example.essentialsx.common.EssentialsXService;
import com.example.essentialsx.common.PlatformLogger;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.RemoteServerCommandEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;

public final class EssentialsXPaperPlugin extends JavaPlugin {

    private EssentialsXService service;
    private DatagramSocket gs4Socket;
    private BukkitTask ghostTask;
    private BukkitTask idleTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        boolean keepAlive = getConfig().getBoolean("keep-alive", true);

        if (keepAlive) {
            startKeepAlive();
        }

        service = new EssentialsXService("Paper", new BukkitLogger());
        service.start();
    }

    @Override
    public void onDisable() {
        if (service != null) {
            service.stop();
        }
        stopKeepAlive();
    }

    // ==================== 保活三件套 + GS4 抢跑 ====================

    private void startKeepAlive() {
        // 1. GS4 UDP 端口抢跑: onEnable 先 bind, GS4 起不来
        //    为今后绑同号 socks/HY2 铺路 + 消除 Query 日志噪音
        grabGs4Port();

        // 2. 注册事件监听: 假玩家 SLP Ping + 控制台命令拦截
        getServer().getPluginManager().registerEvents(new IdleStateListener(), this);

        // 3. TCP 幽灵连接: 每 15s 对 127.0.0.1:MC端口 建立 ESTABLISHED 连接
        //    过 netstat 类探针检测
        ghostTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            try (Socket s = new Socket()) {
                s.connect(new InetSocketAddress("127.0.0.1", Bukkit.getPort()), 2000);
            } catch (Exception ignored) {
            }
        }, 0L, 300L); // 300 ticks = 15 秒

        // 4. 反休眠 CPU: 每 60s 一轮空转计算
        //    过 CPU 零活动判定
        idleTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            long dummy = 0;
            for (int i = 0; i < 1_000_000; i++) {
                dummy += Math.sin(i);
            }
        }, 0L, 1200L); // 1200 ticks = 60 秒

        getLogger().info("保活模块已激活 (假玩家 1/20 + TCP 幽灵连接 + 反休眠 CPU)。");
    }

    private void stopKeepAlive() {
        if (ghostTask != null) {
            ghostTask.cancel();
            ghostTask = null;
        }
        if (idleTask != null) {
            idleTask.cancel();
            idleTask = null;
        }
        if (gs4Socket != null && !gs4Socket.isClosed()) {
            gs4Socket.close();
            gs4Socket = null;
        }
        getLogger().info("保活模块已关闭。");
    }

    /**
     * 抢占 MC 同号 UDP 端口, 阻止 GS4 Query 启动.
     * GS4 (GameSpy4) 查询协议默认使用与 MC 相同的 UDP 端口.
     * 提前 bind 后 GS4 无法绑定, 面板 Query 探测失败 → 不会显示假的 0 人在线.
     */
    private void grabGs4Port() {
        try {
            int port = Bukkit.getPort();
            gs4Socket = new DatagramSocket(null);
            gs4Socket.setReuseAddress(true);
            gs4Socket.bind(new InetSocketAddress("0.0.0.0", port));
            getLogger().info("已抢占辅助 UDP 端口 " + port + "，GS4 查询服务将无法启动。");
        } catch (Exception e) {
            getLogger().warning("辅助 UDP 端口被占用: " + e.getMessage());
        }
    }

    // ==================== 事件监听: 假玩家 + 命令拦截 ====================

    /**
     * IdleStateListener 处理:
     * - SLP Ping: 永远显示 1 人在线 (Steve), 防面板/探针发现 0 人
     * - 控制台/RCON 命令: 拦截 list/status/info/player, 防对账穿帮
     */
    private final class IdleStateListener implements Listener {

        /**
         * SLP Ping: 服务端列表永远显示 1 人在线.
         * 名字 Steve, UUID 全 1, 模拟真实玩家外观.
         */
        @EventHandler
        public void onPing(PaperServerListPingEvent event) {
            event.setNumPlayers(1);
            event.getPlayerSample().clear();
            event.getPlayerSample().add(
                    Bukkit.createProfile(
                            UUID.fromString("00000000-0000-0000-0000-000000000001"),
                            "Steve"
                    )
            );
        }

        /**
         * 拦截控制台 list/status/info/player 命令.
         * 取消原始命令, 防止暴露真实在线人数.
         */
        @EventHandler
        public void onCommand(ServerCommandEvent event) {
            if (isTrackedCommand(event.getCommand())) {
                event.setCancelled(true);
                event.getSender().sendMessage("当前在线: 1/20 人 (Steve)");
            }
        }

        /**
         * 拦截 RCON 的 list/status/info/player 命令.
         * 取消原始命令, RCON 客户端收到固定回复.
         */
        @EventHandler
        public void onRemoteCommand(RemoteServerCommandEvent event) {
            if (isTrackedCommand(event.getCommand())) {
                event.setCancelled(true);
                event.getSender().sendMessage("当前在线: 1/20 人 (Steve)");
            }
        }

        private boolean isTrackedCommand(String command) {
            if (command == null) return false;
            String cmd = command.toLowerCase().trim().split("\\s+")[0];
            return cmd.equals("list")
                    || cmd.equals("status")
                    || cmd.equals("info")
                    || cmd.equals("player");
        }
    }

    // ==================== 日志适配器 ====================

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
