This fork has been customised with changes that have NOT been verified!
PLEASE do not use it in a public environment!

It is a test and for me a private exercise to work with java!

Thanks to the actual developer that you have done so much preparatory work and I do not have to rewrite everything but can work with the existing code.


<p align="center">
  <img src="https://github.com/beanbeanjuice/SimpleProxyChat/blob/master/Images/Finished/SimpleProxyChat.png?raw=true" alt="SimpleProxyChat Logo"/>
</p>
<center>
Dies ist ein Plugin für Bungeecord Chat Sync, Velocity Chat Sync und Proxy Chat Sync. Es ist ein einfaches Plugin, das eine <b>globale</b> <i>serverübergreifende</i> Kommunikation und Nachrichtenübermittlung mit Unterstützung für <b>PlaceholderAPI</b>, <b>LuckPerms</b>, <b>LiteBans</b>, <b>AdvancedBan</b>, <b>NetworkManager</b> und <b>Discord</b> ermöglicht.
</center>

---

<p align="center">
  <img src="https://github.com/beanbeanjuice/SimpleProxyChat/blob/master/Images/Finished/Installation.png?raw=true" alt="installation"/>
</p>

### Mit PlaceholderAPI
1) Sie **müssen** das *Hilfs-Plugin* verwenden, das Sie [hier](https://www.spigotmc.org/resources/116966/) herunterladen können.
1) Ziehen Sie das Hilfs-Plugin auf jeden **Spigot/Paper/Bukkit**-Server, auf dem Sie PlaceholderAPI-Unterstützung wünschen.
* Sie benötigen keine zusätzliche Konfiguration auf dem Spigot/Paper/Bukkit-Server, solange Sie PlaceholderAPI installiert haben.
1) Setzen Sie `use-helper` in der `config.yml` für die Proxy-Konfiguration auf `true`. Dies wird auf Ihrem **BungeeCord/Velocity**-Server sein.
1) Starten Sie das Plugin neu oder laden Sie es neu! `/spc-reload`

> Für **PlaceholderAPI**-Unterstützung ist das Hilfs-Plugin erforderlich. Außerdem können **PlaceholderAPI-Platzhalter** nur für **Minecraft-Chatnachrichten** verwendet werden. Es funktioniert **nicht** für Beitritts-/Verlassensnachrichten.

### Ohne Discord
1) Platzieren Sie das Plugin einfach in Ihrem Ordner „Plugins“ auf **BungeeCord/Waterfall/Velocity** und starten Sie Ihren Proxy neu!

### Mit Discord
1) Gehen Sie zu discord.com/developers und wählen Sie **Neue Anwendung**. Sie können *jeden* gewünschten Namen auswählen.
1) Gehen Sie zum Abschnitt **OAuth2**.
1) Gehen Sie zum **OAuth2-URL-Generator** und geben Sie ihm den **Bot**-Bereich.
1) Wählen Sie unter **Bot-Berechtigungen**:

- Kanäle verwalten (**ERFORDERLICH**: Allgemeine Berechtigungen)
- Nachrichten senden (**ERFORDERLICH**: Textberechtigungen)
- Nachrichten verwalten (**ERFORDERLICH**: Textberechtigungen)
- Nachrichtenverlauf lesen (**ERFORDERLICH**: Textberechtigungen)
- *+ Alle anderen, die Sie hinzufügen möchten...*
1) Kopieren Sie die **Generierte URL** unten.
1) Fügen Sie die generierte URL in einen Webbrowser ein.
1) Laden Sie den Bot auf Ihren Server ein.
1) Gehen Sie zurück zu discord.com/developers.
1) Wählen Sie Ihren Bot aus.
1) Klicken Sie auf **Bot**.
1) Klicken Sie auf **Token zurücksetzen** und kopieren Sie das Token an einen sicheren Ort.
1) Scrollen Sie nach unten zu **Privileged Gateway Intents**.
1) Wählen Sie **SERVER MEMBER INTENT** und **MESSAGE CONTENT INTENT**.
1) Platzieren Sie das Plugin im Ordner `plugins` auf **BungeeCord/Velocity**.
1) Starten Sie Ihren Server einmal *vollständig* und stoppen Sie ihn dann. Es *wird* Fehler geben, das ist normal.
1) Fügen Sie in die generierte Datei `ProxyChat/config.yml` Ihr **Bot-Token** ein und wählen Sie den entsprechenden Kanal aus, an den Nachrichten gesendet werden sollen.
1) Starten Sie Ihren Proxy neu!

