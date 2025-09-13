package com.itsisaacio.natureSMP.entrails.classes;

import com.itsisaacio.natureSMP.NatureSMP;
import com.itsisaacio.natureSMP.entrails.BaseEntrail;
import com.itsisaacio.natureSMP.events.EntrailEvents;
import com.itsisaacio.natureSMP.utils.DisplayUtils;
import com.itsisaacio.natureSMP.utils.Particles;
import com.itsisaacio.natureSMP.utils.Players;
import com.itsisaacio.natureSMP.utils.Utilities;
import org.bukkit.*;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Naturen extends BaseEntrail {
    @Override
    public String getName() {
        return "Naturen";
    }

    @Override
    public String getColor() {
        return Utilities.hex("#00b041");
    }

    @Override
    public ArrayList<String> getAbilities() {
        return new ArrayList<>() {{
            add("Vine Whip");
            add("Leech");
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
            add("§f  " + getColor() + getAbilities().get(0));
            add("§f  Gives §cplayers §caround you §bGlowing§f, and gives you");
            add("§b  Strength §a2 §fand §bSpeed §a3 §ffor §a15 §fseconds§f.");
            add("");
            add("§f  " + getColor() + getAbilities().get(1));
            add("§f  You take §bno damage §ffor the next §a4 §ehits §fagainst you.");
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

        };
    }

    public void pull(LivingEntity entity, Location location)
    {
        Vector direction = location.toVector().subtract(entity.getLocation().toVector()).normalize();
        entity.setVelocity(direction);
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
            Players.setCooldown(player, type, 30, false);
            player.sendMessage("You have used " + getColor() + getAbilities().get(type) + "§f!");
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CAVE_VINES_BREAK, 1.0f, 1.0f);
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_VINE_STEP, 1.0f, 1.0f);

            Location eye = player.getEyeLocation().subtract(0, 0.5, 0);
            LivingEntity entity = null;
            final Location[] location = {null, null};
            boolean toPlayer = false;

            int distance = 0;
            for (int i = 0; i <= 46; i++)
            {
                distance = i;
                location[1] = eye.clone().add(eye.getDirection().multiply(i));

                if (!location[1].getBlock().isPassable())
                {
                    entity = player;
                    location[0] = location[1].getBlock().getLocation();

                    break;
                }
                else {
                    LivingEntity target = null;
                    for (LivingEntity hit : location[1].getNearbyLivingEntities(0.5)) {
                        if (hit.equals(player)) continue;

                        target = hit;
                        break;
                    }
                    if (target != null)
                    {
                        entity = target;
                        location[0] = player.getLocation();
                        toPlayer = true;
                        break;
                    }
                }
            }
            if (entity != null)
            {
                LivingEntity finalEntity = entity;
                boolean finalToPlayer = toPlayer;
                float[] dist = {distance};

                new BukkitRunnable() {
                    int loop = 0;

                    @Override
                    public void run() {
                        dist[0] -= 0.93f;
                        if (dist[0] <= 0)
                        {
                            cancel();
                            return;
                        }
                        if (loop++ % 4 == 0)
                        {
                            Location leafLocation = location[1];
                            player.getWorld().spawnParticle(Particle.DUST, leafLocation, 1, 0, 0, 0, new Particle.DustOptions(Color.GREEN, 1));
                        }

                        finalEntity.getWorld().playSound(finalEntity.getLocation(), Sound.BLOCK_MOSS_CARPET_STEP, 1, 0.5f);
                        if (finalToPlayer) location[0] = player.getLocation();
                        pull(finalEntity, location[0]);
                    }
                }.runTaskTimer(NatureSMP.NATURE, 0, 1);
            }
        } else if (type == 1) {
            Players.setCooldown(player, type, 999999999, true);
            player.sendMessage("You have used " + getColor() + getAbilities().get(type) + "§f!");
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1, 1);

            int delay = 2;
            int duration = 30;

            EntrailEvents.LEECH.add(player);

            new BukkitRunnable() {
                int loops = 0;
                BlockDisplay leech;
                LivingEntity attached = null;

                final Runnable reset = () -> {
                    EntrailEvents.LEECH.remove(player);
                    EntrailEvents.LEECHED.remove(player);

                    if (leech != null) {
                        attached.removePassenger(leech);
                        leech.remove();
                    }
                };
                boolean added = false;

                @Override
                public void run()
                {
                    if (!added) {
                        added = true;
                        NatureSMP.ON_SHUTDOWN.add(reset);
                    }

                    if (loops >= (20f / delay * duration) || !Players.entityValid(player) || (attached != null && !Players.entityValid(attached)) || !player.getScoreboardTags().contains(getName()))
                    {
                        NatureSMP.ON_SHUTDOWN.remove(reset);
                        reset.run();

                        cancel();
                        return;
                    }

                    if (EntrailEvents.LEECHED.containsKey(player))
                    {
                        if (attached == null)
                        {
                            Players.setCooldown(player, type, 1, true);
                            Players.setCooldown(player, type, 210, false);
                            attached = EntrailEvents.LEECHED.get(player);
                            attached.sendMessage("You have been affected with " + getColor() + getAbilities().get(type) + " §fby " + player.getName() + "§f!");
                        }
                        if (leech == null) {
                            leech = DisplayUtils.blockDisplay(attached.getLocation().toVector().toLocation(attached.getWorld()), Material.SPORE_BLOSSOM);

                            Transformation newTranslate = leech.getTransformation();
                            Vector3f translate = newTranslate.getTranslation();
                            translate.sub(0.5f, 0.6f, 0.5f);
                            newTranslate.getTranslation().set(translate);
                            leech.setTransformation(newTranslate);
                            attached.addPassenger(leech);
                        }

                        if (loops++ % 30 == 0)
                        {
                            int damage = 1;
                            if (player.getWorld().isDayTime()) damage = 2;
                            Players.trueDamage(attached, player, damage);
                            player.setHealth(Math.min(player.getHealth() + 1, player.getMaxHealth()));
                        }
                    }
                }
            }.runTaskTimer(NatureSMP.NATURE, 0, delay);
        }
    }

    @Override
    public void secondary(Player player) {
        if (checkPhase1Restriction(player, 2)) return;
    }
}