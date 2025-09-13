package com.itsisaacio.natureSMP.custom;

import com.itsisaacio.natureSMP.NatureSMP;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;

public class EnchantedReinforcedDeepslate {
    public static NamespacedKey itemKey = new NamespacedKey(NatureSMP.NATURE, "enchanted_reinforced_deepslate");

    public static NamespacedKey getKey() {
        return itemKey;
    }

    public static ItemStack getItem() {
        ItemStack item = new ItemStack(Material.REINFORCED_DEEPSLATE);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("§5§lEnchanted Reinforced Deepslate");
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        ArrayList<String> lore = new ArrayList<>();
        lore.add("§7A mystical block infused with ancient power");
        lore.add("§7Used to create portals to other dimensions");
        lore.add("§d✦ Glows with ethereal energy when placed");
        meta.setLore(lore);

        meta.getPersistentDataContainer().set(getKey(), PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);

        return item;
    }
}