---

<p align="center">
  <img src="https://github.com/beanbeanjuice/SimpleProxyChat/blob/master/Images/Finished/Features.png?raw=true" alt="features"/>
</p>

* **Global Network Chat**
* **LuckPerms Support**
* **LiteBans Support**
* **AdvancedBan Support**
* **NetworkManager Support**
* **Discord Support**
* **Velocity/Waterfall/BungeeCord Support**
* **Colored Chat**
* **Cross-Server Communication**
* **PlaceholderAPI Support**
* **... und mehr!**

---

<p align="center">
  <img src="https://github.com/beanbeanjuice/SimpleProxyChat/blob/master/Images/Finished/Configuration.png?raw=true" alt="configuration"/>
</p>

**config.yml**
```YAML
# ==========================================================
#             INFORMATIONEN
# HEX-Werte werden unterstützt
# Beispiel: <#FFFFFF>Ein Text</#FFFFFF> dies ist eine Nachricht!
# Unterstützt Mini-Nachrichten/Legacy-Farbcodes
# ==========================================================

# True, wenn du Discord verwenden wirst. Der Befehl "reload" funktioniert damit nicht.
use-discord: false

# Discord Bot Token (IGNORIEREN, WENN use_discord = falsch). Der Befehl "reload" funktioniert damit nicht.
BOT-TOKEN: "TOKEN, hier rein!"

#Kanal, an den Discord-Nachrichten gesendet werden sollen (IGNORIEREN, WENN use_discord = falsch). Der Befehl "reload" funktioniert damit nicht.
CHANNEL-ID: "GLOBAL_CHANNEL_ID"

bot-activity:
   # Gültige Typen: ONLINE, DO_NOT_DISTURB, IDLE, INVISIBLE
   status: ONLINE
   # Gültige Typen: PLAYING, STREAMING, LISTENING, WATCHING, COMPETIN
   type: "COMPETING"
   text: "SimpleProxyChat by beanbeanjuice"

# Die Anzahl der Sekunden, um zu prüfen, ob ein Server online/offline ist.
# Kleinere Zahlen können Fehler verursachen. Vorsicht.
server-update-interval: 3

#Verwende dies, wenn du die Standardaliase ändern möchtest.
#Es MUSS derselbe Name sein, den du in deiner Bungee/Velocity-Konfiguration hast.
#Setze es einfach auf disabled: disabled, um es zu deaktivieren.
aliases:
   ServerInConfigExample: ServerAliasExample
   hub: Hub1
   smp: smp1

# Ob das Berechtigungssystem verwendet werden soll.
# Einige Berechtigungen (markiert mit ➕) sind immer aktiv, auch wenn dies auf false gesetzt ist.
# simpleproxychat.read.chat - Chat-Nachrichten lesen.
# simpleproxychat.read.join - Beitrittsnachrichten lesen.
# simpleproxychat.read.leave - Austrittsnachrichten lesen.
# simpleproxychat.read.fake - Fake-Beitritts-/Austrittsnachrichten lesen. Spieler müssen auch die REAL-Berechtigung für Beitritt/Austritt haben.
# simpleproxychat.read.switch - Wechselnachrichten lesen.
# simpleproxychat.read.update - Update-Nachrichten lesen. ➕
# simpleproxychat.toggle.chat - Proxy-Chat für einen einzelnen Server umschalten. ➕
# simpleproxychat.toggle.chat.all - Proxy-Chat für alle Server umschalten. ➕
# simpleproxychat.reload - Die Konfiguration neu laden. ➕
# simpleproxychat.ban - Einen Spieler vom Proxy bannen. ➕
# simpleproxychat.unban - Einen Spieler vom Proxy entbannen. ➕
# simpleproxychat.whisper - Einem anderen Spieler auf dem Proxy flüstern. ➕
use-permissions: false


# Nur Nachrichten, die mit diesem Zeichen beginnen, werden durch das Plugin gesendet.
# Auf '' setzen, um es zu deaktivieren.
# Beispiel: Wenn es auf '$' gesetzt ist, wird, wenn ein Spieler $hello sendet, dies durch den Proxy gesendet.
proxy-message-prefix: ''

# Ob die Status der an den Proxy angeschlossenen Server beim Start des Proxys gesendet werden sollen.
# DIES IST NUR FÜR DISCORD-NACHRICHTEN.
use-initial-server-status: true

# Ob eine Fake-Beitritts-/Austrittsnachricht beim Verschwinden/Wiedererscheinen gesendet werden soll.
use-fake-messages: true

# Format: https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
# Zeitzone: https://www.joda.org/joda-time/timezones.html
timestamp:
   # Wenn dein Server dazu neigt, zeitlich aus dem Takt zu geraten, kannst du eine API verwenden.
   # WARNUNG: Die Verwendung der API wird dazu führen, dass Nachrichten etwas länger zum Senden brauchen.
   # Außerdem wird die maximale Genauigkeit nur bis zu 1 Minute betragen, statt Sekunden.
   use-api: false
   format: "hh:mm a"
   timezone: "America/Los_Angeles"

# True, wenn du das Helfer-Plugin verwenden wirst.
use-helper: false

update-notifications: true

# Es wird DRINGEND empfohlen, ein robusteres Proxy-weites Banns-System wie LiteBans oder AdvancedBan zu verwenden.
# Wenn du jedoch ein leichtgewichtiges, einfaches Bannsystem bevorzugst, kannst du es hier aktivieren.
# EIN VOLLSTÄNDIGER PROXY-NEUSTART IST ERFORDERLICH, UM DIES ZU VERWENDEN.
use-simple-proxy-chat-banning-system: false

# Diese Änderungen erfordern einen Neustart, um wirksam zu werden.
commands:
   whisper-aliases:
      - "spc-msg"
   reply-aliases:
      - "spc-r"

# NICHT BERÜHREN
file-version: 12

```

