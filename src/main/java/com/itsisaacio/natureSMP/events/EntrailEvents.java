package com.itsisaacio.natureSMP.events;

import com.itsisaacio.natureSMP.NatureSMP;
import com.itsisaacio.natureSMP.custom.EnergyCore;
import com.itsisaacio.natureSMP.custom.Swapper;
import com.itsisaacio.natureSMP.entrails.BaseEntrail;
import com.itsisaacio.natureSMP.entrails.EntrailList;
import com.itsisaacio.natureSMP.entrails.classes.Blazeborne;
import com.itsisaacio.natureSMP.utils.MathUtils;
import com.itsisaacio.natureSMP.Keys;
import com.itsisaacio.natureSMP.utils.Particles;
import com.itsisaacio.natureSMP.utils.Players;
import com.itsisaacio.natureSMP.utils.Utilities;
import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.entity.EntityDamageEvent.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.*;

public class EntrailEvents implements Listener {
    public static HashMap<Player, Integer> NATURE_HITS = new HashMap<>();
    public static HashMap<Player, Runnable> TRIDENTS = new HashMap<>();
    public static ArrayList<Player> SHIELDS = new ArrayList<>();
    public static ArrayList<Player> LEECH = new ArrayList<>();
    public static HashMap<Player, LivingEntity> LEECHED = new HashMap<>();
    public static ArrayList<Player> GROUND_BREAK = new ArrayList<>();
    public static HashMap<Player, LivingEntity> GROUND_BREAKING = new HashMap<>();

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onEntityTarget(EntityTargetEvent event) {
        Entity mob = event.getEntity();

        if (event.getTarget() instanceof Player player) {
            Entity summoner = mob.getType() == EntityType.VEX ? ((Vex) mob).getSummoner() : mob;

            List<MetadataValue> data = summoner != null ? summoner.getMetadata("Owner") : null;
            Player owner = (data != null && !data.isEmpty() && data.getFirst().value() instanceof Player) ? (Player) data.getFirst().value() : null;

            if (owner != null && owner.equals(player))
            {
                ArrayList<Player> trusted = Players.getTrusted(owner, true);
                Collection<Player> players = mob.getLocation().getNearbyPlayers(100, 10, 100);

                for (Player other : players) {
                    if (trusted.contains(other)) continue;

                    event.setTarget(other);
                    return;
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event)
    {
        Player player = event.getPlayer();

        if (event.isSneaking() && player.hasPotionEffect(PotionEffectType.WEAVING) && player.getScoreboardTags().contains("Blazeborne"))
        {
            if (player.getLocation().getBlock().getType() == Material.COBWEB)
                Blazeborne.burn(player.getLocation(), 8, 0.8f);
            if (player.getLocation().add(0, 1, 0).getBlock().getType() == Material.COBWEB)
                Blazeborne.burn(player.getLocation().add(0, 1, 0), 8, 0.8f);
        }
    }

    @EventHandler
    public void onPlayerEat(PlayerItemConsumeEvent event)
    {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (player.getScoreboardTags().contains("Naturen") && item.getType() == Material.ENCHANTED_GOLDEN_APPLE)
        {
            player.getWorld().spawnParticle(Particle.DUST_PLUME, player.getLocation().add(0, 1, 0), 20, 0.75, 0.5, 0.75, 0.001);
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 255));
        }
        else if (player.getScoreboardTags().contains("Ethereal") && item.getType() == Material.GOLDEN_APPLE && MathUtils.random.nextInt(0, 11) == 1)
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20, 4));
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();
        DamageCause cause = event.getCause();

