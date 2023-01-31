package me.hsgamer.bettergui.mmohook;

import cc.mewcraft.mewcore.item.api.PluginItem;
import cc.mewcraft.mewcore.item.api.PluginItemRegistry;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static me.hsgamer.hscore.bukkit.utils.ItemUtils.ItemCheckSession;
import static me.hsgamer.hscore.bukkit.utils.ItemUtils.createItemCheckSession;
import static net.kyori.adventure.text.Component.text;

/**
 * Contains common code of the reforge feature.
 */
public final class ReforgeCommon {

    // Reading plugin items from 3rd party plugins are kinda expensive.
    // We cache it in case the players keep fast trying reforging items.
    public static final LoadingCache<String, Map<PluginItem<?>, Integer>> COST_CACHE = CacheBuilder.newBuilder()
        .expireAfterAccess(Duration.ofSeconds(60))
        .build(new CacheLoader<>() {
            @Override public @NotNull Map<PluginItem<?>, Integer> load(final @NotNull String key) throws NullPointerException {
                Map<PluginItem<?>, Integer> costMap = new HashMap<>(); // mappings: item -> amount
                List<String> configLines = Main.INSTANCE.reforgeAvailableConfig.getOriginal().getStringList(key);
                for (final String line : configLines) {
                    String[] split = line.split("/", 2); // left to the "/" is item key, right to the "/" is the amount
                    String costKey = split[0]; // e.g. "plugin:item"
                    int costAmount = Integer.parseInt(split[1]); // e.g. "3"
                    PluginItem<?> pi = PluginItemRegistry.get().fromReferenceNullable(costKey);
                    if (pi != null) {
                        costMap.put(pi, costAmount);
                    } else {
                        throw new NullPointerException("Cannot load plugin item \"%s\" in the reforge config".formatted(costKey));
                    }
                }
                return costMap;
            }
        });

    public static void flush() {
        COST_CACHE.invalidateAll();
    }

    public static @Nullable Map<PluginItem<?>, Integer> getCostMap(
        @NotNull final PluginItem<?> slotPi
    ) {
        Map<PluginItem<?>, Integer> costMap;
        try {
            // If the try-block finishes without exception, that means
            // the Plugin Items are correctly loaded and safe to use.
            costMap = ReforgeCommon.COST_CACHE.get(Objects.requireNonNull(slotPi).getItemId());
        } catch (ExecutionException e) { // non-existing item id are found in the reforge config file.
            Main.INSTANCE.getPlugin().getLogger().severe(e.getMessage());
            return null;
        }
        return costMap;
    }

    /**
     * Checks if the item can be reforged.
     *
     * @return true if it can be reforged; otherwise false
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean canReforge(
        @Nullable final ItemStack itemInSlot,
        @Nullable final PluginItem<?> slotPi
    ) {
        if (itemInSlot == null) { // the slot is empty
            return false;
        }
        if (slotPi == null || !slotPi.getPlugin().equals("mmoitems")) { // it's not of a MMOItem; of course cannot be reforged
            return false;
        }
        if (!Main.INSTANCE.reforgeAvailableConfig.getOriginal().getKeys(false).contains(slotPi.getItemId())) { // it's of a MMOItem, but not present in the reforge config
            return false;
        }
        return true;
    }

    /**
     * Gets a list of {@link ItemCheckSession} based on the given cost map.
     *
     * @return a list of {@link ItemCheckSession}s.
     */
    public static @NotNull List<ItemCheckSession> checkCost(
        @NotNull final PlayerInventory inventory,
        @NotNull final Map<PluginItem<?>, Integer> costMap
    ) {
        List<ItemCheckSession> sessionList = new ArrayList<>();
        costMap.forEach((key, value) -> sessionList.add(createItemCheckSession(inventory, key::matches, value)));
        return sessionList;
    }

    /**
     * Gets a hoverable text component of all the items.
     *
     * @param map a map of the items
     *
     * @return a hoverable text component of all the items; or null if internal error occurs
     */
    public static @NotNull Component makeItemStackText(
        @NotNull final Map<ItemStack, Integer> map
    ) {
        TextComponent.Builder itemCostText = text();
        Iterator<Map.Entry<ItemStack, Integer>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) { // use iterator to properly add space between items
            Map.Entry<ItemStack, Integer> entry = iterator.next();
            ItemStack item = entry.getKey();
            itemCostText
                .append(item.displayName())
                .append(text("×"))
                .append(text(entry.getValue()));
            if (iterator.hasNext())
                itemCostText.appendSpace();
        }
        return itemCostText.asComponent();
    }

    /**
     * Gets a hoverable text component of all the items.
     *
     * @param map a map of the items
     *
     * @return a hoverable text component of all the items; or null if internal error occurs
     */
    public static @Nullable Component makePluginItemText(
        @NotNull final Map<PluginItem<?>, Integer> map
    ) {
        HashMap<ItemStack, Integer> itemMap = new HashMap<>();
        for (final Map.Entry<PluginItem<?>, Integer> entry : map.entrySet()) {
            ItemStack item = entry.getKey().createItemStack();
            if (item == null) { // the plugin item is registered but the backed plugin return a null
                Main.INSTANCE.getPlugin().getLogger().severe("Error occurred when making plugin item text");
                return null;
            }
            itemMap.put(item, entry.getValue());
        }
        return makeItemStackText(itemMap);
    }

}
