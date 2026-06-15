package net.mobilelize.essentialsMobHook;

import com.earth2me.essentials.IEssentials;
import net.mobilelize.essentialsMobHook.listener.SimpleMessageRecipientHook;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class EssentialsMobHook extends JavaPlugin {

    private final IEssentials essentials = (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(new SimpleMessageRecipientHook(essentials, this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


}
