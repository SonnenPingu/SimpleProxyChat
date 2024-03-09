package com.beanbeanjuice.simpleproxychat.utility.config;

public enum ConfigDataKey {
    // CONFIG
    USE_DISCORD,
    BOT_TOKEN,
    CHANNEL_ID,
    SERVER_UPDATE_INTERVAL,
    ALIASES,

    // MESSAGES
    MINECRAFT_JOIN,
    MINECRAFT_LEAVE,
    MINECRAFT_MESSAGE,
    MINECRAFT_DISCORD_MESSAGE,
    MINECRAFT_SWITCH_DEFAULT,
    MINECRAFT_SWITCH_SHORT,

    DISCORD_JOIN,
    DISCORD_LEAVE,
    DISCORD_SWITCH,
    DISCORD_MINECRAFT_MESSAGE,
    DISCORD_PROXY_ENABLED,
    DISCORD_PROXY_DISABLED,
    DISCORD_PROXY_TITLE,
    DISCORD_PROXY_MESSAGE,
    DISCORD_PROXY_STATUS_ONLINE,
    DISCORD_PROXY_STATUS_OFFLINE,

    // External
    VANISH_ENABLED,
    LUCKPERMS_ENABLED
}
