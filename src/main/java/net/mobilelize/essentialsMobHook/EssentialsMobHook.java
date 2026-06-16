package net.mobilelize.essentialsMobHook;

import com.earth2me.essentials.IEssentials;
import net.mobilelize.essentialsMobHook.listener.PlayerQuitHook;
import net.mobilelize.essentialsMobHook.listener.SimpleMessageRecipientHook;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class EssentialsMobHook extends JavaPlugin {

    private final IEssentials essentials = (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");

    @Override
    public void onEnable() {
        saveDefaultConfig();
        final Map<UUID, Long> lastMessageMs = new ConcurrentHashMap<>();
        Bukkit.getPluginManager().registerEvents(new SimpleMessageRecipientHook(essentials, this, lastMessageMs), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitHook(lastMessageMs), this);
    }

    @Override
    public void onDisable() {}
}
