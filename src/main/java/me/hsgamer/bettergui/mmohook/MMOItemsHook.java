package me.hsgamer.bettergui.mmohook;

import me.hsgamer.bettergui.builder.ActionBuilder;
import me.hsgamer.bettergui.mmohook.action.ItemReforgeAction;
import me.hsgamer.bettergui.mmohook.config.MessageConfig;
import me.hsgamer.hscore.bukkit.addon.PluginAddon;
import me.hsgamer.hscore.bukkit.config.BukkitConfig;
import me.hsgamer.hscore.config.Config;

import java.io.File;

public final class MMOItemsHook extends PluginAddon {

    private final Config reforgeEcoConfig = new BukkitConfig(new File(getDataFolder(), "reforge-eco.yml"));
    private final Config reforgeAvailableConfig = new BukkitConfig(new File(getDataFolder(), "reforge-available.yml"));
    private final MessageConfig messageConfig = new MessageConfig(this);

    @Override public void onEnable() {
        ActionBuilder.INSTANCE.register(input -> new ItemReforgeAction(this, input), "mmo-reforge");
    }

    public Config getReforgeEcoConfig() {
        return reforgeEcoConfig;
    }

    public Config getReforgeAvailableConfig() {
        return reforgeAvailableConfig;
    }

    public MessageConfig getMessageConfig() {
        return messageConfig;
    }
}
