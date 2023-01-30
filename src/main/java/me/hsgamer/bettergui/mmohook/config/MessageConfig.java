package me.hsgamer.bettergui.mmohook.config;

import me.hsgamer.hscore.bukkit.addon.PluginAddon;
import me.hsgamer.hscore.bukkit.config.BukkitConfig;
import me.hsgamer.hscore.config.annotated.AnnotatedConfig;
import me.hsgamer.hscore.config.annotation.ConfigPath;

import java.io.File;

public class MessageConfig extends AnnotatedConfig {

    public final @ConfigPath("not-enough-ingredient") String notEnoughIngredient;
    public final @ConfigPath("reforge-failed") String reforgeFailed;
    public final @ConfigPath("reforge-succeeded") String reforgeSucceeded;

    public MessageConfig(final PluginAddon addon) {
        super(new BukkitConfig(new File(addon.getDataFolder(), "message.yml")));

        notEnoughIngredient = "<red>重铸失败! 本次重铸需要这些: <ingredient>";
        reforgeFailed = "<red>重铸失败! 哥布林只能重铸服务器特有的RPG武器和装备";
        reforgeSucceeded = "<green>重铸成功!";
    }
}
