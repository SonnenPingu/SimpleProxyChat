package com.beanbeanjuice.simpleproxychat.utility.listeners.velocity;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatVelocity;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.Player;
import de.myzelyam.api.vanish.VelocityVanishAPI;

import java.util.HashSet;
import java.util.Set;

public class VelocityVanishListener {

    private final SimpleProxyChatVelocity plugin;
    private final VelocityServerListener listener;
    private final Set<Player> vanishedPlayers = new HashSet<>();

    public VelocityVanishListener(SimpleProxyChatVelocity plugin, VelocityServerListener listener) {
        this.plugin = plugin;
        this.listener = listener;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        startVanishListener();
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        checkVanishStatus(event.getPlayer());
    }

    private void checkVanishStatus(Player player) {
        if (!plugin.getConfig().getAsBoolean(ConfigDataKey.VANISH_ENABLED)) return;
        
        boolean isInvisible = VelocityVanishAPI.isInvisible(player);

        if (isInvisible && !vanishedPlayers.contains(player)) {
            vanishedPlayers.add(player);
            listener.leave(player); // Spieler wird unsichtbar -> Simuliere Verlassen
        } else if (!isInvisible && vanishedPlayers.contains(player)) {
            vanishedPlayers.remove(player);
            listener.join(player, player.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("unknown")); // Spieler wird sichtbar -> Simuliere Beitritt
        }
    }
    
    public void startVanishListener() {
        plugin.getProxyServer().getAllPlayers().forEach(this::checkVanishStatus); // Initialer Check beim Start
    }
}
