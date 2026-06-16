package net.mobilelize.essentialsMobHook.listener;

import net.ess3.api.events.PrivateMessageSentEvent;
import net.mobilelize.essentialsMobHook.EssentialsMobHook;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class PlayerMessageSoundHook implements Listener {

    private final EssentialsMobHook plugin;

    public PlayerMessageSoundHook(EssentialsMobHook plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrivateMessage(PrivateMessageSentEvent event) {
        if (!event.getResponse().isSuccess()) return;

        final UUID uuid = event.getRecipient().getUUID();
        if (uuid == null) return;

        final Player player = plugin.getServer().getPlayer(uuid);
        if (player == null
                //|| !player.hasPermission("mobilelize.privatemsgbeep")
        ) return;

        player.playSound(player, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 0.5f);
    }
}
