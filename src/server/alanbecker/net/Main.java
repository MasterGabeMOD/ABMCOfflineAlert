package server.alanbecker.net;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;


public class Main {

    private final Map<String, Boolean> serverStatus = new HashMap<>();
    private final Map<String, Long> serverOfflineTimestamps = new HashMap<>();
    private final List<String> monitoredServers = Arrays.asList(
            "Lobby1", "Lobby2", "BattleGrounds", "Factions",
            "PSurvival", "Creative", "Survival", "Vanilla"
    );
    private ScheduledTask offlineAlertTask;

    public Main(OfflineListener plugin) {
        for (String serverName : monitoredServers) {
            serverStatus.put(serverName, true);
        }
        startOfflineAlertTask(plugin);
    }

    public void startOfflineAlertTask(OfflineListener plugin) {
        offlineAlertTask = ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            for (String serverName : monitoredServers) {
                if (serverStatus.get(serverName) == false && serverOfflineTimestamps.containsKey(serverName) &&
                        currentTime - serverOfflineTimestamps.get(serverName) >= TimeUnit.MINUTES.toMillis(5)) {
                    String message = "§c§lAttention! §cThe server §f§l" + serverName + " §chas been offline for more than 5 minutes! §cPlease contact an administrator if you believe this is an error.";
                    ProxyServer.getInstance().broadcast(message);
                    serverOfflineTimestamps.remove(serverName);
                }
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public void checkServers() {
        Set<String> alreadyBroadcasted = new HashSet<>();

        for (String serverName : monitoredServers) {
            ServerInfo server = ProxyServer.getInstance().getServerInfo(serverName);
            if (server == null) {
                continue;
            }

            server.ping((result, error) -> {
                boolean online = error == null;

                if (serverStatus.containsKey(serverName)) {
                    boolean previousStatus = serverStatus.get(serverName);
                    if (previousStatus != online && !alreadyBroadcasted.contains(serverName)) {
                        serverStatus.put(serverName, online);
                        alreadyBroadcasted.add(serverName);

                        String message = "§f§lAttention! The server §c§l" + serverName + " §f§lis now " + (online ? "§a§lonline!" :
                                "§c§loffline!");

                        ProxyServer.getInstance().broadcast(message);

                        if (!online) {
                            serverOfflineTimestamps.put(serverName, System.currentTimeMillis());
                        } else {
                            serverOfflineTimestamps.remove(serverName);
                        }
                    } else if (previousStatus == online && alreadyBroadcasted.contains(serverName)) {
                        alreadyBroadcasted.remove(serverName);
                    }
                }
            });
        }
    }


    public void runServerCheck() {
        checkServers();
    }
}
