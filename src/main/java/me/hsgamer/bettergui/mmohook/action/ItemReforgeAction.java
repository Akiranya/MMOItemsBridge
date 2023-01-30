package me.hsgamer.bettergui.mmohook.action;

import cc.mewcraft.mewcore.util.UtilComponent;
import cc.mewcraft.mewcore.util.UtilInventory;
import me.hsgamer.bettergui.api.action.BaseAction;
import me.hsgamer.bettergui.builder.ActionBuilder;
import me.hsgamer.bettergui.mmohook.MMOItemsHook;
import me.hsgamer.bettergui.mmohook.util.ReforgeUtils;
import me.hsgamer.hscore.task.element.TaskProcess;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Optional;
import java.util.UUID;

public class ItemReforgeAction extends BaseAction {
    private final MMOItemsHook hook;

    public ItemReforgeAction(MMOItemsHook hook, ActionBuilder.Input input) {
        super(input);
        this.hook = hook;
    }

    @Override public void accept(final UUID uuid, final TaskProcess process) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            process.next();
            return;
        }
        String slot = input.value; // take the item in specific slot
        String options = Optional // take the item reforge options
            .of(input.getOptionAsList())
            .map(opt -> opt.isEmpty() ? "" : opt.get(0))
            .orElse("");
        Bukkit.getScheduler().runTask(hook.getPlugin(), () -> {
            PlayerInventory inventory = player.getInventory();
            ItemStack itemInSlot = UtilInventory.getItemInSlot(inventory, slot, null);
            if (itemInSlot == null) {
                process.next();
                return;
            }
            ItemStack result = ReforgeUtils.reforge(itemInSlot, options);
            if (result != null) {
                inventory.setItemInMainHand(result);
                player.sendMessage(UtilComponent.asComponent(hook.getMessageConfig().reforgeSucceeded));
                process.next();
            }
        });
    }

}
