package com.beanbeanjuice.simpleproxychat;

import com.beanbeanjuice.simpleproxychat.commands.velocity.VelocityChatToggleCommand;
import com.beanbeanjuice.simpleproxychat.commands.velocity.VelocityReloadCommand;
import com.beanbeanjuice.simpleproxychat.commands.velocity.VelocityReplyCommand;
import com.beanbeanjuice.simpleproxychat.commands.velocity.VelocityWhisperCommand;
import com.beanbeanjuice.simpleproxychat.commands.velocity.ban.VelocityBanCommand;
import com.beanbeanjuice.simpleproxychat.commands.velocity.ban.VelocityUnbanCommand;
import com.beanbeanjuice.simpleproxychat.commands.velocity.*;
import com.beanbeanjuice.simpleproxychat.socket.velocity.VelocityPluginMessagingListener;
import com.beanbeanjuice.simpleproxychat.utility.BanHelper;
import com.beanbeanjuice.simpleproxychat.utility.UpdateChecker;
import com.beanbeanjuice.simpleproxychat.utility.helper.WhisperHandler;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import com.beanbeanjuice.simpleproxychat.utility.epoch.EpochHelper;
import com.beanbeanjuice.simpleproxychat.utility.status.ServerStatusManager;
import com.google.inject.Inject;
import com.beanbeanjuice.simpleproxychat.chat.ChatHandler;
import com.beanbeanjuice.simpleproxychat.utility.listeners.velocity.VelocityServerListener;
import com.beanbeanjuice.simpleproxychat.discord.Bot;
import com.beanbeanjuice.simpleproxychat.utility.helper.Helper;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import de.myzelyam.api.vanish.VelocityVanishAPI;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class SimpleProxyChatVelocity {

    private final Metrics.Factory metricsFactory;

    @Getter private final ProxyServer proxyServer;
    @Getter private final Logger logger;
    @Getter private final Config config;
    @Getter private final EpochHelper epochHelper;
    @Getter private Bot discordBot;
    @Getter private WhisperHandler whisperHandler;
    @Getter private BanHelper banHelper;
    private Metrics metrics;
    private VelocityServerListener serverListener;

    private final File dataDirectory;

    @Inject
    public SimpleProxyChatVelocity(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory, Metrics.Factory metricsFactory) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.metricsFactory = metricsFactory;

        this.getLogger().info("The plugin is starting.");
        this.dataDirectory = dataDirectory.toFile();
        this.config = new Config(dataDirectory.toFile());
        this.config.initialize();

        epochHelper = new EpochHelper(config);

        // Plugin enabled.
        this.getLogger().info("Plugin wurde erfolgreich geladen.");
    }

    @Subscribe(order = PostOrder.LAST)
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // Initialize discord bot.
        this.getLogger().info("Versuch, Discord-Bot zu initialisieren... (falls aktiviert)");
        discordBot = new Bot(this.config);

        // Bot ready.
        this.proxyServer.getScheduler().buildTask(this, () -> {
            try { discordBot.start();
            } catch (Exception e) { this.getLogger().warn("Es gab einen Fehler beim Starten des Discord-Bots: {}", e.getMessage()); }
        }).schedule();

        hookPlugins();
        registerListeners();
        registerCommands();

        // Start Channel Topic Updater
        this.proxyServer.getScheduler()
                .buildTask(this, () -> {
                    int numPlayers = proxyServer.getPlayerCount();

                    if (config.getAsBoolean(ConfigDataKey.VANISH_ENABLED))
                        numPlayers = (int) proxyServer.getAllPlayers().stream()
                                .filter((player) -> !VelocityVanishAPI.isInvisible(player))
                                .count();

                    discordBot.channelUpdaterFunction(numPlayers);
                })
                .delay(5, TimeUnit.MINUTES)
                .repeat(5, TimeUnit.MINUTES)
                .schedule();

        // Start Update Checker
        startUpdateChecker();

        // bStats Stuff
        this.getLogger().info("Starting bStats... (IF ENABLED)");
        this.metrics = metricsFactory.make(this, 21147);

        // Plugin has started.
        this.getLogger().info("The plugin has been started.");

        // Send initial server status.
        discordBot.addRunnableToQueue(() -> {
            this.getProxyServer().getScheduler().buildTask(this, () -> {
                this.config.overwrite(ConfigDataKey.PLUGIN_STARTING, false);

                ServerStatusManager manager = serverListener.getServerStatusManager();
                manager.getAllStatusStrings().forEach(this.getLogger()::info);

                if (!config.getAsBoolean(ConfigDataKey.USE_INITIAL_SERVER_STATUS)) return;
                if (!config.getAsBoolean(ConfigDataKey.DISCORD_PROXY_STATUS_ENABLED)) return;
                discordBot.sendMessageEmbed(manager.getAllStatusEmbed());
            })
            .delay(config.getAsInteger(ConfigDataKey.SERVER_UPDATE_INTERVAL) * 2L, TimeUnit.SECONDS)
            .schedule();
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
        // Register chat listener.
        ChatHandler chatHandler = new ChatHandler(
                config,
                epochHelper,
                discordBot,
                (message) -> {
                    logger.info(Helper.sanitize(message));
                    Component messageComponent = MiniMessage.miniMessage().deserialize(message);
                    proxyServer.getAllPlayers().forEach((player) -> player.sendMessage(messageComponent));
                },
                (message) -> logger.info(Helper.sanitize(message))
        );
        serverListener = new VelocityServerListener(this, chatHandler);
        serverListener.initializeVelocityVanishListener();
        this.proxyServer.getEventManager().register(this, serverListener);

        this.proxyServer.getEventManager().register(this, new VelocityPluginMessagingListener(this, serverListener));
        this.proxyServer.getChannelRegistrar().register(VelocityPluginMessagingListener.IDENTIFIER);

        whisperHandler = new WhisperHandler();
    }

    private void registerCommands() {
        CommandManager commandManager = proxyServer.getCommandManager();

        CommandMeta reloadCommand = commandManager.metaBuilder("spc-reload")
                .aliases("spcreload")
                .plugin(this)
                .build();

        CommandMeta chatToggleCommand = commandManager.metaBuilder("spc-chat")
                .aliases("spcchat")
                .plugin(this)
                .build();

        CommandMeta whisperCommand = commandManager.metaBuilder("spc-whisper")
                .aliases(config.getAsArrayList(ConfigDataKey.WHISPER_ALIASES).toArray(new String[0]))
                .plugin(this)
                .build();

        CommandMeta replyCommand = commandManager.metaBuilder("spc-reply")
                .aliases(config.getAsArrayList(ConfigDataKey.REPLY_ALIASES).toArray(new String[0]))
                .plugin(this)
                .build();

        CommandMeta banCommand = commandManager.metaBuilder("spc-ban")
                .aliases("spcban")
                .plugin(this)
                .build();

        CommandMeta unbanCommand = commandManager.metaBuilder("spc-unban")
                .aliases("spcunban")
                .plugin(this)
                .build();

        commandManager.register(reloadCommand, new VelocityReloadCommand(this));
        commandManager.register(chatToggleCommand, new VelocityChatToggleCommand(this));
        commandManager.register(whisperCommand, new VelocityWhisperCommand(this));
        commandManager.register(replyCommand, new VelocityReplyCommand(this));

        // Only enable if the Simple Banning System is enabled.
        if (config.getAsBoolean(ConfigDataKey.USE_SIMPLE_PROXY_CHAT_BANNING_SYSTEM)) {
            commandManager.register(banCommand, new VelocityBanCommand(this));
            commandManager.register(unbanCommand, new VelocityUnbanCommand(this));
        }
    }

    @Subscribe(order = PostOrder.LAST)
    public void onProxyShutdown(ProxyShutdownEvent event) {
        this.getLogger().info("Das Plugin schaltet sich ab...");
        discordBot.stop();
    }

}
