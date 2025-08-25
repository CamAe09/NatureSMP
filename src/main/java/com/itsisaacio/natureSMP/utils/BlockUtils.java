package com.itsisaacio.natureSMP.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.noise.PerlinOctaveGenerator;

import java.util.ArrayList;

public class BlockUtils {
    public static PerlinOctaveGenerator noise = new PerlinOctaveGenerator(MathUtils.random, 1);

    public static ArrayList<Block> getNearbyBlocks(Location location, int xRadius, int yRadius, int zRadius) {
        ArrayList<Block> blocks = new ArrayList<>();

        for (int x = location.getBlockX() - xRadius; x <= location.getBlockX() + xRadius; x++) {
            for (int y = location.getBlockY() - yRadius; y <= location.getBlockY() + yRadius; y++) {
                for (int z = location.getBlockZ() - zRadius; z <= location.getBlockZ() + zRadius; z++) {
                    blocks.add(location.getWorld().getBlockAt(x, y, z));
                }
            }
        }
        return blocks;
    }

    public static ArrayList<Block> getNearbyBlocks(Location location, int radius) {
        return getNearbyBlocks(location, radius, radius, radius);
    }

    public static ArrayList<Block> getSurfaceBlocks(ArrayList<Block> area, boolean liquids) {
        try {
            ArrayList<Block> surface = new ArrayList<>();
            for (Block block : area) {
                Block above = block.getWorld().getBlockAt(block.getLocation().add(0, 1, 0));

                if (above.getType().isEmpty() || (liquids && (above.getType() == Material.WATER || above.getType() == Material.LAVA)))
                    surface.add(block);
            }

            return surface;
        } catch (Error error) {
            error.printStackTrace();
        }

        return new ArrayList<>();
    }

    public static ArrayList<Material> replaceable = new ArrayList<>() {{
        add(Material.GRASS_BLOCK);
        add(Material.DIRT);
        add(Material.STONE);
        add(Material.GRANITE);
        add(Material.DIORITE);
        add(Material.ANDESITE);
        add(Material.GRAVEL);
        add(Material.SAND);
        add(Material.SANDSTONE);
        add(Material.RED_SAND);
        add(Material.RED_SANDSTONE);
        add(Material.TUFF);
        add(Material.DEEPSLATE);
        add(Material.NETHERRACK);
        add(Material.BASALT);
        add(Material.BLACKSTONE);
        add(Material.PODZOL);
        add(Material.COARSE_DIRT);
        add(Material.MYCELIUM);
        add(Material.ROOTED_DIRT);
        add(Material.MOSS_BLOCK);
        add(Material.MUD);
        add(Material.MUDDY_MANGROVE_ROOTS);
        add(Material.CRIMSON_NYLIUM);
        add(Material.WARPED_NYLIUM);
        add(Material.SOUL_SAND);
        add(Material.CALCITE);
        add(Material.SMOOTH_BASALT);
        add(Material.CLAY);
        add(Material.DRIPSTONE_BLOCK);
        add(Material.POINTED_DRIPSTONE);
        add(Material.END_STONE);

        add(Material.TERRACOTTA);
        add(Material.WHITE_TERRACOTTA);
        add(Material.WHITE_GLAZED_TERRACOTTA);
        add(Material.RED_TERRACOTTA);
        add(Material.RED_GLAZED_TERRACOTTA);
        add(Material.ORANGE_TERRACOTTA);
        add(Material.ORANGE_GLAZED_TERRACOTTA);
        add(Material.YELLOW_TERRACOTTA);
        add(Material.YELLOW_GLAZED_TERRACOTTA);
        add(Material.GREEN_TERRACOTTA);
        add(Material.GREEN_GLAZED_TERRACOTTA);
        add(Material.CYAN_TERRACOTTA);
        add(Material.CYAN_GLAZED_TERRACOTTA);
        add(Material.BLUE_TERRACOTTA);
        add(Material.BLUE_GLAZED_TERRACOTTA);
        add(Material.PURPLE_TERRACOTTA);
        add(Material.PURPLE_GLAZED_TERRACOTTA);
        add(Material.PINK_TERRACOTTA);
        add(Material.PINK_GLAZED_TERRACOTTA);
        add(Material.BROWN_TERRACOTTA);
        add(Material.BROWN_GLAZED_TERRACOTTA);
        add(Material.BLACK_TERRACOTTA);
        add(Material.BLACK_GLAZED_TERRACOTTA);
        add(Material.BLACK_GLAZED_TERRACOTTA);
        add(Material.BLACK_TERRACOTTA);
        add(Material.BLACK_GLAZED_TERRACOTTA);
        add(Material.MAGENTA_TERRACOTTA);
        add(Material.MAGENTA_GLAZED_TERRACOTTA);
        add(Material.LIME_TERRACOTTA);
        add(Material.LIME_GLAZED_TERRACOTTA);
        add(Material.LIGHT_BLUE_TERRACOTTA);
        add(Material.LIGHT_BLUE_GLAZED_TERRACOTTA);
        add(Material.LIGHT_GRAY_TERRACOTTA);
        add(Material.LIGHT_GRAY_GLAZED_TERRACOTTA);
    }};
}
