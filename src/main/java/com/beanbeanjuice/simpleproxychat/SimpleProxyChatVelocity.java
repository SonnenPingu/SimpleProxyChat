package com.beanbeanjuice.simpleproxychat;

import com.beanbeanjuice.simpleproxychat.utility.UpdateChecker;
import com.beanbeanjuice.simpleproxychat.utility.status.ServerStatusManager;
import com.google.inject.Inject;
import com.beanbeanjuice.simpleproxychat.chat.ChatHandler;
import com.beanbeanjuice.simpleproxychat.utility.listeners.velocity.VelocityServerListener;
import com.beanbeanjuice.simpleproxychat.discord.Bot;
import com.beanbeanjuice.simpleproxychat.utility.Helper;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataEntry;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.awt.*;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class SimpleProxyChatVelocity {

    private final Metrics.Factory metricsFactory;

    @Getter private final ProxyServer proxyServer;
    @Getter private final Logger logger;
    @Getter private final Config config;
    @Getter private Bot discordBot;
    private Metrics metrics;

    @Inject
    public SimpleProxyChatVelocity(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory, Metrics.Factory metricsFactory) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.metricsFactory = metricsFactory;

        this.logger.info("The plugin is starting.");

        this.config = new Config(dataDirectory.toFile());
        this.config.initialize();

        PluginManager pm = this.proxyServer.getPluginManager();

        // Enable vanish support.
        if (pm.getPlugin("PremiumVanish").isPresent() || pm.getPlugin("SuperVanish").isPresent()) {
            this.config.overwrite(ConfigDataKey.VANISH_ENABLED, new ConfigDataEntry(true));
            this.logger.info("Enabled PremiumVanish/SuperVanish Support");
        }

        this.logger.info("Initializing discord bot.");

        try { discordBot = new Bot(this.config); }
        catch (Exception e) { logger.warn("There was an error starting the discord bot: " + e.getMessage()); }

        discordBot.getJDA().ifPresentOrElse((jda) -> { }, () -> this.logger.warn("Discord logging is not enabled."));

        discordBot.sendMessageEmbed(
                new EmbedBuilder()
                        .setTitle(config.getAsString(ConfigDataKey.DISCORD_PROXY_ENABLED))
                        .setColor(Color.GREEN)
                        .build()
        );

        this.logger.info("Plugin has been initialized.");
    }

    @Subscribe(order = PostOrder.LAST)
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // Registering LuckPerms support.
        if (this.proxyServer.getPluginManager().getPlugin("luckperms").isPresent()) {
            try {
                config.overwrite(ConfigDataKey.LUCKPERMS_ENABLED, new ConfigDataEntry(true));
                getLogger().info("LuckPerms support has been enabled.");
            } catch (IllegalStateException e) {
                getLogger().info("Error Enabling LuckPerms: " + e.getMessage());
            }
        }

        // Register Chat Listener
        ChatHandler chatHandler = new ChatHandler(
                config,
                discordBot,
                (message) -> {
                    Component messageComponent = MiniMessage.miniMessage().deserialize(message);
                    logger.info(Helper.stripColor(messageComponent));
                    proxyServer.getAllPlayers().forEach((player) -> player.sendMessage(messageComponent));
                },
                (message) -> getLogger().info(message)
        );
        VelocityServerListener serverListener = new VelocityServerListener(this, chatHandler);
        this.proxyServer.getEventManager().register(this, serverListener);

        // Start Channel Topic Updater
        this.proxyServer.getScheduler()
                .buildTask(this, () -> discordBot.channelUpdaterFunction(proxyServer.getPlayerCount()))
                .delay(5, TimeUnit.MINUTES)
                .repeat(5, TimeUnit.MINUTES)
                .schedule();

        // Start Update Checker
        this.proxyServer.getScheduler()
                .buildTask(
                        this,
                        () -> UpdateChecker.checkUpdate(
                                (spigotMCVersion) -> this.proxyServer.getPluginManager().getPlugin("simpleproxychat")
                                        .flatMap(
                                                pluginContainer -> pluginContainer.getDescription().getVersion()
                                        ).ifPresent((version) -> {
                                                    if (!version.equalsIgnoreCase(spigotMCVersion))
                                                        this.logger.info("ATTENTION - There is a new update available: v" + spigotMCVersion);
                                                }
                                        )
                        )
                ).delay(0, TimeUnit.MINUTES).repeat(12, TimeUnit.HOURS).schedule();

        // bStats Stuff
        this.logger.info("Starting bStats... (IF ENABLED)");
        int pluginId = 21147;
        this.metrics = metricsFactory.make(this, pluginId);

        // Plugin has started.
        this.getLogger().info("The plugin has been started.");

        this.getProxyServer().getScheduler().buildTask(this, () -> {
            this.config.overwrite(ConfigDataKey.PLUGIN_STARTING, new ConfigDataEntry(false));

            ServerStatusManager manager = serverListener.getServerStatusManager();
            manager.getAllStatusStrings().forEach(this.logger::info);

            if (!config.getAsBoolean(ConfigDataKey.USE_INITIAL_SERVER_STATUS)) return;
            discordBot.sendMessageEmbed(manager.getAllStatusEmbed());
        }).delay(config.getAsInteger(ConfigDataKey.SERVER_UPDATE_INTERVAL) * 2L, TimeUnit.SECONDS).schedule();
    }

    @Subscribe(order = PostOrder.LAST)
    public void onProxyShutdown(ProxyShutdownEvent event) {
        discordBot.sendMessageEmbed(
                new EmbedBuilder()
                        .setTitle(config.getAsString(ConfigDataKey.DISCORD_PROXY_DISABLED))
                        .setColor(Color.RED)
                        .build()
        );

        discordBot.getJDA().ifPresent((jda) -> {
            try {
                jda.shutdown();
                if (!jda.awaitShutdown(Duration.ofSeconds(10))) {
                    jda.shutdownNow(); // Cancel all remaining requests
                    jda.awaitShutdown(); // Wait until shutdown is complete (indefinitely)
                }
            } catch (InterruptedException ignored) { }
        });
    }

}
