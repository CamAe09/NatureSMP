package com.itsisaacio.natureSMP.entrails.classes;

import com.itsisaacio.natureSMP.NatureSMP;
import com.itsisaacio.natureSMP.entrails.BaseEntrail;
import com.itsisaacio.natureSMP.utils.Particles;
import com.itsisaacio.natureSMP.utils.Players;
import com.itsisaacio.natureSMP.utils.Utilities;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Hypnotic extends BaseEntrail {
    private static final Map<Player, Long> sleepingPlayers = new HashMap<>();

    @Override
    public String getName() {
        return "Hypnotic";
    }

    @Override
    public String getColor() {
        return Utilities.hex("#9d4edd");
    }

    @Override
    public ArrayList<String> getAbilities() {
        return new ArrayList<>() {{
            add("Hypnotic Beam");
            add("Sleepy Dome");
        }};
    }

    @Override
    public ArrayList<String> getLore() {
        return new ArrayList<>() {{
            add("§f  " + getColor() + getAbilities().get(0));
            add("§f  Shoots a §dbeam §fthat puts targets in a §esleeping pose");
            add("§f  for §a5 §fseconds, making them §cunable to move§f.");
            add("");
            add("§f  " + getColor() + getAbilities().get(1));
            add("§f  Creates a §ddome of particles §fwhere §btrusted users §fget");
            add("§f  §bHaste §a2 §fwhile §cnon-trusted users §fget fully slowed");
            add("§f  and can't leave the dome. Lasts §a8 §fseconds.");
        }};
    }

    @Override
    public ArrayList<String> getPassive() {
        return new ArrayList<>() {{
            add("§f  During nighttime, gain §bSpeed §a1 §fand §cStrength §a1§f.");
            add("§f  Enemies within §a5 §fblocks sometimes get §eNausea §ffor §a2 §fseconds.");
            add("§f  Standing still for §a10 §fseconds heals §c1 heart §fand §61 hunger§f.");
            add("§c  Fragile Wakefulness §f- You gain §7Mining Fatigue §a1 §fin sunlight.");
        }};
    }

    @Override
    public Effect[] getEffects() {
        return new Effect[] {
                // No constant effects - nighttime effects handled in main loop
        };
    }

    @Override
    public void perform(Player player, int type) {
        if (Players.onCooldown(player, type))
        {
            player.sendMessage(getColor() + getAbilities().get(type) + Players.cooldownText(player, type));
            return;
        }

        if (!checkPhase1Restriction(player, type)) {
            return;
        }

        if (type == 0) {
            // Hypnotic Beam - puts targets to sleep
            if (Players.onCooldown(player, type)) return;

            Location start = player.getEyeLocation();
            Vector direction = start.getDirection();

            // Create beam effect with particles
            new BukkitRunnable() {
                double distance = 0;
                final double maxDistance = 15;

                @Override
                public void run() {
                    if (distance >= maxDistance) {
                        cancel();
                        return;
                    }

                    Location current = start.clone().add(direction.clone().multiply(distance));

                    // Purple particles for hypnotic beam
                    current.getWorld().spawnParticle(Particle.DUST, current, 3, 0.1, 0.1, 0.1,
                            new Particle.DustOptions(Color.fromRGB(157, 78, 221), 1.0f));

                    // Check for entities at this location
                    for (Entity entity : current.getWorld().getNearbyEntities(current, 0.8, 0.8, 0.8)) {
                        if (entity instanceof Player target && !target.equals(player)) {
                            // Put target to sleep (freeze them)
                            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
                            target.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 100, 1));
                            target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, -10));

                            sleepingPlayers.put(target, System.currentTimeMillis());

                            // Visual effect on target
                            target.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0),
                                    20, 0.5, 0.5, 0.5, new Particle.DustOptions(Color.fromRGB(157, 78, 221), 1.0f));

                            cancel();
                            return;
                        }
                    }

                    distance += 0.5;
                }
            }.runTaskTimer(NatureSMP.NATURE, 0, 1);

            Players.setCooldown(player, 0, 200, true);

        } else if (type == 1) {
            // Sleepy Dome - creates area effect for trusted/non-trusted players
            if (Players.onCooldown(player, type)) {
                player.sendMessage(getColor() + getAbilities().get(type) + Players.cooldownText(player, type));
                return;
            }

            player.sendMessage("You have used " + getColor() + getAbilities().get(type) + "§f!");
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 0.8f);

            Location center = player.getLocation();
            ArrayList<Player> trusted = Players.getTrusted(player, true);
            ArrayList<Player> trappedPlayers = new ArrayList<>();
            final double radius = 8.0;
            final int duration = 160; // 8 seconds in ticks

            // Find all players in dome at start
            for (Player nearbyPlayer : center.getWorld().getNearbyPlayers(center, radius)) {
                if (nearbyPlayer.getLocation().distance(center) <= radius) {
                    trappedPlayers.add(nearbyPlayer);
                }
            }

            new BukkitRunnable() {
                int ticks = 0;

                @Override
                public void run() {
                    if (ticks >= duration || !player.getScoreboardTags().contains(getName())) {
                        // Release all trapped players
                        trappedPlayers.clear();
                        cancel();
                        return;
                    }

                    // Create dome particles
                    for (int i = 0; i < 360; i += 10) {
                        for (double y = 0; y <= radius; y++) {
                            double angle = Math.toRadians(i);
                            double x = center.getX() + radius * Math.cos(angle);
                            double z = center.getZ() + radius * Math.sin(angle);
                            Location particleLocation = new Location(center.getWorld(), x, center.getY() + y, z);

                            center.getWorld().spawnParticle(Particle.DUST, particleLocation, 1, 0, 0, 0,
                                    new Particle.DustOptions(Color.fromRGB(128, 0, 128), 1.0f));
                        }
                    }

                    // Apply effects and prevent escape for trapped players
                    for (Player trappedPlayer : new ArrayList<>(trappedPlayers)) {
                        if (!trappedPlayer.isOnline() || trappedPlayer.isDead()) {
                            trappedPlayers.remove(trappedPlayer);
                            continue;
                        }

                        // Check if player is trying to leave dome
                        if (trappedPlayer.getLocation().distance(center) > radius) {
                            // Teleport them back to edge of dome
                            Vector direction = trappedPlayer.getLocation().toVector().subtract(center.toVector()).normalize();
                            Location edgeLocation = center.clone().add(direction.multiply(radius - 0.5));
                            trappedPlayer.teleport(edgeLocation);
                        }

                        // Apply effects
                        if (trusted.contains(trappedPlayer)) {
                            trappedPlayer.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 200, -10));
                        } else {
                            trappedPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 2));
                            trappedPlayer.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 200, 2));
                        }
                    }

                    ticks++;
                }
            }.runTaskTimer(NatureSMP.NATURE, 0, 1);

            Players.setCooldown(player, 1, 300, true);
        }
    }

    @Override
    public void secondary(Player player) {
        if (checkPhase1Restriction(player, 2)) return;
    }


    public boolean checkPhase1Restriction(Player player, int type) {
        if (NatureSMP.phase == 1 && type == 1) { // Assuming type 1 is the second ability
            player.sendMessage(ChatColor.RED + "You are not allowed to use this during Phase 1.");
            // Refund energy or handle it as per your game mechanics
            // For now, let's assume energy is handled elsewhere or by returning false
            return false;
        }
        return true;
    }
}