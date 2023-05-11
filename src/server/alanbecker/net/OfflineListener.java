package server.alanbecker.net;

import net.md_5.bungee.api.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public class OfflineListener extends Plugin {

    private Main serverMonitor;

    @Override
    public void onEnable() {
        serverMonitor = new Main(this);
        getProxy().getScheduler().schedule(this, serverMonitor::checkServers, 0, 15, TimeUnit.SECONDS);
    }

    @Override
    public void onDisable() {
        serverMonitor = null;
    }
}
