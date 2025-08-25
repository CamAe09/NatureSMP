package com.itsisaacio.natureSMP.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemUtils {
    public static ArrayList<Component> loreFix(List<?> lore)
    {
        ArrayList<Component> fixed = new ArrayList<>();

        for (Object o : lore) {
            Component component = o instanceof Component ? (Component) o : Component.text((String) o);
            fixed.add(component);
        }

        return fixed;
    }

    public static void damageItemPercent(ItemStack item, int percent) {
        ItemMeta meta = item.getItemMeta();

        if(meta instanceof Damageable damageable && damageable.hasMaxDamage()) {
            int damage = damageable.getMaxDamage() * (percent / 100);
            damageable.setDamage(damageable.getDamage() + damage);
            item.setItemMeta(damageable);
        }
    }
    public static void damageItem(ItemStack item, int damage) {
        if(item instanceof Damageable damageable) {
            if(damageable.hasDamage())
                damageable.setDamage(damageable.getDamage() + damage);
        }
    }

    public static ItemStack item(Material material, Component name, NamespacedKey model, List<Component> lore)
    {
        ItemStack item = ItemStack.of(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(name.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.lore(lore);
        item.setItemMeta(meta);

        itemModel(item, model);

        return item;
    }
    public static ItemStack item(Material material, Component name, String model, List<Component> lore)
    {
        return item(material, name, NamespacedKey.fromString("lucksmp:" + model), lore);
    }
    public static ItemStack item(Material material, Component name, NamespacedKey model, String... lore)
    {
        return item(material, name, model, loreFix(Arrays.asList(lore)));
    }
    public static ItemStack item(Material material, String name, String model, String... lore)
    {
        return item(material, Component.text(name), NamespacedKey.fromString("lucksmp:" + model), loreFix(Arrays.asList(lore)));
    }
    public static ItemStack item(Material material, Component name, String model, String... lore)
    {
        return item(material, name, NamespacedKey.fromString("lucksmp:" + model), loreFix(Arrays.asList(lore)));
    }
    public static ItemStack item(Material material, String name, String model, List<String> lore)
    {
        return item(material, Component.text(name),NamespacedKey.fromString("lucksmp:" + model), loreFix(lore));
    }
    public static ItemStack itemModel(ItemStack item, NamespacedKey model)
    {
        ItemMeta meta = item.getItemMeta();
        meta.setItemModel(model);
        item.setItemMeta(meta);
        return item;
    }
    public static ItemStack itemModel(ItemStack item, String model)
    {
        return itemModel(item, NamespacedKey.fromString(model));
    }
}
