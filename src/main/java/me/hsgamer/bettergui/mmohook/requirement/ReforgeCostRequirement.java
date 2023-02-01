package me.hsgamer.bettergui.mmohook.requirement;

import cc.mewcraft.mewcore.item.api.PluginItem;
import cc.mewcraft.mewcore.item.api.PluginItemRegistry;
import cc.mewcraft.mewcore.util.UtilComponent;
import cc.mewcraft.mewcore.util.UtilInventory;
import me.hsgamer.bettergui.api.requirement.TakableRequirement;
import me.hsgamer.bettergui.builder.RequirementBuilder;
import me.hsgamer.bettergui.mmohook.Main;
import me.hsgamer.bettergui.mmohook.ReforgeCommon;
import me.hsgamer.bettergui.util.StringReplacerApplier;
import me.hsgamer.hscore.bukkit.utils.ItemUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Checks whether the player has enough items to reforge.
 */
public class ReforgeCostRequirement extends TakableRequirement<String> {

    public ReforgeCostRequirement(final RequirementBuilder.Input input) {
        super(input);
    }

    @Override protected String convert(final Object value, final UUID uuid) {
        return StringReplacerApplier.replace(String.valueOf(value).trim(), uuid, this);
    }

    @Override protected Result checkConverted(final UUID uuid, final String value) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return Result.fail();

        PlayerInventory inventory = player.getInventory();
        ItemStack itemInSlot = UtilInventory.getItemInSlot(inventory, value, null);
        final PluginItem<?> slotPi = PluginItemRegistry.get().fromItemStackNullable(itemInSlot);
        if (slotPi == null) { // should be handled with other "requirement"
            Main.INSTANCE.getPlugin().getLogger().severe(getClass().getName() + ": `slotPi` is null");
            player.sendMessage(UtilComponent.asComponent(Main.INSTANCE.messageConfig.internalError));
            return Result.fail();
        }

        Map<PluginItem<?>, Integer> itemCost = ReforgeCommon.getItemCost(slotPi);
        if (itemCost == null) { // should be handled with other "requirement"
            Main.INSTANCE.getPlugin().getLogger().severe(getClass().getName() + ": `itemCost` is null");
            player.sendMessage(UtilComponent.asComponent(Main.INSTANCE.messageConfig.internalError));
            return Result.fail();
        }

        List<ItemUtils.ItemCheckSession> sessions = ReforgeCommon.checkItemCost(inventory, itemCost);
        for (final ItemUtils.ItemCheckSession session : sessions) {
            if (!session.isAllMatched)
                return Result.fail();
        }

        return successConditional((uid, process) -> {
            Component component = ReforgeCommon.makePluginItemText(itemCost);
            if (component != null) {
                sessions.forEach(session -> session.takeRunnable.run()); // take items out of player inventory
            } else {
                Main.INSTANCE.getPlugin().getLogger().severe(getClass().getName() + ": `component` is null");
                player.sendMessage(UtilComponent.asComponent(Main.INSTANCE.messageConfig.internalError));
            }
            process.next();
        });
    }

    @Override protected boolean getDefaultTake() {
        return false;
    }

    @Override protected Object getDefaultValue() {
        return "hand";
    }

}
