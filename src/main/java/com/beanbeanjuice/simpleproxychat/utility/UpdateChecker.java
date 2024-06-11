package com.beanbeanjuice.simpleproxychat.utility;

import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.beanbeanjuice.simpleproxychat.utility.helper.Helper;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateChecker {

    private final Config config;
    private final String currentVersion;
    private final Consumer<String> notifyFunction;

    public UpdateChecker(final Config config, final String currentVersion, final Consumer<String> notifyFunction) {
        this.config = config;
        this.currentVersion = currentVersion;
        this.notifyFunction = notifyFunction;
    }
private static final String SPIGOT_UPDATE_URL = "https://api.spigotmc.org/legacy/update.php?resource=115305";

    public Optional<String> getUpdate() {
        try (InputStream is = URI.create(SPIGOT_UPDATE_URL).toURL().openStream(); 
             Scanner scanner = new Scanner(is)) {
            
            return scanner.hasNext() ? Optional.of(scanner.next()) : Optional.empty();

        } catch (IOException | URISyntaxException e) {
            config.getLogger().log(Level.WARNING, "Fehler bei der Update-Prüfung:", e); // Fehler protokollieren
            return Optional.empty();
        }
    }

    public void checkUpdate() {
        getUpdate().ifPresentOrElse(
            spigotVersion -> {
                if (compare(currentVersion, spigotVersion) < 0) { // Versionen vergleichen
                    String message = Helper.replaceKeys(
                        config.getAsString(ConfigDataKey.UPDATE_MESSAGE),
                        Tuple.of("plugin-prefix", config.getAsString(ConfigDataKey.PLUGIN_PREFIX)),
                        Tuple.of("old", currentVersion),
                        Tuple.of("new", spigotVersion),
                        Tuple.of("link", "https://www.spigotmc.org/resources/115305/")
                    );
                    notifyFunction.accept(message);
                }
            },
            () -> config.getLogger().log(Level.WARNING, "Update-Prüfung fehlgeschlagen.") 
        );
    }

    public static int compare(final String version1, final String version2) {
        DefaultArtifactVersion v1 = new DefaultArtifactVersion(version1);
        DefaultArtifactVersion v2 = new DefaultArtifactVersion(version2);

        return v1.compareTo(v2);
    }
}
