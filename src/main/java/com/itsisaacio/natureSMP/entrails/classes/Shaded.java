package com.itsisaacio.natureSMP.entrails.classes;

import com.itsisaacio.natureSMP.NatureSMP;
import com.itsisaacio.natureSMP.entrails.BaseEntrail;
import com.itsisaacio.natureSMP.events.MainEvents;
import com.itsisaacio.natureSMP.Keys;
import com.itsisaacio.natureSMP.utils.DisplayUtils;
import com.itsisaacio.natureSMP.utils.Particles;
import com.itsisaacio.natureSMP.utils.Players;
import com.itsisaacio.natureSMP.utils.Utilities;
import org.bukkit.*;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Shaded extends BaseEntrail {
    @Override
    public String getName() {
        return "Shaded";
    }

    @Override
    public String getColor() {
        return Utilities.hex("#5900d6");
    }

    @Override
    public ArrayList<String> getAbilities() {
        return new ArrayList<>() {{
            add("Shadow Bomb");
            add("Cloak");
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
            add("§b  Strength §a2 §fand §bSpeed §a3 §ffor §a15 §seconds§f.");
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

    public static AttributeModifier speedMod = new AttributeModifier(
            Keys.natureKey, 0.01, AttributeModifier.Operation.ADD_NUMBER
    );
    public static AttributeModifier sneakMod = new AttributeModifier(
            Keys.natureKey, 0.7, AttributeModifier.Operation.ADD_NUMBER
    );
    public static AttributeModifier darkMod = new AttributeModifier(
            Keys.natureKey, 0.1, AttributeModifier.Operation.ADD_NUMBER
    );
    public static AttributeModifier healthMod = new AttributeModifier(
            Keys.natureKey, -6, AttributeModifier.Operation.ADD_NUMBER
    );

    @Override
    public void perform(Player player, int type) {
        if (Players.onCooldown(player, type))
        {
            player.sendMessage(getColor() + getAbilities().get(type) + Players.cooldownText(player, type));
            return;
        }

        if (checkPhase1Restriction(player, type)) {
            return;
        }

        if (type == 0) {
            Players.setCooldown(player, type, 15, true);
            player.sendMessage("You have used " + getColor() + getAbilities().get(type) + "§f!");
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 0.6f, 1.2f);

            Arrow arrow = player.launchProjectile(Arrow.class);
            arrow.setDamage(0);
            arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
            arrow.setInvisible(true);

            BlockDisplay tnt = DisplayUtils.blockDisplay(arrow.getLocation().toVector().toLocation(arrow.getWorld()), Material.OBSIDIAN, -0.3f);
            Transformation newTranslate = tnt.getTransformation();
            Vector3f translate = newTranslate.getTranslation();
            translate.sub(0.5f, 1f, 0.5f);
            newTranslate.getTranslation().set(translate);
            tnt.setTransformation(newTranslate);

            BlockDisplay glass = DisplayUtils.blockDisplay(arrow.getLocation().toVector().toLocation(arrow.getWorld()), Material.TINTED_GLASS);
            Transformation newTranslate2 = glass.getTransformation();
            Vector3f translate2 = newTranslate2.getTranslation();
            translate2.sub(0.5f, 1f, 0.5f);
            newTranslate2.getTranslation().set(translate2);
            glass.setTransformation(newTranslate2);

            arrow.addPassenger(tnt);
            tnt.addPassenger(glass);

            Runnable reset = () -> {
                arrow.remove();
                tnt.remove();
                glass.remove();

                Players.setCooldown(player, type, 30, false);
            };
            NatureSMP.ON_SHUTDOWN.add(reset);

            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if (!arrow.isValid() || arrow.getVelocity().isZero())
                    {
                        NatureSMP.NATURE.delay(() -> {
                            NatureSMP.ON_SHUTDOWN.remove(reset);

                            Location location = tnt.getLocation();
                            location.createExplosion(player, 0, false);

                            for (LivingEntity entity : location.getNearbyLivingEntities(5)) {
                                Vector velocity = location.toVector().subtract(entity.getLocation().toVector()).normalize().multiply(-4).setY(1);
                                entity.setVelocity(velocity);
                            }
                            Particles.sphere(location, 5, 5, 26, 0, 0, alt -> {
                                location.getWorld().spawnParticle(Particle.FALLING_OBSIDIAN_TEAR, location, 1, 0, 0, 0, 0.001);
                                return true;
                            });

                            reset.run();
                        }, 20);

                        cancel();
                    }
                }
            }.runTaskTimer(NatureSMP.NATURE, 0, 5);
        } else if (type == 1) {
            Players.setCooldown(player, type, 10, true);
            player.sendMessage("You have used " + getColor() + getAbilities().get(type) + "§f!");
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 0.6f, 1.2f);

            ArrayList<Player> trusted = Players.getTrusted(player, false);
            MainEvents.INVISIBLE.add(player);
            for (Player other : Bukkit.getOnlinePlayers())
            {
                if (trusted.contains(other)) continue;
                other.hidePlayer(NatureSMP.NATURE, player);
            }

            Runnable reset = () -> {
                MainEvents.INVISIBLE.remove(player);
                for (Player other : Bukkit.getOnlinePlayers())
                    other.showPlayer(NatureSMP.NATURE, player);

                Players.setCooldown(player, type, 90, false);
            };
            NatureSMP.ON_SHUTDOWN.add(reset);

            int delay = 5;
            int duration = 10;

            new BukkitRunnable() {
                int loops = 0;

                @Override
                public void run() {
                    if (loops++ >= (20f / delay * duration) || !Players.entityValid(player) || !player.getScoreboardTags().contains(getName())) {
                        NatureSMP.ON_SHUTDOWN.remove(reset);
                        reset.run();

                        cancel();
                        return;
                    }

                    Location location = player.getLocation().add(0, 0.5, 0);
                    Particles.sphere(location, 5, 0, 20, 0, 0, alt -> {
                        location.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, location, 2, 0.2, 0.2, 0.2, 0.001);
                        return true;
                    });
                    for (LivingEntity entity : player.getLocation().getNearbyLivingEntities(5)) {
                        if (entity.equals(player)) continue;

                        if (entity instanceof Player && trusted.contains(entity))
                            entity.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, 2));
                        else
                            entity.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, 0));
                    }
                }
            }.runTaskTimer(NatureSMP.NATURE, 0, delay);
        }
    }

    @Override
    public void secondary(Player player) {
        if (checkPhase1Restriction(player, 1)) return;
        // Secondary method implementation here if needed
    }
}