        if (entity instanceof Player player)
        {
            Set<String> mob = player.getScoreboardTags();

            if (mob.contains("Ethereal") && cause == DamageCause.FALL)
            {
                Location location = player.getLocation().add(0, 0.2, 0);
                Particles.circle(location, 0, 1, 0, 16, 0, alt -> {
                    location.getWorld().spawnParticle(Particle.DUST_PLUME, location, 1, 0, 0, 0, 0.01);
                    return true;
                });
                event.setCancelled(true);
            }

            else if (mob.contains("Frosted") && (cause == DamageCause.FIRE || cause == DamageCause.FIRE_TICK || cause == DamageCause.CAMPFIRE || cause == DamageCause.LAVA))
                event.setDamage(event.getDamage() * 3);
            else if (SHIELDS.contains(player) && MathUtils.random.nextInt(1, 5) == 1)
                event.setCancelled(true);
            else if (mob.contains("Naturen") && player.hasPotionEffect(PotionEffectType.INFESTED) && MathUtils.random.nextInt(1, 11) == 1)
                player.heal(1, EntityRegainHealthEvent.RegainReason.MAGIC_REGEN);
        }
    }

    @EventHandler
    public void onBlockDamageEntity(EntityDamageByBlockEvent event) {
        Entity entity = event.getEntity();
        Block block = event.getDamager();

        if (entity instanceof Player player) {
            Set<String> mob = player.getScoreboardTags();

            if (mob.contains("Ethereal") && entity.getFallDistance() >= 1 && block != null && block.getType() == Material.POINTED_DRIPSTONE) {
                Location location = player.getLocation().add(0, 0.2, 0);
                Particles.circle(location, 0, 1, 0, 16, 0, alt -> {
                    location.getWorld().spawnParticle(Particle.DUST_PLUME, location, 1, 0, 0, 0, 0.01);
                    return true;
                });

                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity dead = event.getEntity();
        Player killer = dead.getKiller();
        Set<String> tags = killer != null ? killer.getScoreboardTags() : null;

        if (tags != null && tags.contains("Blazeborne"))
        {
            dead.getWorld().dropItem(dead.getLocation(), new ItemStack(Material.GOLD_NUGGET, MathUtils.random.nextInt(9, 35)));
            dead.getWorld().dropItem(dead.getLocation(), new ItemStack(Material.GOLD_NUGGET, MathUtils.random.nextInt(0, 4)));
        }

        List<MetadataValue> data = dead.getMetadata("Owner");
        if (dead.getPersistentDataContainer().has(Keys.ownerKey) || !data.isEmpty())
        {
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }

    @EventHandler
    public void onPotionEffect(EntityPotionEffectEvent event)
    {
        PotionEffect effect = event.getNewEffect();
        Entity entity = event.getEntity();

        if (entity instanceof Player player && effect != null)
        {
            Set<String> tags = player.getScoreboardTags();

            if (tags.contains("Atlantean") && effect.getType() == PotionEffectType.DOLPHINS_GRACE) {
                if (Utilities.potionTag(player, "AtlanteanGrace"))
                {
                    event.setCancelled(true);
                    player.addPotionEffect(effect.withDuration(effect.getDuration() * 2));
                }
            }
            else if (tags.contains("Frosted") && effect.getType() == PotionEffectType.NIGHT_VISION) {
                if (Utilities.potionTag(player, "FrostedNight")) {
                    player.addPotionEffect(effect.withDuration(300));

                    for (LivingEntity enemy : Players.getEnemies(player, null, 30))
                        enemy.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 300, 0));

                    event.setCancelled(true);
                }
            }
            else if (tags.contains("Naturen")) {
                if (effect.getType() == PotionEffectType.ABSORPTION && Utilities.potionTag(player, "NaturenHP"))
                {
                    player.addPotionEffect(effect.withAmplifier((effect.getAmplifier() + 1) * 2 - 1));
                    event.setCancelled(true);
                }
                if (effect.getType() == PotionEffectType.STRENGTH && Utilities.potionTag(player, "NaturenStrength"))
                {
                    player.addPotionEffect(effect.withDuration(effect.getDuration() / 2));
                    event.setCancelled(true);
                }
                if (effect.getType() == PotionEffectType.SPEED && Utilities.potionTag(player, "NaturenSpeed"))
                {
                    player.addPotionEffect(effect.withDuration(effect.getDuration() / 2));
                    event.setCancelled(true);
                }
            }
            else if (tags.contains("Ethereal")) {
                if (effect.getType() == PotionEffectType.SLOW_FALLING) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, effect.getDuration(), 1));
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event)
    {
        Projectile entity = event.getEntity();
        ProjectileSource shooter = entity.getShooter();
        if (shooter == null) return;

        if (shooter instanceof Player player && player.getScoreboardTags().contains("Ethereal") && entity.getType() == EntityType.WIND_CHARGE)
            player.heal(2, EntityRegainHealthEvent.RegainReason.MAGIC_REGEN);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        if (item.getType() == Material.DRAGON_EGG)
        {
            event.setCancelled(true);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.6f, 1);

            BaseEntrail entrail = NatureSMP.getEntrail(player);
            int index = EntrailList.entrails.indexOf(entrail) + 1;
            if (index >= EntrailList.entrails.size()) index = index - EntrailList.entrails.size();
            NatureSMP.setEntrail(player, EntrailList.entrails.get(index));
        }
        if (event.getAction().isLeftClick() && TRIDENTS.containsKey(player)) {
            TRIDENTS.get(player).run();
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null
                && item.getType() == Material.WATER_BUCKET) {
            World world = event.getClickedBlock().getWorld();

            if (player.getScoreboardTags().contains("Atlantean") && world.getEnvironment() == World.Environment.NETHER) {
                item.setType(Material.BUCKET);
                event.setCancelled(true);
                event.getClickedBlock().getRelative(event.getBlockFace()).setType(Material.WATER);
            }
        } else if (event.getAction().isRightClick()) {
            PersistentDataContainerView data = item.getPersistentDataContainer();

            if (data.has(Swapper.getKey()))
                Swapper.interact(player, item);
            else if (data.has(EnergyCore.getKey()))
                EnergyCore.interact(player, item);
        }
    }

    @EventHandler
    public void onEntityDamageEntity(EntityDamageByEntityEvent event)
    {
        Entity tookDamage = event.getEntity();
        Entity attacker = event.getDamager();
        double finalDamage = 0;

        if (tookDamage instanceof LivingEntity damagedEntity && !damagedEntity.equals(attacker))
        {
            if (attacker instanceof Player player)
            {
                Set<String> entrail = player.getScoreboardTags();
                if (entrail.contains("Blazeborne") && event.getCause() == DamageCause.ENTITY_SWEEP_ATTACK && MathUtils.random.nextInt(1, 4) == 1)
                    damagedEntity.setFireTicks(80);

                if (entrail.contains("Frosted") && player.isUnderWater())
                    damagedEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 0));

                if (entrail.contains("Ethereal") && player.getFallDistance() >= 7 && !MainEvents.GROUNDED.getOrDefault(player, true))
                    event.setDamage(event.getDamage() * 1.5);

                if (tookDamage instanceof Player damaged && attacker.getScoreboardTags().contains("Naturen"))
                {
                    if (NATURE_HITS.containsKey(damaged) && NATURE_HITS.get(damaged) >= 20)
                    {
                        player.addPotionEffect(PotionEffectType.POISON.createEffect(200, 0));
                        NATURE_HITS.replace(damaged, 0);
                    }
                    else if (NATURE_HITS.containsKey(damaged))
                        NATURE_HITS.replace(damaged, NATURE_HITS.get(damaged) + 1);
                    else
                        NATURE_HITS.put(damaged, 1);
                }

                if (LEECH.contains(player))
                {
                    LEECH.remove(player);
                    LEECHED.putIfAbsent(player, damagedEntity);
                }

                if (GROUND_BREAK.contains(player))
                {
                    GROUND_BREAK.remove(player);
                    GROUND_BREAKING.putIfAbsent(player, damagedEntity);
                }
            } else if (attacker instanceof Arrow arrow && arrow.getShooter() instanceof Player player)
            {
                Set<String> entrail = player.getScoreboardTags();
                if (entrail.contains("Frosted") && MathUtils.random.nextInt(1, 11) == 1)
                    damagedEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 400, 0));
            }
        }
    }
}