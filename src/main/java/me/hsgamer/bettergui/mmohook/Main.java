package me.hsgamer.bettergui.mmohook;

import me.hsgamer.bettergui.builder.ActionBuilder;
import me.hsgamer.bettergui.builder.ItemModifierBuilder;
import me.hsgamer.bettergui.builder.RequirementBuilder;
import me.hsgamer.bettergui.mmohook.action.ItemReforgeAction;
import me.hsgamer.bettergui.mmohook.modifier.ReforgeInfoModifier;
import me.hsgamer.bettergui.mmohook.requirement.ReforgeAvailableRequirement;
import me.hsgamer.bettergui.mmohook.requirement.ReforgeCostRequirement;
import me.hsgamer.hscore.bukkit.addon.PluginAddon;
import me.hsgamer.hscore.bukkit.config.BukkitConfig;

import java.io.File;

public final class Main extends PluginAddon {
    public static Main INSTANCE;

    public final MessageConfig messageConfig = new MessageConfig(this);
    public final BukkitConfig reforgeSettings = new BukkitConfig(new File(getDataFolder(), "reforge-settings.yml"));
    public final BukkitConfig reforgeAvailable = new BukkitConfig(new File(getDataFolder(), "reforge-available.yml"));

    @Override public void onEnable() {
        INSTANCE = this;
        messageConfig.setup();
        reforgeSettings.setup();
        reforgeAvailable.setup();
        ActionBuilder.INSTANCE.register(ItemReforgeAction::new, "mmo-reforge", "reforge");
        ItemModifierBuilder.INSTANCE.register(ReforgeInfoModifier::new, "mmo-reforge-cost-lore", "reforge-cost-lore");
        RequirementBuilder.INSTANCE.register(ReforgeAvailableRequirement::new, "mmo-reforge-available", "reforge-available");
        RequirementBuilder.INSTANCE.register(ReforgeCostRequirement::new, "mmo-reforge-item-cost", "reforge-item-cost");
    }

    @Override public void onReload() {
        INSTANCE = this;
        ReforgeCommon.flush();
        messageConfig.reload();
        reforgeSettings.reload();
        reforgeAvailable.reload();
    }

    @Override public void onDisable() {
        INSTANCE = null;
    }
}
