package com.itsisaacio.natureSMP.entrails.classes;

import com.itsisaacio.natureSMP.NatureSMP;
import com.itsisaacio.natureSMP.entrails.BaseEntrail;
import com.itsisaacio.natureSMP.events.EntrailEvents;
import com.itsisaacio.natureSMP.Keys;
import com.itsisaacio.natureSMP.utils.Particles;
import com.itsisaacio.natureSMP.utils.Players;
import com.itsisaacio.natureSMP.utils.Utilities;
import org.bukkit.*;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class Frosted extends BaseEntrail {
    @Override
    public String getName() {
        return "Frosted";
    }

    @Override
    public String getColor() {
        return Utilities.hex("#27e1f2");
    }

    @Override
    public ArrayList<String> getAbilities() {
        return new ArrayList<>() {{
            add("Impulse");
            add("Frost Shield");
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
        return new Effect[0];
    }

    public static AttributeModifier speedMod = new AttributeModifier(
            Keys.natureKey, 0.2, AttributeModifier.Operation.ADD_NUMBER
    );

    @Override
    public void perform(Player player, int type) {
        if (Players.onCooldown(player, type))
        {
            player.sendMessage(getColor() + getAbilities().get(type) + Players.cooldownText(player, type));
            return;
        };

        if (type == 0) {
            Players.setCooldown(player, type, 30, false);
            player.sendMessage("You have used " + getColor() + getAbilities().get(type) + "§f!");
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);

            int timeout = 6;
            Location origin = player.getEyeLocation();
            ArrayList<Player> trusted = Players.getTrusted(player, true);

            new BukkitRunnable() {
                int loops = 0;
                float distance = 0;

                @Override
                public void run() {
                    if (loops++ >= (20 * timeout) || !player.getScoreboardTags().contains(getName())) {
                        cancel();
                        return;
                    }

                    distance += 0.75f;

                    Location start = origin.clone();
                    start.add(origin.getDirection().multiply(distance));

                    Location end = origin.clone();
                    end.add(origin.getDirection().multiply(distance + 0.1));

                    Particles.line(start, end, 1, 1,
                            0, 0 ,0, Particle.SCRAPE, 0,
                            location -> location.getBlock().isPassable(), null);

                    for (LivingEntity entity : end.getNearbyLivingEntities(0.5)) {
                        if (entity.equals(player)) continue;
                        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, 1, 1);

                        if (entity instanceof Player && trusted.contains(entity)) {
                            entity.addPotionEffect(PotionEffectType.SPEED.createEffect(100, 2));
                        } else {
                            Players.trueDamage(entity, player, 5);
                            entity.addPotionEffect(PotionEffectType.SLOWNESS.createEffect(100, 2));
                        }
                        entity.sendMessage("You have been affected with " + getColor() + getAbilities().get(type) + "§fby " + player.getName() + "§f!");

                        cancel();
                        return;
                    }
                }
            }.runTaskTimer(NatureSMP.NATURE, 0, 1);
        } else if (type == 1) {
            Players.setCooldown(player, type, 10, true);
            player.sendMessage("You have used " + getColor() + getAbilities().get(type) + "§f!");
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1, 1);

            int delay = 5;
            int duration = 10;

            EntrailEvents.SHIELDS.add(player);

            new BukkitRunnable() {
                int loops = 0;

                @Override
                public void run()
                {
                    if (loops++ >= (20 / delay * duration) || !Players.entityValid(player) || !player.getScoreboardTags().contains(getName()))
                    {
                        EntrailEvents.SHIELDS.remove(player);
                        Players.setCooldown(player, type, 45, false);
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1, 1);

                        cancel();
                        return;
                    }

                    Location location = player.getLocation().add(0, 2, 0);
                    Particles.sphere(location, 3, 0, 26, 0, 0, alt -> {
                        player.getWorld().spawnParticle(Particle.SNOWFLAKE, location, 1, 0, 3, 0, 0.0001);

                        return true;
                    });
                }
            }.runTaskTimer(NatureSMP.NATURE, 0, delay);
        }
    }
}
