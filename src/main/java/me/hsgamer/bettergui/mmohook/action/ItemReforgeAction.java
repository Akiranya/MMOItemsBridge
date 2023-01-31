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
import me.hsgamer.hscore.bukkit.utils.ItemUtils;
import me.hsgamer.hscore.task.element.TaskProcess;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
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
        final String action = optionAsMap.get("a"); // accepted: "make", "view"
        final String options = optionAsMap.get("o"); // accepted: "o=12367"
        if (action == null) {
            process.next();
            return;
        }

        final Runnable task = switch (action) {
            case "make" -> () -> {
                PlayerInventory inventory = player.getInventory();
                ItemStack itemInSlot = UtilInventory.getItemInSlot(inventory, slot, null);
                PluginItem<?> slotPi = PluginItemRegistry.get().fromItemStackNullable(itemInSlot);
                if (!ReforgeCommon.canReforge(itemInSlot, slotPi)) {
                    player.sendMessage(UtilComponent.asComponent(Main.INSTANCE.messageConfig.reforgeFailed));
                    process.next();
                    return;
                }

                // Check if the player has all the item cost
                Map<PluginItem<?>, Integer> costMap = ReforgeCommon.getCostMap(slotPi);
                if (costMap == null) {
                    player.sendMessage(UtilComponent.asComponent(Main.INSTANCE.messageConfig.internalError));
                    process.next();
                    return;
                }
                List<ItemUtils.ItemCheckSession> allSessions = ReforgeCommon.checkCost(inventory, costMap);
                for (final ItemUtils.ItemCheckSession session : allSessions) {
                    if (!session.isAllMatched) {
                        Component component = ReforgeCommon.makePluginItemText(costMap);
                        if (component == null) {
                            player.sendMessage(UtilComponent.asComponent(Main.INSTANCE.messageConfig.internalError));
                            process.next();
                            return;
                        }
                        TagResolver resolver = TagResolver.resolver(
                            Placeholder.component("target", itemInSlot.displayName()),
                            Placeholder.component("item_cost", component)
                        );
                        player.sendMessage(UtilComponent.asComponent(Main.INSTANCE.messageConfig.notEnoughItems, resolver));
                        process.next();
                        return;
                    }
                }

                // All good, reforge the item
                Optional<ItemStack> result = ReforgeUtils.reforge(itemInSlot, options);
                if (result.isPresent()) {
                    allSessions.forEach(session -> session.takeRunnable.run()); // take items out of player inventory
                    player.sendMessage(UtilComponent.asComponent(Main.INSTANCE.messageConfig.reforgeSucceeded));
                    inventory.setItemInMainHand(result.get());
                    process.next();
                } else {
                    Main.INSTANCE.getPlugin().getLogger().severe("MMOItems failed to reforge the item");
                    player.sendMessage(UtilComponent.asComponent(Main.INSTANCE.messageConfig.internalError));
                    process.next();
                }
            };

            case "view" -> () -> {
                PlayerInventory inventory = player.getInventory();
                ItemStack itemInSlot = UtilInventory.getItemInSlot(inventory, slot, null);
                PluginItem<?> slotPi = PluginItemRegistry.get().fromItemStackNullable(itemInSlot);
                if (!ReforgeCommon.canReforge(itemInSlot, slotPi)) {
                    player.sendMessage(UtilComponent.asComponent(Main.INSTANCE.messageConfig.viewFailed));
                    process.next();
                    return;
                }

                Map<PluginItem<?>, Integer> costMap = ReforgeCommon.getCostMap(slotPi);
                if (costMap == null) {
                    player.sendMessage(UtilComponent.asComponent(Main.INSTANCE.messageConfig.internalError));
                    process.next();
                    return;
                }
                Component component = ReforgeCommon.makePluginItemText(costMap);
                if (component == null) {
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