**messages.yml**
```YAML
# ==========================================================
#                       INFORMATIONEN
#                 HEX-Werte werden unterstützt
#  Beispiel: <#FFFFFF>Some text</#FFFFFF> dies ist eine Nachricht!
#         Unterstützt Mini-Message/Legacy-Farbcodes
# ==========================================================

# Präfix für das Plugin. %plugin-prefix% überall verwendbar.
plugin-prefix: "&8[<bold><rainbow>SimpleProxyChat&r&8]"

# Minecraft-Einstellungen
minecraft:
   join:
      enabled: true
      message: "&e%player% &ahas hat das Netzwerk betreten. (%server%)"
   leave:
      enabled: true
      message: "&e%player% &chas hat das Netzwerk verlassen. (%server%)"
   chat:
      enabled: true
      message: "&8[&3%server%&8] &e%player% &9» &7%message%"
      vanished: "&cDu kannst keine Proxy-Nachrichten senden, während du unsichtbar bist. Deine Nachricht muss mit einem '&e/&c' enden, um zu sprechen."
   switch:
      enabled: true
      default: "&e%player% &7ist von &c%from% &7zu &a%to%&7 gewechselt."
      no-from: "&e%player% &7ist zu &a%to%&7 gewechselt."
   whisper:
      send: "&8[&dyou&8] &f⇒ &8[&d%receiver%&8] &9» &e%message%"
      receive: "&8[&d%sender%&8] &f⇒ &8[&dyou&8] &9» &e%message%"
      error: "&c/spc-whisper (Benutzer) (Nachricht)"
   discord:
      enabled: true
      message: "**%server%** %player% » %message%"
      embed:
         use: false
         title: "[%server%] %player%"
         message: "%message%"
         color: "#FFC0CB"
         use-timestamp: true
   command:
      no-permission: "%plugin-prefix% &cEntschuldigung, du hast keine Berechtigung, diesen Befehl auszuführen."
      unknown: "%plugin-prefix% &cUnbekannter Befehl."
      must-be-player: "%plugin-prefix% &cDu musst ein Spieler sein, um diesen Befehl auszuführen."
      reload: "%plugin-prefix% &aDas Plugin wurde erfolgreich neu geladen!"
      chat-lock:
         usage: "%plugin-prefix% &cDie korrekte Verwendung ist &a/spc-chat all lock/unlock &c oder &a/spc-chat lock/unlock"
         single:
            locked: "%plugin-prefix% &6%server% &cwird keine Proxy-Chat-Nachrichten mehr senden."
            unlocked: "%plugin-prefix% &6%server% &awird jetzt Proxy-Chat-Nachrichten senden."
         all:
            locked: "%plugin-prefix% &cAlle Server werden keine Proxy-Chat-Nachrichten mehr senden."
            unlocked: "%plugin-prefix% &aAlle Server werden jetzt Proxy-Chat-Nachrichten senden."
      proxy-ban:
         usage: "%plugin-prefix% &c/(un)ban (Spieler)"
         banned: "%plugin-prefix% &c%player% &7wurde gebannt."
         unbanned: "%plugin-prefix% &c%player% &7wurde entbannt."
         login-message: "&cDu wurdest vom Proxy gebannt."

# Discord-Einstellungen
discord:
   join:
      enabled: true
      message: "%player% hat das Netzwerk betreten. (%server%)"
      use-timestamp: true
   leave:
      enabled: true
      message: "%player% hat das Netzwerk verlassen. (%server%)"
      use-timestamp: true
   switch:
      enabled: true
      message: "%player% ist von %from% zu %to% gewechselt."
      use-timestamp: true
   chat:
      enabled: true
      minecraft-message: "&8[&bDiscord&8] %role% &f%user% &9» &7%message%"
   topic:
      online: "Es sind %online% Spieler online."
      offline: "Der Proxy ist offline."
   proxy-status:
      enabled: true
      messages:
         enabled: "✅ Proxy aktiviert!"
         disabled: "⛔ Proxy deaktiviert."
         title: "Serverstatus"
         message: "Status: "
         online: "Online ✅"
         offline: "Offline ⛔"
         use-timestamp: true

# Konsoleneinstellungen - Verwendet Minecraft-Nachrichten
console:
   chat: true
   join: true
   leave: true
   switch: true
   discord-chat: true
   server-status: true

# Die Nachricht für alle Updates, die gesendet werden.
# Das plugin-prefix wird automatisch an den Anfang dieser Nachricht angehängt.
update-message: "&7Es gibt ein Update! Du bist auf &c%old%. Neue Version ist &a%new%&7: &6%link%"

# NICHT BERÜHREN
file-version: 8
```
---

