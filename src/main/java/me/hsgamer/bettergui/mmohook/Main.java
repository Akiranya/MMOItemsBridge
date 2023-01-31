package me.hsgamer.bettergui.mmohook;

import me.hsgamer.bettergui.builder.ActionBuilder;
import me.hsgamer.bettergui.builder.RequirementBuilder;
import me.hsgamer.bettergui.mmohook.action.ItemReforgeAction;
import me.hsgamer.bettergui.mmohook.requirement.ItemReforgeRequirement;
import me.hsgamer.hscore.bukkit.addon.PluginAddon;
import me.hsgamer.hscore.bukkit.config.BukkitConfig;

import java.io.File;

public final class Main extends PluginAddon {
    public static Main INSTANCE;

    public final MessageConfig messageConfig = new MessageConfig(this);
    public final BukkitConfig reforgeEcoConfig = new BukkitConfig(new File(getDataFolder(), "reforge-eco.yml"));
    public final BukkitConfig reforgeAvailableConfig = new BukkitConfig(new File(getDataFolder(), "reforge-available.yml"));

    @Override public void onEnable() {
        INSTANCE = this;
        messageConfig.setup();
        reforgeEcoConfig.setup();
        reforgeAvailableConfig.setup();
        ActionBuilder.INSTANCE.register(ItemReforgeAction::new, "mmo-reforge", "reforge");
        RequirementBuilder.INSTANCE.register(ItemReforgeRequirement::new, "mmo-reforge", "reforge");
    }

    @Override public void onReload() {
        INSTANCE = this;
        ReforgeCommon.flush();
        messageConfig.reload();
        reforgeEcoConfig.reload();
        reforgeAvailableConfig.reload();
    }

    @Override public void onDisable() {
        INSTANCE = null;
    }
}
