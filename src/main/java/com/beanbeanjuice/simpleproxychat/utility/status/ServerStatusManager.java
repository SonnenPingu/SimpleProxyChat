package com.beanbeanjuice.simpleproxychat.utility.status;

import com.beanbeanjuice.simpleproxychat.discord.Bot;
import com.beanbeanjuice.simpleproxychat.utility.helper.Helper;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.function.Consumer;


public class ServerStatusManager {

    private final Map<String, ServerStatus> servers;
    private final Config config;

    public ServerStatusManager(Config config) {
        servers = new HashMap<>(); 
        this.config = config;
    }

    public ServerStatus getStatus(String serverName) {
        servers.putIfAbsent(serverName, new ServerStatus()); // Wenn nicht vorhanden, neuen Status erstellen
        return servers.get(serverName);
    }

    public void setStatus(String serverName, boolean status) {
        servers.put(serverName, new ServerStatus(status));
    }
    public MessageEmbed getStatusEmbed(String serverName, boolean status) {
        String statusMessageString = config.getAsString(ConfigDataKey.DISCORD_PROXY_STATUS_MODULE_MESSAGE);
        String statusString = status ? config.getAsString(ConfigDataKey.DISCORD_PROXY_STATUS_MODULE_ONLINE) : config.getAsString(ConfigDataKey.DISCORD_PROXY_STATUS_MODULE_OFFLINE);

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(config.getAsString(ConfigDataKey.DISCORD_PROXY_STATUS_MODULE_TITLE));
        embedBuilder.addField(
                Helper.convertAlias(config, serverName),
                String.format("%s%s", statusMessageString, statusString),
                false);
        embedBuilder.setColor(status ? Color.GREEN : Color.RED);
        return embedBuilder.build();
    }


    public MessageEmbed getAllStatusEmbed() {
        String title = config.getAsString(ConfigDataKey.DISCORD_PROXY_STATUS_MODULE_TITLE);
        String onlineString = config.getAsString(ConfigDataKey.DISCORD_PROXY_STATUS_MODULE_ONLINE);
        String offlineString = config.getAsString(ConfigDataKey.DISCORD_PROXY_STATUS_MODULE_OFFLINE);

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(title);

        servers.forEach((serverName, serverStatus) -> {
            // Verwenden von String.format für bessere Lesbarkeit und weniger Redundanz
            String statusString = String.format("%s %s", 
                                                config.getAsString(ConfigDataKey.DISCORD_PROXY_STATUS_MODULE_MESSAGE),
                                                serverStatus.getStatus() ? onlineString : offlineString);
            embedBuilder.addField(Helper.convertAlias(config, serverName), statusString, true);
        });

        embedBuilder.setColor(Color.YELLOW);
        return embedBuilder.build();
    }

    public String getStatusString(String serverName, boolean status) {
        String statusString = status ? "online" : "offline";
        return String.format("%s is %s.", Helper.convertAlias(config, serverName), statusString);
    }
  public List<String> getAllStatusStrings() { // Verwendung von List für mehr Flexibilität
        List<String> statusStrings = new ArrayList<>();
        if (config.getAsBoolean(ConfigDataKey.CONSOLE_SERVER_STATUS)) {
            servers.forEach((serverName, serverStatus) -> 
                statusStrings.add(getStatusString(serverName, serverStatus.getStatus()))
            );
        }
        return statusStrings;
    }

    
    public void runStatusLogic(String serverName, boolean newStatus, Bot discordBot, Consumer<String> logger) {
        if (config.getAsBoolean(ConfigDataKey.PLUGIN_STARTING)) {
            this.setStatus(serverName, newStatus); // Set initial status on startup
            return; // Exit the method early on startup
        }

        ServerStatus currentStatus = this.getStatus(serverName);
        currentStatus.updateStatus(newStatus).ifPresent(isOnline -> {
            // Diese if-Bedingung ist redundant, da status bereits den aktuellen Status enthält
            // if (currentStatus.getStatus() != isOnline) { 
                if (config.getAsBoolean(ConfigDataKey.DISCORD_PROXY_STATUS_ENABLED)) {
                    discordBot.sendMessageEmbed(this.getStatusEmbed(serverName, isOnline));
                }

                if (config.getAsBoolean(ConfigDataKey.CONSOLE_SERVER_STATUS)) {
                    logger.accept(this.getStatusString(serverName, isOnline));
                }
            //}
        });
    }
}

        ServerStatus currentStatus = this.getStatus(serverName);
        currentStatus.updateStatus(newStatus).ifPresent(isOnline -> {
            if (config.getAsBoolean(ConfigDataKey.DISCORD_PROXY_STATUS_ENABLED)) {
                discordBot.sendMessageEmbed(this.getStatusEmbed(serverName, isOnline));
            }

            if (config.getAsBoolean(ConfigDataKey.CONSOLE_SERVER_STATUS)) {
                logger.accept(this.getStatusString(serverName, isOnline));
            }
        });
    }
}