<p align="center">
  <img src="https://github.com/beanbeanjuice/SimpleProxyChat/blob/master/Images/Finished/Commands.png?raw=true" alt="commands"/>
</p>

* `/spc-reload` - Lädt die Konfigurationsdateien neu.
* `/spc-chat` - Sperrt/entsperrt den Chat.
* `/spc-whisper` - Sendet eine private Nachricht an jemanden.
* `/spc-reply` - Antwortet auf eine private Nachricht, ohne einen Benutzer anzugeben.


---

<p align="center">
  <img src="https://github.com/beanbeanjuice/SimpleProxyChat/blob/master/Images/Finished/Permissions.png?raw=true" alt="permissions"/>
</p>

```yaml
* `simpleproxychat.read.chat` - Lese Chatnachrichten.
* `simpleproxychat.read.join` - Lese Beitrittsnachrichten.
* `simpleproxychat.read.leave` - Lese Austrittsnachrichten.
* `simpleproxychat.read.fake` - Lese gefälschte Beitritts-/Austrittsnachrichten. Muss auch die reale Berechtigung haben.
* `simpleproxychat.read.switch` - Lese Wechselnachrichten.
* `simpleproxychat.read.update` - Lese Update-Benachrichtigungen.
* `simpleproxychat.toggle.chat` - Schalte den Proxy-Chat für einen einzelnen Server um.
* `simpleproxychat.toggle.chat.all` - Schalte den Proxy-Chat für alle Server um.
* `simpleproxychat.reload` - Lade die Konfiguration neu.
* `simpleproxychat.ban` - Jemanden bannen.
* `simpleproxychat.unban` - Jemanden entbannen.
* `simpleproxychat.whisper` - Berechtigungen für private Nachrichten.
```

