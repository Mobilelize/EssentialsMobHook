package net.mobilelize.essentialsMobHook.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;

public class PlayerQuitHook implements Listener {

    private final Map<UUID, Long> lastMessageMs;

    public PlayerQuitHook(Map<UUID, Long> lastMessageMs) {
        this.lastMessageMs = lastMessageMs;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastMessageMs.remove(event.getPlayer().getUniqueId());
    }
}
