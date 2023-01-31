package me.hsgamer.bettergui.mmohook.util;

import net.Indyuce.mmoitems.api.ReforgeOptions;
import net.Indyuce.mmoitems.api.util.MMOItemReforger;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

/**
 * Provides convenience methods to interact with MMOItems.
 */
public class ReforgeUtils {
    /**
     * @param itemStack the item to be reforged
     * @param options   some reforge options
     *
     * @return empty if it cannot be reforged
     */
    public static Optional<ItemStack> reforge(ItemStack itemStack, String options) {
        MMOItemReforger mod = new MMOItemReforger(itemStack);
        if (!mod.hasTemplate()) {
            return Optional.empty();
        }
        if (!mod.reforge(parseOptions(options))) {
            return Optional.empty();
        }
        return Optional.ofNullable(mod.getResult());
    }

    /**
     * <p>
     * A convenience method to create ReforgeOption instance from a string representation.
     * <p>
     * Following is the order of options from the source code of MMOItems:
     * <ol>
     *     <li>keepName</li>
     *     <li>keepLore</li>
     *     <li>keepEnchantments</li>
     *     <li>keepUpgrades</li>
     *     <li>keepGemStones</li>
     *     <li>keepSoulBind</li>
     *     <li>keepExternalSH</li>
     *     <li>reRoll</li>
     *     <li>keepModifications</li>
     *     <li>keepAdvancedEnchantments</li>
     *     <li>keepSkins</li>
     *     <li>KeepTier</li>
     * </ol>
     *
     * @param options a string like <pre>"124589"</pre> to indicate which reforge option should be "true". Omitting one
     *                means setting that option to "false".
     *
     * @return a reforge options
     */
    public static ReforgeOptions parseOptions(String options) {
        boolean[] mask = new boolean[12];
        for (final char c : options.toCharArray()) {
            int set = Integer.parseInt(String.valueOf(c));
            mask[set - 1] = true;
        }
        return new ReforgeOptions(mask);
    }
}
