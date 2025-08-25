package com.itsisaacio.natureSMP.custom;

import com.itsisaacio.natureSMP.NatureSMP;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;

public class EnergizingPedestal {
    public static NamespacedKey itemKey = new NamespacedKey(NatureSMP.NATURE, "recharge_spawner");

    public static NamespacedKey getKey()
    {
        return itemKey;
    }

    public static ItemStack getItem() {
        ItemStack item = ItemStack.of(Material.REINFORCED_DEEPSLATE);
        ItemMeta meta = item.getItemMeta();
        meta.setItemModel(NamespacedKey.fromString("naturesmp:recharge_spawner"));

        meta.setDisplayName("§bʀᴇᴄʜᴀʀɢᴇ ѕᴘᴀᴡɴᴇʀ");
        meta.setLore(new ArrayList<>(){{
            add("§fPlaces a recharge station.");
        }});

        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(itemKey, PersistentDataType.BOOLEAN, true);

        item.setItemMeta(meta);

        return item;
    }
}
