package com.itsisaacio.natureSMP.events;

import com.itsisaacio.natureSMP.NatureSMP;
import com.itsisaacio.natureSMP.custom.EnchantedReinforcedDeepslate;
import com.itsisaacio.natureSMP.custom.EnchantedFlintAndSteel;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PortalEvents implements Listener {

    private static final Map<Location, Long> breakingBlocks = new HashMap<>();
    private static final Set<Location> enchantedBlocks = new HashSet<>();

    // ----------------- ITEM DROP -----------------
    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        ItemStack itemStack = item.getItemStack();

        if (itemStack.getItemMeta() != null &&
                (itemStack.getItemMeta().getPersistentDataContainer().has(EnchantedReinforcedDeepslate.getKey(), PersistentDataType.BYTE) ||
                        itemStack.getItemMeta().getPersistentDataContainer().has(EnchantedFlintAndSteel.getKey(), PersistentDataType.BYTE))) {

            item.setGlowing(true);
            item.setMetadata("EnchantedItem", new FixedMetadataValue(NatureSMP.NATURE, true));

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!item.isValid() || item.isDead()) {
                        cancel();
                        return;
                    }
                    Location loc = item.getLocation();
                    loc.getWorld().spawnParticle(Particle.ENCHANT, loc, 3, 0.2, 0.2, 0.2, 0.1);
                    loc.getWorld().spawnParticle(Particle.GLOW, loc, 1, 0.1, 0.1, 0.1, 0);
                }
            }.runTaskTimer(NatureSMP.NATURE, 0, 10);
        }
    }

    // ----------------- BLOCK PLACE -----------------
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();

        if (item.getItemMeta() != null &&
                item.getItemMeta().getPersistentDataContainer().has(EnchantedReinforcedDeepslate.getKey(), PersistentDataType.BYTE)) {

            enchantedBlocks.add(block.getLocation());

            // Particles on block
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!enchantedBlocks.contains(block.getLocation()) ||
                            block.getType() != Material.REINFORCED_DEEPSLATE) {
                        cancel();
                        return;
                    }
                    Location loc = block.getLocation().add(0.5, 0.5, 0.5);
                    loc.getWorld().spawnParticle(Particle.ENCHANT, loc, 2, 0.3, 0.3, 0.3, 0.05);
                    loc.getWorld().spawnParticle(Particle.GLOW, loc, 1, 0.2, 0.2, 0.2, 0);
                }
            }.runTaskTimer(NatureSMP.NATURE, 0, 20);

            // Check inventory for portal requirements
            int count = 0;
            boolean hasFlintSteel = false;

            for (ItemStack invItem : player.getInventory().getContents()) {
                if (invItem != null && invItem.getItemMeta() != null) {
                    if (invItem.getItemMeta().getPersistentDataContainer().has(EnchantedReinforcedDeepslate.getKey(), PersistentDataType.BYTE)) {
                        count += invItem.getAmount();
                    }
                    if (invItem.getItemMeta().getPersistentDataContainer().has(EnchantedFlintAndSteel.getKey(), PersistentDataType.BYTE)) {
                        hasFlintSteel = true;
                    }
                }
            }

            if (count >= 14 && hasFlintSteel) {
                checkPortalPattern(block.getLocation());
            }
        }
    }

    // ----------------- INTERACT -----------------
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        if (clicked.getType() == Material.REINFORCED_DEEPSLATE &&
                enchantedBlocks.contains(clicked.getLocation())) {

            if (event.getItem() != null &&
                    event.getItem().getItemMeta() != null &&
                    event.getItem().getItemMeta().getPersistentDataContainer().has(EnchantedFlintAndSteel.getKey(), PersistentDataType.BYTE)) {

                Location clickedLoc = clicked.getLocation();

                if (isValidPortalPattern(clickedLoc)) {
                    createNetherPortal(clickedLoc);
                    event.getPlayer().sendMessage("§5Portal activated! The Nether awaits...");
                }
            }
        }
    }

    // ----------------- BLOCK BREAK -----------------
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();

        if (block.getType() == Material.REINFORCED_DEEPSLATE &&
                enchantedBlocks.contains(loc)) {

            event.setCancelled(true);

            if (!breakingBlocks.containsKey(loc)) {
                breakingBlocks.put(loc, System.currentTimeMillis());
                event.getPlayer().sendMessage("§6Keep breaking for 3 seconds to remove...");

                NatureSMP.NATURE.delay(() -> {
                    if (breakingBlocks.containsKey(loc)) {
                        event.getPlayer().getInventory().addItem(EnchantedReinforcedDeepslate.getItem());
                        block.setType(Material.AIR);
                        enchantedBlocks.remove(loc);
                        breakingBlocks.remove(loc);
                        event.getPlayer().sendMessage("§aEnchanted Reinforced Deepslate returned!");
                    }
                }, 60);

            } else {
                long startTime = breakingBlocks.get(loc);
                if (System.currentTimeMillis() - startTime >= 3000) {
                    event.getPlayer().getInventory().addItem(EnchantedReinforcedDeepslate.getItem());
                    block.setType(Material.AIR);
                    enchantedBlocks.remove(loc);
                    breakingBlocks.remove(loc);
                    event.getPlayer().sendMessage("§aEnchanted Reinforced Deepslate returned!");
                }
            }
        }
    }

    // ----------------- PORTAL LOGIC -----------------
    private void checkPortalPattern(Location center) {
        if (isValidPortalPattern(center)) {
            // Only build when lit
        }
    }

    private boolean isValidPortalPattern(Location center) {
        int enchantedCount = 0;

        for (int x = -10; x <= 9; x++) {
            for (int y = -2; y <= 2; y++) {
                Location checkLoc = center.clone().add(x, y, 0);
                Block block = checkLoc.getBlock();

                if (block.getType() == Material.REINFORCED_DEEPSLATE &&
                        enchantedBlocks.contains(checkLoc)) {
                    enchantedCount++;
                }
            }
        }

        return enchantedCount >= 14;
    }

    private void createNetherPortal(Location center) {
        for (int x = -10; x <= 9; x++) {
            for (int y = -2; y <= 2; y++) {
                Location portalLoc = center.clone().add(x, y, 0);
                Block block = portalLoc.getBlock();

                if (block.getType() == Material.REINFORCED_DEEPSLATE) {
                    enchantedBlocks.remove(block.getLocation());
                    block.setType(Material.NETHER_PORTAL);
                }
            }
        }

        Location effectCenter = center.clone();
        effectCenter.getWorld().spawnParticle(Particle.PORTAL, effectCenter, 100, 10, 2.5, 0.5, 2);
        effectCenter.getWorld().spawnParticle(Particle.DRAGON_BREATH, effectCenter, 50, 8, 2, 0.5, 0.1);
    }
}
