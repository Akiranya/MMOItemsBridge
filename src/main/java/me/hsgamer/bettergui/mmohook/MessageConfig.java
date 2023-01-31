package me.hsgamer.bettergui.mmohook;

import me.hsgamer.hscore.bukkit.addon.PluginAddon;
import me.hsgamer.hscore.bukkit.config.BukkitConfig;
import me.hsgamer.hscore.config.annotated.AnnotatedConfig;
import me.hsgamer.hscore.config.annotation.ConfigPath;

import java.io.File;

public class MessageConfig extends AnnotatedConfig {

    public final @ConfigPath("view-item-cost") String viewItemCost;
    public final @ConfigPath("not-enough-items") String notEnoughItems;
    public final @ConfigPath("reforge-failed") String reforgeFailed;
    public final @ConfigPath("view-failed") String viewFailed;
    public final @ConfigPath("reforge-succeeded") String reforgeSucceeded;
    public final @ConfigPath("internal-error") String internalError;
    public final @ConfigPath("reforge-air") String reforgeAir;

    public MessageConfig(final PluginAddon addon) {
        super(new BukkitConfig(new File(addon.getDataFolder(), "message.yml")));

        viewItemCost = "<gray>重铸所需材料 <white><item_cost>";
        notEnoughItems = "<gray>重铸失败！重铸需要材料 <white><item_cost>";
        reforgeFailed = "<gray>重铸失败! 哥布林只能重铸部分RPG武器和装备";
        reforgeSucceeded = "<green>重铸成功！";
        internalError = "<red>系统发生内部错误，请反馈到上古茶馆";
        reforgeAir = "<gray>你不能重铸空气！";
        viewFailed = "<gray>该装备无法重铸！";
    }
}
