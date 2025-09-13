package com.itsisaacio.natureSMP.custom;

import com.itsisaacio.natureSMP.NatureSMP;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class Energizer {
    private static final NamespacedKey ITEM_KEY = new NamespacedKey(NatureSMP.NATURE, "recharger");

    public static NamespacedKey getKey() {
        return ITEM_KEY;
    }

    public static ItemStack getItem() {
        // ✅ Use proper constructor, not ItemStack.of or ItemStack.empty
        ItemStack item = new ItemStack(Material.WARPED_FUNGUS_ON_A_STICK, 1);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // ✅ 1.21+ custom model hook
            // Use your own if you have a resource pack, e.g. NamespacedKey.fromString("naturesmp:recharger")
            meta.setItemModel(NamespacedKey.minecraft("ominous_trial_key"));

            meta.setCustomModelData(1);
            meta.setEnchantmentGlintOverride(true);

            meta.setDisplayName("§bᴇɴᴇʀɢɪᴢᴇʀ");
            meta.setLore(List.of(
                    "§fWhen dropped onto the §bEnergizing Pedestal§f, starts an §aenergizing ritual§f."
            ));

            // ✅ persistent data flag
            PersistentDataContainer data = meta.getPersistentDataContainer();
            data.set(ITEM_KEY, PersistentDataType.BOOLEAN, true);

            item.setItemMeta(meta);
        }

        return item;
    }
}
