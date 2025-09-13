package com.itsisaacio.natureSMP.utils;

import com.itsisaacio.natureSMP.NatureSMP;
import org.bukkit.persistence.PersistentDataContainer;
import me.kodysimpson.simpapi.colors.ColorTranslator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Comparator;

public class Utilities {
    public static Vector relativeRight(Location location)
    {
        float yaw = location.getYaw() + 90f; // could be wrong, change to - 90 if so
        double yawRad = yaw * (Math.PI / 180d);
        double z = Math.cos(yawRad);
        double x = -Math.sin(yawRad); // because 90 degrees is -X?!?!!? notch why.
        return new Vector(x, 0d, z); // a unit vector pointing to the right of the entity
    }

    public static ArrayList<NamedTextColor> colors = new ArrayList<>() {{
        add(NamedTextColor.DARK_RED);
        add(NamedTextColor.RED);
        add(NamedTextColor.GOLD);
        add(NamedTextColor.YELLOW);
        add(NamedTextColor.DARK_GREEN);
        add(NamedTextColor.GREEN);
        add(NamedTextColor.AQUA);
        add(NamedTextColor.DARK_AQUA);
        add(NamedTextColor.DARK_BLUE);
        add(NamedTextColor.BLUE);
        add(NamedTextColor.LIGHT_PURPLE);
        add(NamedTextColor.DARK_PURPLE);
        add(NamedTextColor.WHITE);
        add(NamedTextColor.GRAY);
        add(NamedTextColor.DARK_GRAY);
        add(NamedTextColor.BLACK);
    }};
    public static ArrayList<PotionEffectType> positiveEffects = new ArrayList<>() {{
        add(PotionEffectType.REGENERATION);
        add(PotionEffectType.STRENGTH);
        add(PotionEffectType.HASTE);
        add(PotionEffectType.RESISTANCE);
        add(PotionEffectType.DOLPHINS_GRACE);
        add(PotionEffectType.FIRE_RESISTANCE);
        add(PotionEffectType.HERO_OF_THE_VILLAGE);
        add(PotionEffectType.ABSORPTION);
        add(PotionEffectType.CONDUIT_POWER);
        add(PotionEffectType.HEALTH_BOOST);
        add(PotionEffectType.INSTANT_HEALTH);
        add(PotionEffectType.INVISIBILITY);
        add(PotionEffectType.SPEED);
        add(PotionEffectType.SATURATION);
        add(PotionEffectType.WATER_BREATHING);
        add(PotionEffectType.NIGHT_VISION);
        add(PotionEffectType.LUCK);
    }};
    public static ArrayList<Biome> warmBiomes = new ArrayList<>() {{
        add(Biome.BADLANDS);
        add(Biome.BEACH);
        add(Biome.DESERT);
        add(Biome.ERODED_BADLANDS);
        add(Biome.SAVANNA);
        add(Biome.SAVANNA_PLATEAU);
        add(Biome.BADLANDS);
    }};

    public static ArrayList<LivingEntity> getNearby(Location location, float rangeX, float rangeY, float rangeZ) {
        ArrayList<LivingEntity> entities = new ArrayList<>(location.getNearbyLivingEntities(rangeX, rangeY, rangeZ));
        entities.sort(Comparator.comparingDouble(entity -> entity.getLocation().distanceSquared(location)));

        return entities;
    }
    public static ArrayList<LivingEntity> getNearby(Location location, float range) {
        return getNearby(location, range, range, range);
    }
    public static boolean stringIs(PersistentDataContainer data, NamespacedKey key, String equals, String orDefault)
    {
        return data.getOrDefault(key, PersistentDataType.STRING, orDefault).equalsIgnoreCase(equals);
    }

    public static String getCompassDirection(Entity entity) {
        double rotation = (entity.getLocation().getYaw() - 90) % 360;
        if (rotation < 0)
            rotation += 360f;
        if (0 <= rotation && rotation < 22.5)
            return "N";
        else if (22.5 <= rotation && rotation < 67.5)
            return "NE";
        else if (67.5 <= rotation && rotation < 112.5)
            return "E";
        else if (112.5 <= rotation && rotation < 157.5)
            return "SE";
        else if (157.5 <= rotation && rotation < 202.5)
            return "S";
        else if (202.5 <= rotation && rotation < 247.5)
            return "SW";
        else if (247.5 <= rotation && rotation < 292.5)
            return "W";
        else if (292.5 <= rotation && rotation < 337.5)
            return "NW";
        else return "N";
    }
    public static String getSimpleDirection(Entity entity)
    {
        return switch (getCompassDirection(entity)) {
            case "E", "SE" -> "e";
            case "S", "SW" -> "s";
            case "W", "NW" -> "w";
            default -> "n";
        };

    }
    public static String degreesToDirection(int degrees)
    {
        return switch (degrees) {
            case 270 -> "s";
            case 180 -> "e";
            case 0 -> "w";
            default -> "n";
        };
    }
    public static int directionToDegrees(String direction)
    {
        return switch (direction) {
            case "s" -> 270;
            case "e" -> 180;
            case "w" -> 0;
            default -> 90;
        };
    }

    static MetadataValue value = new FixedMetadataValue(NatureSMP.NATURE, true);
    public static void metaData(Metadatable object, String metaData, Object data)
    {
        object.removeMetadata(metaData, NatureSMP.NATURE);
        object.setMetadata(metaData, new FixedMetadataValue(NatureSMP.NATURE, data));
    }
    public static void metaData(Metadatable object, String... metaData)
    {
        for (String data : metaData) {
            object.removeMetadata(data, NatureSMP.NATURE);
            object.setMetadata(data, value);
        }
    }
    public static boolean potionTag(Entity entity, String tag)
    {
        if (entity.hasMetadata(tag)) return false;
        metaData(entity, tag);

        NatureSMP.NATURE.delay(() -> entity.removeMetadata(tag, NatureSMP.NATURE), 1);
        return true;
    }

    public static Component text(String string)
    {
        return text(string, NamedTextColor.WHITE);
    }
    public static Component text(String string, TextColor color)
    {
        return Component.text(string).color(color).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }
    public static String inBetween(String text, String between)
    {
        return text.replaceAll("(?<=.)(?=.)", between);
    }

    public static String hex(String string) {
        return ColorTranslator.translateColorCodes("&" + string);
    }
}