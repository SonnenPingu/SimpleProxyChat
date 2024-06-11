package com.beanbeanjuice.simpleproxychat.utility;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class BanHelper {
    private YamlDocument yamlBans;
    private final File configFolder;
    @Getter
    private final List<String> bannedPlayers; // Verwende List statt ArrayList für Flexibilität

    public BanHelper(File configFolder) {
        this.configFolder = configFolder;
        this.bannedPlayers = new ArrayList<>();
    }

    public void initialize() {
        try {
            yamlBans = loadBans("bannedPlayers.yml");
            yamlBans.update();
            yamlBans.save();
            readBans();
        } catch (IOException e) {
            config.getLogger().log(Level.SEVERE, "Fehler beim Laden der Bannliste:", e); // Fehler protokollieren
        }
    }

    public void reload() {
        try {
            yamlBans.reload();
            bannedPlayers.clear();
            readBans();
        } catch (IOException e) {
            config.getLogger().log(Level.SEVERE, "Fehler beim erneuten Laden der Bannliste:", e);
        }
    }

    public void addBan(String playerName) {
        try {
            // Keine Notwendigkeit, die Liste erneut zu laden, da sie intern aktualisiert wird
            bannedPlayers.add(playerName.toLowerCase()); // Konsistenz: Kleinbuchstaben verwenden

            yamlBans.set("bannedPlayers", bannedPlayers); // Direkte Zuweisung der Liste
            yamlBans.save();
        } catch (IOException e) {
            config.getLogger().log(Level.SEVERE, "Fehler beim Hinzufügen eines Banns:", e);
        }
    }

    public void removeBan(String playerName) {
        try {
            // Konsistenz: Kleinbuchstaben verwenden beim Entfernen
            bannedPlayers.removeIf(nameInArray -> nameInArray.equalsIgnoreCase(playerName)); 

            yamlBans.set("bannedPlayers", bannedPlayers);
            yamlBans.save();
        } catch (IOException e) {
            config.getLogger().log(Level.SEVERE, "Fehler beim Entfernen eines Banns:", e);
        }
    }

    public boolean isBanned(String playerName) {
        return bannedPlayers.contains(playerName.toLowerCase()); // Direkter Vergleich (schneller)
    }

    private void readBans() throws IOException {
        bannedPlayers.addAll(yamlBans.getStringList("bannedPlayers"));

        // Konvertiere alle Namen in Kleinbuchstaben, um die Suche zu vereinfachen
        for (int i = 0; i < bannedPlayers.size(); i++) {
            bannedPlayers.set(i, bannedPlayers.get(i).toLowerCase());
        }
    }

    private YamlDocument loadBans(String fileName) throws IOException {
        return YamlDocument.create(
                new File(configFolder, fileName),
                Objects.requireNonNull(getClass().getResourceAsStream("/" + fileName)),
                GeneralSettings.DEFAULT,
                DumperSettings.DEFAULT
        );
    }

}
