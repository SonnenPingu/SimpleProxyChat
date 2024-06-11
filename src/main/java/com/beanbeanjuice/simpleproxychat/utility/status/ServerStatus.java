package com.beanbeanjuice.simpleproxychat.utility.status;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j // Using SLF4J for logging
public class ServerStatus {

    @Getter
    private Boolean status;
    private Boolean previousStatus;

    private int onlineCount = 0;
    private int offlineCount = 0;

    private static final int COUNT_UNTIL_UPDATE = 5;

    public ServerStatus() { }

    public ServerStatus(boolean initialStatus) {
        this.status = initialStatus;
        this.previousStatus = initialStatus;
    }

    private void resetCount() {
        onlineCount = 0;
        offlineCount = 0;
    }

    public Optional<Boolean> updateStatus(Boolean newStatus) {
        if (newStatus == status) { 
            return Optional.empty(); // Keine Änderung, frühzeitig zurückgeben
        }

        if (newStatus != previousStatus) { 
            resetCount(); // Statuswechsel erkannt, Zähler zurücksetzen
        }
        previousStatus = newStatus;

        int count = newStatus ? ++onlineCount : ++offlineCount;
        if (count < COUNT_UNTIL_UPDATE) {
            return Optional.empty(); // Noch nicht genug Bestätigungen
        }

        resetCount(); 
        status = newStatus;
        log.info("Serverstatusänderung erkannt: {} -> {}", !newStatus, newStatus); // Statusänderung protokollieren
        return Optional.of(status);
    }
}
