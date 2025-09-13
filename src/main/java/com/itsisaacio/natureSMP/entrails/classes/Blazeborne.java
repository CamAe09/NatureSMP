package com.itsisaacio.natureSMP.entrails.classes;

import com.itsisaacio.natureSMP.NatureSMP;
import com.itsisaacio.natureSMP.entrails.BaseEntrail;
import com.itsisaacio.natureSMP.Keys;
import com.itsisaacio.natureSMP.utils.BlockUtils;
import com.itsisaacio.natureSMP.utils.Particles;
import com.itsisaacio.natureSMP.utils.Players;
import com.itsisaacio.natureSMP.utils.Utilities;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;

public class Blazeborne extends BaseEntrail {
    @Override
    public String getName() {
        return "Blazeborne";
    }

    @Override
    public String getColor() {
        return Utilities.hex("#fc6f03");
    }

    @Override
    public ArrayList<String> getAbilities() {
        return new ArrayList<>() {{
            add("Meteor Rain");
            add("Heat");
        }};
    }

    // §c Red
    // §a Lime
    // §b Aqua
    // §e Yellow
    // §f White
    @Override
    public ArrayList<String> getLore() {
        return new ArrayList<>() {{
            add("§f  " + getColor() + getAbilities().getFirst());
            add("§f  Rains down §c10 meteors §faround you within a §a7 §fblock");
            add("§f  radius. Each meteor has a §c5x5x5 blast radius §fdoing §c5");
            add("§f  damage regardless of armor. Destroys cobwebs and clears");
            add("§f  negative effects from §btrusted players§f.");
            add("");
            add("§f  " + getColor() + getAbilities().get(1));
            add("§f  Creates a §cburning aura §fthat destroys cobwebs and");
            add("§f  sets enemies on fire for §a5 §fseconds§f.");
        }};
    }

    @Override
    public ArrayList<String> getPassive() {
        return new ArrayList<>() {{
            add("§f  When you're §ebelow §a4 §ehearts§f, gain §bSpeed §a2§f.");
        }};
    }

    @Override
    public Effect[] getEffects() {
        return new Effect[] {
                new Effect(PotionEffectType.FIRE_RESISTANCE, 0),
        };
    }

    public static AttributeModifier speedMod = new AttributeModifier(
            Keys.natureKey, 0.025, AttributeModifier.Operation.ADD_NUMBER
    );
    public static AttributeModifier damageMod = new AttributeModifier(
            Keys.natureKey, 1, AttributeModifier.Operation.ADD_NUMBER
    );

