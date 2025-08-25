package com.itsisaacio.natureSMP.custom;

import com.itsisaacio.natureSMP.NatureSMP;
import com.itsisaacio.natureSMP.Keys;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;

public class EnergyCore {
    public static NamespacedKey itemKey = new NamespacedKey(NatureSMP.NATURE, "energy_core");

    public static NamespacedKey getKey()
    {
        return itemKey;
    }

    public static ItemStack getItem() {
        ItemStack item = ItemStack.of(Material.WARPED_FUNGUS_ON_A_STICK);
        ItemMeta meta = item.getItemMeta();
        //meta.setItemModel(NamespacedKey.fromString("naturesmp:recharger"));
        meta.setItemModel(NamespacedKey.fromString("minecraft:echo_shard"));
        meta.setCustomModelData(3);
        meta.setEnchantmentGlintOverride(true);

        meta.setDisplayName("§eᴇɴᴇʀɢʏ ᴄᴏʀᴇ");
        meta.setLore(new ArrayList<>(){{
            add("§fWhen right clicked, gives +1 energy.");
            add("§f/withdraw can be used to convert an energy into this item.");
        }});

        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(itemKey, PersistentDataType.BOOLEAN, true);

        item.setItemMeta(meta);

        return item;
    }
    public static void interact(Player player, ItemStack item)
    {
        PersistentDataContainer data = player.getPersistentDataContainer();
        int energy = data.getOrDefault(Keys.energyKey, PersistentDataType.INTEGER, 3);

        if (energy <= 0)
            player.sendMessage("You have §c0 energy §fleft! You need to recharge at the §bEnergizing Pedestal§f.");
        else if (energy >= 5)
            player.sendMessage("You are at §bmaximum energy§f!");
        else
        {
            data.set(Keys.energyKey, PersistentDataType.INTEGER, energy + 1);

            item.setAmount(item.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1, 1);
            player.sendMessage("You now have §a" + (energy + 1) + "§b energy§f!");
        }
    }
}
