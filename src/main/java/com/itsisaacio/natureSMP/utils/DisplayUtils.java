package com.itsisaacio.natureSMP.utils;

import com.itsisaacio.natureSMP.NatureSMP;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public class DisplayUtils {
    public static NamespacedKey locationKey = new NamespacedKey(NatureSMP.NATURE, "DisplayLocation");
    public static NamespacedKey displayKey = new NamespacedKey(NatureSMP.NATURE, "DisplayType");

    public static String locationToString(Location location)
    {
        return location.getWorld().getName() + "|" + location.getBlockX() + "|" + location.getBlockY() + "|" + location.getBlockZ() + "|";
    }
    public static Location stringToLocation(String string)
    {
        String[] split = string.split("\\|");
        return new Location(Bukkit.getWorld(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));
    }

    public static BlockDisplay blockDisplay(Location location, Material type)
    {
        return blockDisplay(location, type, 0.002f);
    }
    public static BlockDisplay blockDisplay(Location location, Material type, float scale)
    {
        return blockDisplay(location, type, scale, scale, scale);
    }
    public static BlockDisplay blockDisplay(Location location, Material type, float xScale, float yScale, float zScale)
    {
        BlockDisplay display = (BlockDisplay) location.getWorld().spawnEntity(location, EntityType.BLOCK_DISPLAY);
        display.setBlock(type.createBlockData());
        display.setTransformation(new Transformation(
                new Vector3f(xScale / -2, yScale / -2, zScale / -2),
                new AxisAngle4f(),
                new Vector3f(xScale + 1, yScale + 1, zScale + 1),
                new AxisAngle4f()
        ));

        return display;
    }

    public static ItemDisplay itemDisplay(Location location, ItemStack type)
    {
        return itemDisplay(location, type, 1.002f);
    }
    public static ItemDisplay itemDisplay(Location location, ItemStack type, float scale)
    {
        return itemDisplay(location, type, scale, scale, scale);
    }
    public static ItemDisplay itemDisplay(Location location, ItemStack item, float xScale, float yScale, float zScale)
    {
        ItemDisplay display = (ItemDisplay) location.getWorld().spawnEntity(location, EntityType.ITEM_DISPLAY);
        display.setItemStack(item);
        display.setTransformation(new Transformation(
                new Vector3f(),
                new AxisAngle4f(),
                new Vector3f(xScale, yScale, zScale),
                new AxisAngle4f()
        ));

        return display;
    }

    public static TextDisplay textDisplay(Location location, Component text, float scale)
    {
        return textDisplay(location, text, scale, scale, scale);
    }
    public static TextDisplay textDisplay(Location location, String text)
    {
        return textDisplay(location, Component.text(text), 0, 0, 0);
    }
    public static TextDisplay textDisplay(Location location, String text, float scale)
    {
        return textDisplay(location, Component.text(text), scale, scale, scale);
    }
    public static TextDisplay textDisplay(Location location, Component text, float xScale, float yScale, float zScale)
    {
        TextDisplay display = (TextDisplay) location.getWorld().spawnEntity(location, EntityType.TEXT_DISPLAY);
        display.setTransformation(new Transformation(
                new Vector3f(xScale / -2, yScale / -2, zScale / -2),
                new AxisAngle4f(),
                new Vector3f(xScale + 1, yScale + 1, zScale + 1),
                new AxisAngle4f()
        ));
        display.text(text);

        return display;
    }
}
