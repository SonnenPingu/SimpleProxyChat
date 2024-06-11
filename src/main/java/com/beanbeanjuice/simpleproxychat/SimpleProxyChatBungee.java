package com.beanbeanjuice.simpleproxychat;

import com.beanbeanjuice.simpleproxychat.commands.bungee.*;
import com.beanbeanjuice.simpleproxychat.commands.bungee.ban.BungeeBanCommand;
import com.beanbeanjuice.simpleproxychat.commands.bungee.ban.BungeeUnbanCommand;
import com.beanbeanjuice.simpleproxychat.socket.bungee.BungeeCordPluginMessagingListener;
import com.beanbeanjuice.simpleproxychat.utility.helper.Helper;
import com.beanbeanjuice.simpleproxychat.utility.helper.WhisperHandler;
import com.beanbeanjuice.simpleproxychat.utility.BanHelper;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import com.beanbeanjuice.simpleproxychat.utility.epoch.EpochHelper;
import com.beanbeanjuice.simpleproxychat.utility.listeners.bungee.BungeeServerListener;
import com.beanbeanjuice.simpleproxychat.utility.listeners.bungee.BungeeVanishListener;
import com.beanbeanjuice.simpleproxychat.chat.ChatHandler;
import com.beanbeanjuice.simpleproxychat.discord.Bot;
import com.beanbeanjuice.simpleproxychat.utility.UpdateChecker;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.beanbeanjuice.simpleproxychat.utility.status.ServerStatusManager;
import de.myzelyam.api.vanish.BungeeVanishAPI;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import org.bstats.bungeecord.Metrics;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public final class SimpleProxyChatBungee extends Plugin {

    @Getter private Config config;
    @Getter private EpochHelper epochHelper;
    @Getter private Bot discordBot;
    @Getter private Metrics metrics;
    @Getter private BungeeServerListener serverListener;
    @Getter private WhisperHandler whisperHandler;
    @Getter private BanHelper banHelper;

    @Override
    public void onEnable() {
        this.getLogger().info("Das Plugin wird gestartet.");

        this.config = new Config(this.getDataFolder());
        this.config.initialize();

        epochHelper = new EpochHelper(config);

        this.getLogger().info("Versuch, den Discord-Bot zu initialisieren... (falls aktiviert)");
        discordBot = new Bot(this.config);

        this.getProxy().getScheduler().runAsync(this, () -> {
            try { discordBot.start();
            } catch (Exception e) { getLogger().warning("Es gab einen Fehler beim Starten des Discord-Bots: " + e.getMessage()); }
        });

        registerListeners();
        hookPlugins();
        registerCommands();

        // Discord Topic Updater
        this.getProxy().getScheduler().schedule(this, () -> {
            int numPlayers = this.getProxy().getPlayers().size();

            if (config.getAsBoolean(ConfigDataKey.VANISH_ENABLED))
                numPlayers = (int) this.getProxy().getPlayers().stream()
                        .filter((player) -> !BungeeVanishAPI.isInvisible(player))
                        .count();

            discordBot.channelUpdaterFunction(numPlayers);
        }, 5, 5, TimeUnit.MINUTES);

        // Update Checker
        startUpdateChecker();

        // bStats Stuff
        this.getLogger().info("Starting bStats... (WENN AKTIVIERT)");
        this.metrics = new Metrics(this, 21146);

        startPluginMessaging();

        // Plugin has started.
        this.getLogger().log(Level.INFO, "Das Plugin wurde gestartet.");

        // Send Initial Server Status
        discordBot.addRunnableToQueue(() -> {
            this.getProxy().getScheduler().schedule(this, () -> {
                this.config.overwrite(ConfigDataKey.PLUGIN_STARTING, false);

                ServerStatusManager manager = serverListener.getServerStatusManager();
                manager.getAllStatusStrings().forEach((string) -> this.getLogger().info(string));

                if (!config.getAsBoolean(ConfigDataKey.USE_INITIAL_SERVER_STATUS)) return;
                if (!config.getAsBoolean(ConfigDataKey.DISCORD_PROXY_STATUS_ENABLED)) return;
                this.discordBot.sendMessageEmbed(manager.getAllStatusEmbed());
            }, config.getAsInteger(ConfigDataKey.SERVER_UPDATE_INTERVAL) * 2L, TimeUnit.SECONDS);
        });
    }

private void startUpdateChecker() {
    String currentVersion = this.getDescription().getVersion();

    UpdateChecker updateChecker = new UpdateChecker(
        config,
        currentVersion,
        (message) -> {
            if (!config.getAsBoolean(ConfigDataKey.UPDATE_NOTIFICATIONS)) {
                return; // Benachrichtigungen sind deaktiviert, also nichts tun
            }

            this.getLogger().info(Helper.sanitize(message)); // Loggen der bereinigten Nachricht

            // Formatieren der Update-Nachricht
            Component minimessage = MiniMessage.miniMessage().deserialize(
                config.getAsString(ConfigDataKey.PLUGIN_PREFIX) + message
            );

            // Senden der Nachricht nur an Spieler mit der entsprechenden Berechtigung
            this.getProxy().getPlayers().stream()
                .filter(player -> player.hasPermission(Permission.READ_UPDATE_NOTIFICATION.getPermissionNode()))
                .forEach(player -> player.sendMessage(ChatMessageType.CHAT, BungeeComponentSerializer.get().serialize(minimessage)));
        }
    );

    // Planen der Update-Prüfung mit Fehlerbehandlung
    this.getProxy().getScheduler().schedule(this, () -> {
        try {
            updateChecker.checkUpdate(); // Update-Prüfung durchführen
        } catch (Exception e) {
            getLogger().warning("Fehler bei der Update-Prüfung: " + e.getMessage());
        }
    }, 0, 12, TimeUnit.HOURS); // Alle 12 Stunden wiederholen
}


    
    private void hookPlugins() {
        PluginManager pm = this.getProxy().getPluginManager();

        if (pm.getPlugin("PremiumVanish") != null || pm.getPlugin("SuperVanish") != null) {
            this.config.overwrite(ConfigDataKey.VANISH_ENABLED, true);
            this.getLogger().log(Level.INFO, "PremiumVanish/SuperVanish-Unterstützung wurde aktiviert.");
            this.getProxy().getPluginManager().registerListener(this, new BungeeVanishListener(serverListener, config));
        }

        if (pm.getPlugin("LuckPerms") != null) {
            config.overwrite(ConfigDataKey.LUCKPERMS_ENABLED, true);
            getLogger().info("LuckPerms-Unterstützung wurde aktiviert.");
        }

        if (pm.getPlugin("LiteBans") != null) {
            config.overwrite(ConfigDataKey.LITEBANS_ENABLED, true);
            getLogger().info("LiteBans-Unterstützung wurde aktiviert.");
        }

        if (pm.getPlugin("AdvancedBan") != null) {
            config.overwrite(ConfigDataKey.ADVANCEDBAN_ENABLED, true);
            getLogger().info("AdvancedBan-Unterstützung wurde aktiviert.");
        }

        if (pm.getPlugin("NetworkManager") != null) {
            config.overwrite(ConfigDataKey.NETWORKMANAGER_ENABLED, true);
            getLogger().info("NetworkManager-Unterstützung wurde aktiviert.");
        }

        if (!config.getAsBoolean(ConfigDataKey.LITEBANS_ENABLED) 
            && !config.getAsBoolean(ConfigDataKey.ADVANCEDBAN_ENABLED) 
            && config.getAsBoolean(ConfigDataKey.USE_SIMPLE_PROXY_CHAT_BANNING_SYSTEM)) {

            getLogger().info("LiteBans und AdvancedBan nicht gefunden. Verwende das eingebaute Bannsystem für SimpleProxyChat...");
            banHelper = new BanHelper(this.getDataFolder());
            banHelper.initialize();
        } else {
            config.overwrite(ConfigDataKey.USE_SIMPLE_PROXY_CHAT_BANNING_SYSTEM, false);
        }
    }

private void registerListeners() {
    // Register Discord Listener
    ChatHandler chatHandler = new ChatHandler(
            config,
            epochHelper,
            discordBot,
            (message) -> this.getProxy().broadcast(Helper.convertToBungee(message)),
            (message) -> getLogger().info(Helper.sanitize(message))
    );

    serverListener = new BungeeServerListener(this, chatHandler);
    this.getProxy().getPluginManager().registerListener(this, serverListener);

    whisperHandler = new WhisperHandler();
}

private void registerCommands() {
    this.getProxy().getPluginManager().registerCommand(this, new BungeeReloadCommand(this, config));
    this.getProxy().getPluginManager().registerCommand(this, new BungeeChatToggleCommand(this, config));
    this.getProxy().getPluginManager().registerCommand(this, new BungeeWhisperCommand(this, config, config.getAsArrayList(ConfigDataKey.WHISPER_ALIASES).toArray(new String[0])));
    this.getProxy().getPluginManager().registerCommand(this, new BungeeReplyCommand(this, config, config.getAsArrayList(ConfigDataKey.REPLY_ALIASES).toArray(new String[0])));

    // Only enable when needed.
    if (config.getAsBoolean(ConfigDataKey.USE_SIMPLE_PROXY_CHAT_BANNING_SYSTEM)) {
        this.getProxy().getPluginManager().registerCommand(this, new BungeeBanCommand(this));
        this.getProxy().getPluginManager().registerCommand(this, new BungeeUnbanCommand(this));
    }
}

private void startPluginMessaging() {
    this.getProxy().registerChannel("BungeeCord");
    this.getProxy().getPluginManager().registerListener(this, new BungeeCordPluginMessagingListener(this, serverListener));
}

private void stopPluginMessaging() {
    this.getProxy().unregisterChannel("BungeeCord");
}

@Override
public void onDisable() {
    this.getLogger().info("Das Plugin wird heruntergefahren...");
    stopPluginMessaging();
    discordBot.stop();
}