---

<p align="center">
  <img src="https://github.com/beanbeanjuice/SimpleProxyChat/blob/master/Images/Finished/Placeholders.png?raw=true" alt="placeholders"/>
</p>

* `%plugin-prefix%` - Das festgelegte Präfix des Plugins.
* `%server%` - Der aktuell verbundene Server. Verwendet den Alias, falls einer angegeben ist.
* `%original_server%` - Dasselbe wie `%server%`, aber ohne den Alias.
* `%to%` - Der aktuell verbundene Server. Verwendet den Alias, falls einer angegeben ist.
* `%original_to%` - Dasselbe wie `%to%`, aber ohne den Alias.
* `%from%` - Der Server, von dem der Spieler gerade getrennt wurde. Verwendet den Alias, falls einer angegeben ist.
* `%original_from%` - Dasselbe wie `%from%`, aber ohne den Alias.
* `%player%` - Der Minecraft-Benutzername des Spielers.
* `%sender%` - (NUR FÜR PRIVATE NACHRICHTEN) Die Person, die die private Nachricht sendet.
* `%receiver%` - (NUR FÜR PRIVATE NACHRICHTEN) Die Person, die die private Nachricht erhält.
* `%user%` - Der Discord-Benutzername des Spielers.
* `%nick%` - Der Discord-Spitzname des Spielers.
* `%role%` - Die Discord-Rolle des Spielers.
* `%prefix%` - Das Präfix des Spielers. **Nur LuckPerms**
* `%suffix%` - Das Suffix des Spielers. **Nur LuckPerms**
* `%message%` - Die Nachricht des Spielers.
* `%epoch%` - Ruft die aktuelle Zeit (in Millisekunden) ab. Formate können [so](https://gist.github.com/LeviSnoot/d9147767abeef2f770e9ddcd91eb85aa) verwendet werden. Ein Beispiel wäre `<t:%epoch%>`. **Nur Discord**
* `%time%` - Ähnlich wie `%epoch%`, aber verwendet ein spezielles Format und eine Zeitzone, die in `config.yml` gefunden werden. **Discord und Minecraft**

---

<p align="center">
  <img src="https://github.com/beanbeanjuice/SimpleProxyChat/blob/master/Images/Finished/Caveats.png?raw=true" alt="caveats"/>
</p>

1) Derzeit ist die Unterstützung für das Verschwinden nur auf *BungeeCord/Waterfall* verfügbar. Das Plugin wird weiterhin normal funktionieren, aber wenn du in den Unsichtbarkeitsmodus gehst, wird keine gefälschte Beitritts-/Austrittsnachricht gesendet.
2) Damit Präfixe und Suffixe funktionieren, **muss** LuckPerms auf dem Proxy installiert sein. Dann kannst du `%prefix%` und `%suffix%` verwenden.
3) `%epoch%` und die Zeitstempel funktionieren nur an bestimmten Stellen auf Discord. Alternativ kannst du einige der Einbettungen auswählen, um `use-timestamp: true` zu haben. Dies liegt leider außerhalb meiner Kontrolle... 😔


---

<p align="center">
  <img src="https://github.com/beanbeanjuice/SimpleProxyChat/blob/master/Images/Finished/Statistics.png?raw=true" alt="statistics"/>
</p>

### Velocity Statistics
![velocity statistics](https://bstats.org/signatures/velocity/SimpleProxyChat.svg)

### BungeeCord/Waterfall Statistics
![bungeecord/waterfall statistics](https://bstats.org/signatures/bungeecord/SimpleProxyChat.svg)