    public static void burn(Location block, int particles, float volume)
    {
        block.getBlock().setType(Material.AIR);
        block.getWorld().playSound(block, Sound.ENTITY_GENERIC_BURN, volume, 1);
        block.getWorld().spawnParticle(Particle.FLAME, block.add(0, 0.5, 0), particles, 0.3, 0.3, 0.3, 0.001);
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

        if (type == 0) { // Meteor Rain
            Players.setCooldown(player, type, 90, true);
            player.sendMessage("You have used " + getColor() + getAbilities().get(type) + "§f!");
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1.0f, 0.8f);

            // Spawn 10 meteors
            for (int i = 0; i < 10; i++) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // Random location within 7 block radius
                        double angle = Math.random() * 2 * Math.PI;
                        double radius = Math.random() * 7;
                        double x = player.getLocation().getX() + radius * Math.cos(angle);
                        double z = player.getLocation().getZ() + radius * Math.sin(angle);
                        double y = player.getLocation().getY() + 20; // Start high up

                        Location meteorStart = new Location(player.getWorld(), x, y, z);
                        Location meteorTarget = new Location(player.getWorld(), x, player.getLocation().getY(), z);

                        // Create small block display as meteor
                        BlockDisplay meteor = (BlockDisplay) player.getWorld().spawnEntity(meteorStart, EntityType.BLOCK_DISPLAY);
                        meteor.setBlock(Material.MAGMA_BLOCK.createBlockData());
                        meteor.setMetadata("Owner", new FixedMetadataValue(NatureSMP.NATURE, player));

                        // Make it small
                        org.bukkit.util.Transformation transform = meteor.getTransformation();
                        transform.getScale().set(0.3f, 0.3f, 0.3f);
                        meteor.setTransformation(transform);

                        // Animate meteor falling
                        new BukkitRunnable() {
                            int ticks = 0;
                            final int fallDuration = 40; // 2 seconds

                            @Override
                            public void run() {
                                if (ticks >= fallDuration || !meteor.isValid()) {
                                    // Meteor impact
                                    Location impact = meteor.getLocation();

                                    // Visual effects
                                    player.getWorld().playSound(impact, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.8f);
                                    player.getWorld().spawnParticle(Particle.EXPLOSION, impact, 5, 1, 1, 1, 0.1);
                                    player.getWorld().spawnParticle(Particle.FLAME, impact, 20, 2, 2, 2, 0.1);
                                    player.getWorld().spawnParticle(Particle.LAVA, impact, 10, 2, 2, 2, 0.1);

                                    // Destroy cobwebs in 5x5x5 area
                                    for (Block block : BlockUtils.getNearbyBlocks(impact, 2, 2, 2)) {
                                        if (block.getType() == Material.COBWEB) {
                                            Blazeborne.burn(block.getLocation(), 5, 0.8f);
                                        }
                                    }

                                    // Damage/heal entities in 5x5x5 area
                                    Collection<Player> trusted = Players.getTrusted(player, true);
                                    for (LivingEntity entity : impact.getNearbyLivingEntities(2.5)) {
                                        if (entity instanceof Player targetPlayer && trusted.contains(targetPlayer)) {
                                            // Clear negative effects for trusted players
                                            targetPlayer.removePotionEffect(PotionEffectType.POISON);
                                            targetPlayer.removePotionEffect(PotionEffectType.WITHER);
                                            targetPlayer.removePotionEffect(PotionEffectType.SLOWNESS);
                                            targetPlayer.removePotionEffect(PotionEffectType.WEAKNESS);
                                            targetPlayer.removePotionEffect(PotionEffectType.NAUSEA);
                                            targetPlayer.removePotionEffect(PotionEffectType.HUNGER);
                                            targetPlayer.removePotionEffect(PotionEffectType.MINING_FATIGUE);
                                            targetPlayer.removePotionEffect(PotionEffectType.BLINDNESS);
                                        } else if (!entity.equals(player)) {
                                            // 5 damage regardless of armor
                                            Players.trueDamage(entity, player, 5);
                                        }
                                    }

                                    meteor.remove();
                                    cancel();
                                    return;
                                }

                                // Move meteor down
                                double progress = (double) ticks / fallDuration;
                                double currentY = meteorStart.getY() - (meteorStart.getY() - meteorTarget.getY()) * progress;
                                meteor.teleport(new Location(player.getWorld(), meteorStart.getX(), currentY, meteorStart.getZ()));

                                // Trail particles
                                meteor.getWorld().spawnParticle(Particle.FLAME, meteor.getLocation(), 3, 0.1, 0.1, 0.1, 0.01);
                                meteor.getWorld().spawnParticle(Particle.SMOKE, meteor.getLocation(), 2, 0.1, 0.1, 0.1, 0.01);

                                ticks++;
                            }
                        }.runTaskTimer(NatureSMP.NATURE, 0, 1);
                    }
                }.runTaskLater(NatureSMP.NATURE, i * 3L); // Stagger meteor spawns
            }

        } else if (type == 1) { // Heat (old first ability)
            Players.setCooldown(player, type, 5, true);
            player.sendMessage("You have used " + getColor() + getAbilities().get(type) + "§f!");
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);

            new BukkitRunnable()
            {
                int index = 0;

                @Override
                public void run()
                {
                    if (index++ == 21 || !player.getScoreboardTags().contains(getName())) // 5 seconds
                    {
                        Players.setCooldown(player, type, 55, false);
                        cancel();
                        return;
                    }
                    if (index % 2 == 1)
                    {
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.6f, 1.2f);

                        for (Block block : BlockUtils.getNearbyBlocks(player.getLocation(), 4))
                        {
                            if (block.getType() == Material.COBWEB)
                                Blazeborne.burn(block.getLocation(), 3, 0.5f);
                        }
                    }

                    Collection<LivingEntity> players = Players.getEnemies(player, null, 4);
                    for (LivingEntity other : players)
                        other.setFireTicks(100);

                    Location location = player.getLocation();

                    for (int i = 1; i <= 3; i++)
                    {
                        Particles.sphere(location, i, 0, i * 8, 0, 0, alt -> {
                            location.getWorld().spawnParticle(Particle.FLAME, location, 2, 0.1, 0.1, 0.1, 0.001);
                            return true;
                        });
                    }
                }
            }.runTaskTimer(NatureSMP.NATURE, 0, 5);
        }
    }

    @Override
    public void secondary(Player player) {

    }
}