package com.itsisaacio.natureSMP.events;

import com.itsisaacio.natureSMP.NatureSMP;
import com.itsisaacio.natureSMP.custom.Energizer;
import com.itsisaacio.natureSMP.custom.EnergizingPedestal;
import com.itsisaacio.natureSMP.Keys;
import com.itsisaacio.natureSMP.utils.DisplayUtils;
import com.itsisaacio.natureSMP.utils.Particles;
import com.itsisaacio.natureSMP.utils.Players;
import com.itsisaacio.natureSMP.utils.Utilities;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BlockEvents implements Listener {
    public static HashMap<Location, BlockData> REPLACED = new HashMap<>();
    public static HashMap<Location, Boolean> rechargers = new HashMap<>();

    public static void setOriginal(Location location)
    {
        if (REPLACED.containsKey(location))
        {
            BlockData data = REPLACED.get(location);
            location.getBlock().setBlockData(data);
            REPLACED.remove(location);
        }
    }

    @EventHandler
    public void onBlockPlaced(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        ItemStack item = event.getItemInHand();

        if (item.getPersistentDataContainer().has(EnergizingPedestal.getKey()))
        {
            rechargers.putIfAbsent(block.getLocation(), false);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (block.getType() == Material.BARRIER)
        {
            ArrayList<Location> toRemove = new ArrayList<>();
            rechargers.forEach((location, active) -> {
                if (block.getLocation().equals(location.getBlock().getLocation()))
                {
                    location.getWorld().playSound(location, Sound.BLOCK_GLASS_BREAK, 1, 1);
                    toRemove.add(location);
                }
            });
            for (Location display : toRemove)
                rechargers.remove(display);
        }

        List<MetadataValue> displayData = block.getMetadata("Display");
        if (!displayData.isEmpty())
        {
            for (MetadataValue value : displayData) {
                if (value.value() instanceof BlockDisplay display)
                    display.remove();
            }
        }
        List<MetadataValue> soundData = block.getMetadata("BreakSound");
        if (!soundData.isEmpty())
        {
            for (MetadataValue value : soundData) {
                if (value.value() instanceof Sound sound)
                    block.getWorld().playSound(block.getLocation(), sound, 1, 1);
            }
        }
    }

    public static void checkRecharger(Location recharger)
    {
        for (Item item : recharger.getNearbyEntitiesByType(Item.class, 1.5)) {
            UUID owner = item.getThrower();
            Player player = owner != null ? Bukkit.getPlayer(owner) : null;

            if (item.getItemStack().getPersistentDataContainer().has(Energizer.getKey()) && player != null)
            {
                item.remove();
                rechargers.replace(recharger, true);

                Block block = recharger.getBlock();
                TextDisplay healthText = DisplayUtils.textDisplay(block.getLocation().add(0.5, 1.6, 0.5), "§c400♥");
                TextDisplay playerText = DisplayUtils.textDisplay(block.getLocation().add(0.5, 1.1, 0.5), player.getName());
                TextDisplay timer = DisplayUtils.textDisplay(block.getLocation().add(0.5, 1.35, 0.5), "§a5:00");
                healthText.setBillboard(Display.Billboard.CENTER);
                playerText.setBillboard(Display.Billboard.CENTER);
                timer.setBillboard(Display.Billboard.CENTER);

                Creeper center = recharger.getWorld().spawn(block.getLocation().add(0.5, 0.5, 0.5), Creeper.class);
                center.setMetadata("Owner", new FixedMetadataValue(NatureSMP.NATURE, "Server"));
                center.setAI(false);
                center.setMaxHealth(800);
                center.setHealth(800);
                center.setInvisible(true);
                center.setGravity(false);
                center.setNoPhysics(true);
                center.setCollidable(false);
                center.setPersistent(true);
                center.setRemoveWhenFarAway(false);

                timer.getWorld().playSound(timer.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1, 1);

                Bukkit.removeBossBar(Keys.rechargeBarKey);
                KeyedBossBar bossBar = Bukkit.createBossBar(Keys.rechargeBarKey, player.getName() + "'s Energizing Ritual", BarColor.RED, BarStyle.SOLID);
                bossBar.setProgress(1);

                Runnable reset = () -> {
                    bossBar.removeAll();
                    Bukkit.removeBossBar(Keys.rechargeBarKey);

                    rechargers.replace(recharger, false);
                    healthText.remove();
                    playerText.remove();
                    timer.remove();
                    center.remove();
                };
                NatureSMP.ON_SHUTDOWN.add(reset);

                new BukkitRunnable()
                {
                    int timeLeft = 299;

                    @Override
                    public void run()
                    {
                        if (!center.isValid() || center.isDead() || !Players.entityValid(player) || !player.isOnline())
                        {
                            // Debug: Log why the ritual was canceled
                            if (!center.isValid()) {
                                player.sendMessage("§cRitual canceled: Center entity is invalid");
                            } else if (center.isDead()) {
                                player.sendMessage("§cRitual canceled: Center entity died");
                            } else if (!Players.entityValid(player)) {
                                player.sendMessage("§cRitual canceled: Player is not valid");
                            } else if (!player.isOnline()) {
                                player.sendMessage("§cRitual canceled: Player went offline");
                            }
                            
                            NatureSMP.ON_SHUTDOWN.remove(reset);
                            reset.run();

                            cancel();
                            return;
                        }

                        Location location = recharger.clone().add(0.5, 0.3, 0.5);
                        Particles.sphere(location, 2.5f, 0, 20, 0, 0, alt -> {
                            location.getWorld().spawnParticle(Particle.TRIAL_SPAWNER_DETECTION_OMINOUS, location, 1, 0, 0, 0, 0);
                            return true;
                        });

                        for (Player onlinePlayer : Bukkit.getOnlinePlayers())
                            bossBar.addPlayer(onlinePlayer);

                        healthText.text(Component.text("§c" + (int) center.getHealth() / 2 + "♥"));
                        bossBar.setProgress(Math.clamp(center.getHealth() / 400f, 0, 1));

                        String minutes = String.valueOf(timeLeft / 60);
                        int secs = timeLeft % 60;
                        String seconds = secs < 10 ? "0" + secs : String.valueOf(secs);

                        timer.text(Component.text("§a" + minutes + ":" + seconds));
                        timer.getWorld().playSound(timer.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1);

                        if (timeLeft-- <= 0)
                        {
                            NatureSMP.ON_SHUTDOWN.remove(reset);
                            reset.run();

                            PersistentDataContainer data = player.getPersistentDataContainer();
                            data.set(Keys.energyKey, PersistentDataType.INTEGER,
                                    Math.clamp(data.getOrDefault(Keys.energyKey, PersistentDataType.INTEGER, 3) + 3, 0, 5)
                            );

                            recharger.getWorld().playSound(recharger, Sound.ENTITY_IRON_GOLEM_REPAIR, 1, 1);
                            recharger.getWorld().spawnParticle(Particle.DUST, recharger.clone().add(0, 1, 0), 20, 2, 1, 2,
                                    new Particle.DustOptions(Color.YELLOW, 1));
                            player.sendMessage("Your §benergy §fhas been §arecharged§f!");

                            cancel();
                        }
                    }
                }.runTaskTimer(NatureSMP.NATURE, 0, 20);
            }
        }
    }
}
