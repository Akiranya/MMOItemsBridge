package me.hsgamer.bettergui.mmohook.action;

import cc.mewcraft.mewcore.item.api.PluginItem;
import cc.mewcraft.mewcore.item.api.PluginItemRegistry;
import cc.mewcraft.mewcore.util.UtilComponent;
import cc.mewcraft.mewcore.util.UtilInventory;
import me.hsgamer.bettergui.api.action.BaseAction;
import me.hsgamer.bettergui.builder.ActionBuilder;
import me.hsgamer.bettergui.mmohook.Main;
import me.hsgamer.bettergui.mmohook.ReforgeCommon;
import me.hsgamer.bettergui.mmohook.util.ReforgeUtils;
import me.hsgamer.hscore.task.element.TaskProcess;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ItemReforgeAction extends BaseAction {

    public ItemReforgeAction(ActionBuilder.Input input) {
        super(input);
    }

    @Override public void accept(final UUID uuid, final TaskProcess process) {
        final Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            process.next();
            return;
        }

        final String slot = input.value; // read the slot value
        final Map<String, String> optionAsMap = input.getOptionAsMap();
        final String action = optionAsMap.get("a"); // accepted: "a=make", "a=view" ...
        final String options = optionAsMap.get("o"); // accepted: "o=12367", "o=138" ...
        if (action == null) {
            process.next();
            return;
        }

        final Runnable task = switch (action) {
            case "make" -> () -> {
                PlayerInventory inventory = player.getInventory();
                ItemStack itemInSlot = UtilInventory.getItemInSlot(inventory, slot, null);
                PluginItem<?> slotPi = PluginItemRegistry.get().fromItemStackNullable(itemInSlot);
                if (!ReforgeCommon.isReforgeAvailable(itemInSlot, slotPi)) {
                    player.sendMessage(UtilComponent.asComponent(Main.INSTANCE.messageConfig.reforgeFailed));
                    process.next();
                    return;
                }

                Optional<ItemStack> result = ReforgeUtils.reforge(itemInSlot, options);
                if (result.isPresent()) {
                    TagResolver.Single resolver = Placeholder.component("result", result.get().displayName());
                    player.sendMessage(UtilComponent.asComponent(Main.INSTANCE.messageConfig.reforgeSucceeded, resolver));
                    inventory.setItemInMainHand(result.get());
                } else {
                    Main.INSTANCE.getPlugin().getLogger().severe(getClass().getName() + ": MMOItems failed to reforge the item");
                    player.sendMessage(UtilComponent.asComponent(Main.INSTANCE.messageConfig.internalError));
                }
                process.next();
            };

            case "view" -> () -> {
                PlayerInventory inventory = player.getInventory();
                ItemStack itemInSlot = UtilInventory.getItemInSlot(inventory, slot, null);
                PluginItem<?> slotPi = PluginItemRegistry.get().fromItemStackNullable(itemInSlot);
                if (!ReforgeCommon.isReforgeAvailable(itemInSlot, slotPi)) {
                    player.sendMessage(UtilComponent.asComponent(Main.INSTANCE.messageConfig.viewFailed));
                    process.next();
                    return;
                }

                Map<PluginItem<?>, Integer> itemCost = ReforgeCommon.getItemCost(slotPi);
                if (itemCost == null) {
                    Main.INSTANCE.getPlugin().getLogger().severe(getClass().getName() + ": `itemCost` is null");
                    player.sendMessage(UtilComponent.asComponent(Main.INSTANCE.messageConfig.internalError));
                    process.next();
                    return;
                }
                Component component = ReforgeCommon.makePluginItemText(itemCost);
                if (component == null) {
                    Main.INSTANCE.getPlugin().getLogger().severe(getClass().getName() + ": `component` is null");
                    player.sendMessage(UtilComponent.asComponent(Main.INSTANCE.messageConfig.internalError));
                    process.next();
                    return;
                }
                TagResolver resolver = TagResolver.resolver(
                    Placeholder.component("target", itemInSlot.displayName()),
                    Placeholder.component("item_cost", component)
                );
                player.sendMessage(UtilComponent.asComponent(Main.INSTANCE.messageConfig.viewItemCost, resolver));
                process.next();
            };

            default -> () -> {};
        };

        Bukkit.getScheduler().runTask(Main.INSTANCE.getPlugin(), task); // run the chosen task
    }

}
