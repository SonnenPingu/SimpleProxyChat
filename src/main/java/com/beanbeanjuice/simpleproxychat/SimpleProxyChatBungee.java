package com.beanbeanjuice.simpleproxychat;

import com.beanbeanjuice.simpleproxychat.commands.bungee.BungeeReloadCommand;
import com.beanbeanjuice.simpleproxychat.utility.Helper;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import com.beanbeanjuice.simpleproxychat.utility.listeners.bungee.BungeeServerListener;
import com.beanbeanjuice.simpleproxychat.utility.listeners.bungee.BungeeVanishListener;
import com.beanbeanjuice.simpleproxychat.chat.ChatHandler;
import com.beanbeanjuice.simpleproxychat.discord.Bot;
import com.beanbeanjuice.simpleproxychat.utility.UpdateChecker;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataEntry;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.beanbeanjuice.simpleproxychat.utility.status.ServerStatusManager;
import de.myzelyam.api.vanish.BungeeVanishAPI;
import de.myzelyam.api.vanish.VanishAPI;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import org.bstats.bungeecord.Metrics;

import java.awt.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public final class SimpleProxyChatBungee extends Plugin {

    @Getter private Config config;
    @Getter private Bot discordBot;
    @Getter private Metrics metrics;
    private BungeeServerListener serverListener;

    @Override
    public void onEnable() {
        this.getLogger().info("The plugin is starting.");

        this.config = new Config(this.getDataFolder());
        this.config.initialize();

        this.getLogger().info("Initializing discord bot.");

        try { discordBot = new Bot(this.config); }
        catch (Exception e) { getLogger().warning("There was an error starting the discord bot: " + e.getMessage()); }
        discordBot.getJDA().ifPresentOrElse((jda) -> { }, () -> getLogger().warning("Discord logging is not enabled."));
        discordBot.start();

        hookPlugins();
        registerListeners();
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
        this.getProxy().getScheduler().schedule(this, () -> UpdateChecker.checkUpdate((spigotMCVersion) -> {
            if (this.getDescription().getVersion().equals(spigotMCVersion)) return;

            String message = "ATTENTION - There is a new update available: v" + spigotMCVersion;

            Component minimessage = MiniMessage.miniMessage().deserialize(message);

            this.getLogger().info(message);
            this.getProxy().getPlayers()
                    .stream()
                    .filter((player) -> player.hasPermission(Permission.READ_UPDATE_NOTIFICATION.getPermissionNode()))
                    .forEach((player) -> player.sendMessage(ChatMessageType.CHAT, BungeeComponentSerializer.get().serialize(minimessage)));
        }), 0, 12, TimeUnit.HOURS);

        // bStats Stuff
        this.getLogger().info("Starting bStats... (IF ENABLED)");
        int pluginId = 21146;
        this.metrics = new Metrics(this, pluginId);

        // Plugin has started.
        this.getLogger().log(Level.INFO, "The plugin has been started.");

        // Send initial status.
        this.getProxy().getScheduler().schedule(this, () -> {
            this.config.overwrite(ConfigDataKey.PLUGIN_STARTING, new ConfigDataEntry(false));

            ServerStatusManager manager = serverListener.getServerStatusManager();
            manager.getAllStatusStrings().forEach((string) -> this.getLogger().info(string));

            if (!config.getAsBoolean(ConfigDataKey.USE_INITIAL_SERVER_STATUS)) return;
            this.discordBot.sendMessageEmbed(manager.getAllStatusEmbed());
        }, config.getAsInteger(ConfigDataKey.SERVER_UPDATE_INTERVAL) * 2L, TimeUnit.SECONDS);
    }

    private void hookPlugins() {
        PluginManager pm = this.getProxy().getPluginManager();

        // Enable vanish support.
        if (pm.getPlugin("PremiumVanish") != null || pm.getPlugin("SuperVanish") != null) {
            this.config.overwrite(ConfigDataKey.VANISH_ENABLED, new ConfigDataEntry(true));
            this.getLogger().log(Level.INFO, "Enabled PremiumVanish/SuperVanish Support");
            this.getProxy().getPluginManager().registerListener(this, new BungeeVanishListener(serverListener, config));
        }

        // Registering LuckPerms support.
        if (pm.getPlugin("LuckPerms") != null) {
            config.overwrite(ConfigDataKey.LUCKPERMS_ENABLED, new ConfigDataEntry(true));
            getLogger().info("LuckPerms support has been enabled.");
        }

        // Registering LiteBans support.
        if (pm.getPlugin("LiteBans") != null) {
            config.overwrite(ConfigDataKey.LITEBANS_ENABLED, new ConfigDataEntry(true));
            getLogger().info("LiteBans support has been enabled.");
        }
    }

    private void registerListeners() {
        ChatHandler chatHandler = new ChatHandler(
                config,
                discordBot,
                (message) -> {
                    Component minimessage = MiniMessage.miniMessage().deserialize(message);
                    this.getProxy().broadcast(BungeeComponentSerializer.get().serialize(minimessage));
                },
                (message) -> getLogger().info(Helper.sanitize(message))
        );

        serverListener = new BungeeServerListener(this, chatHandler);
        this.getProxy().getPluginManager().registerListener(this, serverListener);
    }

    private void registerCommands() {
        this.getProxy().getPluginManager().registerCommand(this, new BungeeReloadCommand(config));
    }

    @Override
    public void onDisable() {
        this.getLogger().info("The plugin is shutting down...");
        discordBot.stop();
    }

}
