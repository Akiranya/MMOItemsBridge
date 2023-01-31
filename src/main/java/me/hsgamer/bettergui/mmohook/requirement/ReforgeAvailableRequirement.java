package me.hsgamer.bettergui.mmohook.requirement;

import cc.mewcraft.mewcore.item.api.PluginItem;
import cc.mewcraft.mewcore.item.api.PluginItemRegistry;
import cc.mewcraft.mewcore.util.UtilInventory;
import me.hsgamer.bettergui.api.requirement.BaseRequirement;
import me.hsgamer.bettergui.builder.RequirementBuilder;
import me.hsgamer.bettergui.mmohook.ReforgeCommon;
import me.hsgamer.bettergui.util.StringReplacerApplier;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

/**
 * Checks whether the item in the specific slot can be reforged, i.e., it is defined in the reforge config file.
 */
public class ReforgeAvailableRequirement extends BaseRequirement<String> {

    public ReforgeAvailableRequirement(final RequirementBuilder.Input input) {
        super(input);
    }

    @Override protected String convert(final Object value, final UUID uuid) {
        return StringReplacerApplier.replace(String.valueOf(value).trim(), uuid, this);
    }

    @Override protected Result checkConverted(final UUID uuid, final String value) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return Result.success();
        }
        PlayerInventory inventory = player.getInventory();
        ItemStack itemInSlot = UtilInventory.getItemInSlot(inventory, value, null);
        final PluginItem<?> slotPi = PluginItemRegistry.get().fromItemStackNullable(itemInSlot);
        return ReforgeCommon.isReforgeAvailable(itemInSlot, slotPi)
            ? Result.success()
            : Result.fail();
    }

}
