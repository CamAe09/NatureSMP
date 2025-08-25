package com.itsisaacio.natureSMP.custom;

import com.itsisaacio.natureSMP.NatureSMP;
import com.itsisaacio.natureSMP.ui.EntrailSwapper;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;

public class Swapper {
    public static NamespacedKey itemKey = new NamespacedKey(NatureSMP.NATURE, "swapper");

    public static NamespacedKey getKey()
    {
        return itemKey;
    }

    public static ItemStack getItem() {
        ItemStack item = ItemStack.of(Material.WARPED_FUNGUS_ON_A_STICK);
        ItemMeta meta = item.getItemMeta();
        //meta.setItemModel(NamespacedKey.fromString("naturesmp:recharger"));
        meta.setItemModel(NamespacedKey.fromString("minecraft:prismarine_shard"));
        meta.setCustomModelData(2);
        meta.setEnchantmentGlintOverride(true);

        meta.setDisplayName("§bsᴡᴀᴘᴘᴇʀ");
        meta.setLore(new ArrayList<>(){{
            add("§fWhen right clicked, removes the item and re-rolls your entrail.");
        }});

        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(itemKey, PersistentDataType.BOOLEAN, true);

        item.setItemMeta(meta);

        return item;
    }
    public static void interact(Player player, ItemStack item)
    {
        EntrailSwapper.randomize(player);
        item.setAmount(item.getAmount() - 1);
    }
}
