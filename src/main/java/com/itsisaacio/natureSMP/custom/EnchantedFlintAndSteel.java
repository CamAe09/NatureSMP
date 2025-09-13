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

public class EnchantedFlintAndSteel {
    public static NamespacedKey itemKey = new NamespacedKey(NatureSMP.NATURE, "enchanted_flint_and_steel");

    public static NamespacedKey getKey() {
        return itemKey;
    }

    public static ItemStack getItem() {
        ItemStack item = new ItemStack(Material.FLINT_AND_STEEL);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("§6§lEnchanted Flint and Steel");
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addEnchant(Enchantment.FIRE_ASPECT, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        ArrayList<String> lore = new ArrayList<>();
        lore.add("§7Ignites with supernatural flame");
        lore.add("§7Can activate ancient portal structures");
        lore.add("§6✦ Radiates mystical energy when dropped");
        meta.setLore(lore);

        meta.getPersistentDataContainer().set(getKey(), PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);

        return item;
    }
}