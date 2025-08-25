package com.itsisaacio.natureSMP.custom;

import com.itsisaacio.natureSMP.NatureSMP;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;

public class Energizer {
    public static NamespacedKey itemKey = new NamespacedKey(NatureSMP.NATURE, "recharger");

    public static NamespacedKey getKey()
    {
        return itemKey;
    }

    public static ItemStack getItem() {
        ItemStack item = ItemStack.of(Material.WARPED_FUNGUS_ON_A_STICK);
        ItemMeta meta = item.getItemMeta();
        //meta.setItemModel(NamespacedKey.fromString("naturesmp:recharger"));
        meta.setItemModel(NamespacedKey.minecraft("ominous_trial_key"));
        meta.setCustomModelData(1);
        meta.setEnchantmentGlintOverride(true);

        meta.setDisplayName("§bᴇɴᴇʀɢɪᴢᴇʀ");
        meta.setLore(new ArrayList<>(){{
            add("§fWhen dropped onto the §bEnergizing Pedestal§f, starts an §aenergizing ritual§f.");
        }});

        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(itemKey, PersistentDataType.BOOLEAN, true);

        item.setItemMeta(meta);

        return item;
    }
}
