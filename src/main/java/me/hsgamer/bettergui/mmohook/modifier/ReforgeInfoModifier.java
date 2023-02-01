package me.hsgamer.bettergui.mmohook.modifier;

import cc.mewcraft.mewcore.item.api.PluginItem;
import cc.mewcraft.mewcore.item.api.PluginItemRegistry;
import cc.mewcraft.mewcore.util.UtilComponent;
import cc.mewcraft.mewcore.util.UtilInventory;
import me.hsgamer.bettergui.mmohook.Main;
import me.hsgamer.bettergui.mmohook.ReforgeCommon;
import me.hsgamer.hscore.bukkit.item.ItemMetaModifier;
import me.hsgamer.hscore.common.interfaces.StringReplacer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ReforgeInfoModifier extends ItemMetaModifier {

    private String slot = "hand";

    @Override
    public String getName() {
        return "cost-lore";
    }

    @Override
    public ItemMeta modifyMeta(ItemMeta meta, UUID uuid, Map<String, StringReplacer> stringReplacerMap) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return meta;

        PlayerInventory inv = player.getInventory();
        Map<PluginItem<?>, Integer> cost = Optional
            .ofNullable(UtilInventory.getItemInSlot(inv, slot, null))
            .map(item -> PluginItemRegistry.get().fromItemStackNullable(item))
            .map(ReforgeCommon::getItemCost)
            .orElse(null);

        if (cost == null)
            return meta;

        List<Component> itemText = new ArrayList<>();
        for (final Map.Entry<PluginItem<?>, Integer> entry : cost.entrySet()) {
            ItemStack item = entry.getKey().createItemStack();
            if (item == null)
                return meta;
            itemText.add(
                Component.text("×")
                    .append(Component.text(entry.getValue()))
                    .appendSpace()
                    .append(item.displayName().hoverEvent(null))
            );
        }

        List<Component> loreFormat = UtilComponent.asComponent(Main.INSTANCE.reforgeSettings.getOriginal().getStringList("reforge-lore"));
        List<Component> lore = UtilComponent.replacePlaceholderList("{item_cost}", loreFormat, itemText, true);
        meta.lore(lore);
        return meta;
    }

    @Override
    public void loadFromItemMeta(ItemMeta meta) {
        // NOT USED
    }

    @Override
    public boolean canLoadFromItemMeta(ItemMeta meta) {
        return true;
    }

    @Override
    public boolean compareWithItemMeta(ItemMeta meta, UUID uuid, Map<String, StringReplacer> stringReplacerMap) {
        return true;
    }

    @Override
    public Object toObject() {
        return slot;
    }

    @Override
    public void loadFromObject(Object object) {
        slot = object.toString();
    }

}
