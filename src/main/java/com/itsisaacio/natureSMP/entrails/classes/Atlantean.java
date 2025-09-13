package com.itsisaacio.natureSMP.entrails.classes;

import com.itsisaacio.natureSMP.NatureSMP;
import com.itsisaacio.natureSMP.entrails.BaseEntrail;
import com.itsisaacio.natureSMP.events.EntrailEvents;
import com.itsisaacio.natureSMP.utils.Particles;
import com.itsisaacio.natureSMP.utils.Players;
import com.itsisaacio.natureSMP.utils.Utilities;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Atlantean extends BaseEntrail {
    @Override
    public String getName() {
        return "Atlantean";
    }

    @Override
    public String getColor() {
        return Utilities.hex("#0062ff");
    }

    @Override
    public ArrayList<String> getAbilities() {
        return new ArrayList<>() {{
            add("Oceanic Impale");
            add("Splashdown");
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
        return new Effect[] {
                new Effect(PotionEffectType.SPEED, 0),
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
            Players.setCooldown(player, type, 15, true);
            player.sendMessage("You have used " + getColor() + getAbilities().get(type) + "§f!");
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 0.6f, 1.2f);

            AtomicInteger spawns = new AtomicInteger(3);
            AtomicBoolean running = new AtomicBoolean(true);
            ArrayList<ArmorStand> holders = new ArrayList<>();

            for (int i = 0; i < spawns.get(); i++) {
                ArmorStand holder = (ArmorStand) player.getWorld().spawnEntity(player.getLocation().add(0, 0.3, 0.5), EntityType.ARMOR_STAND);
                holder.setGravity(false);
                holder.setInvulnerable(true);
                holder.setCollidable(false);
                holder.setNoPhysics(true);
                holder.setVisible(false);
                //holder.setArms(true);
                holder.setItem(EquipmentSlot.HEAD, ItemStack.of(Material.TRIDENT));
                holders.add(holder);
            }
            Runnable attack = () -> {
                if (!holders.isEmpty() && spawns.get() > 0)
                {
                    spawns.getAndDecrement();
                    ArmorStand holder = holders.getFirst();
                    holders.remove(holder);

                    Trident trident = player.launchProjectile(Trident.class);
                    //trident.teleport(holder.getLocation());

                    trident.setVelocity(player.getEyeLocation().getDirection().multiply(3));
                    trident.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
                    trident.setDamage(trident.getDamage() * 2);
                    trident.setLifetimeTicks(160);
                    player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_1, 1, 1);
                    //trident.setRotation(player.getEyeLocation().getYaw(), player.getEyeLocation().getPitch());

                    holder.remove();
                }
            };
            EntrailEvents.TRIDENTS.put(player, attack);

            Runnable reset = () -> {
                Players.setCooldown(player, type, 75, false);

                EntrailEvents.TRIDENTS.remove(player);
                for (ArmorStand holder : holders)
                    if (holder.isValid()) holder.remove();
                holders.clear();
            };
            NatureSMP.ON_SHUTDOWN.add(reset);

            new BukkitRunnable()
            {
                int loops = 0;

                @Override
                public void run()
                {
                    if (holders.isEmpty() || spawns.get() <= 0 || !running.get() || !player.getScoreboardTags().contains(getName()))
                    {
                        running.set(false);
                        reset.run();
                        NatureSMP.ON_SHUTDOWN.remove(reset);

                        cancel();
                        return;
                    }

                    Location center = player.getEyeLocation();
                    Location circle = center.clone();

                    Particles.sphere(circle, 1.2f, 0, spawns.get(), 0, 0, alt -> {
                        if (alt > holders.size() - 1) return true;

                        ArmorStand holder = holders.get(alt);
                        holder.setVelocity(new Vector(0, 0, 0));
                        holder.teleport(circle);
                        holder.setRotation(player.getLocation().getYaw(), 90);

                        return true;
                    });

                    if (loops++ % 2 == 0)
                    {
                        Particles.sphere(circle, 4, 0, 16, 0, 0, alt -> {
                            circle.getWorld().spawnParticle(Particle.FALLING_WATER, circle, 1);

                            return true;
                        });
                    }
                }
            }.runTaskTimer(NatureSMP.NATURE, 0, 1);

            NatureSMP.NATURE.delay(() -> {
                running.set(false);
                reset.run();
                NatureSMP.ON_SHUTDOWN.remove(reset);
            }, 300);
        } else if (type == 1) {
            Players.setCooldown(player, type, 120, false);
            player.sendMessage("You have used " + getColor() + getAbilities().get(type) + "§f!");

            World world = player.getWorld();
            Location location = player.getLocation();
            Location particleLoc = player.getLocation();
            world.playSound(location, Sound.ENTITY_PLAYER_SPLASH, 1.0f, 1.0f);
            world.playSound(location, Sound.BLOCK_WATER_AMBIENT, 1.0f, 1.0f);

            for (Player other : Players.getTrusted(player, false)) {
                if (other.getScoreboardTags().contains("Atlantean"))
                    Players.setCooldown(other, type, 2, false);
            }

            particleLoc.add(0, 5, 0);
            for (int i = 1; i <= 5; i++)
            {
                Particles.sphere(particleLoc, i, 0, i * 16, 0, 0, alt -> {
                    world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(25, 140, 255), 1));
                    world.spawnParticle(Particle.FALLING_WATER, particleLoc, 6, 0.2, 0.2, 0.2);

                    return true;
                });
            }
            NatureSMP.NATURE.delay(() -> {
                world.playSound(location, Sound.ITEM_BUCKET_EMPTY, 1.0f, 1.0f);
                world.playSound(location, Sound.ITEM_MACE_SMASH_AIR, 1.0f, 1.0f);

                for (LivingEntity entity : location.getNearbyLivingEntities(10)) {
                    Collection<Player> trusted = Players.getTrusted(player, true);

                    if (entity instanceof Player && trusted.contains(entity))
                        entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 2));
                    else
                        Players.trueDamage(entity, player, 6);
                }

                Location splashLoc = location.clone();
                Particles.sphere(splashLoc, 10, 0, 16, 0, 0, alt -> {
                    world.spawnParticle(Particle.DUST, splashLoc, 1, 1, 0, 1,
                            new Particle.DustOptions(Color.fromRGB(25, 140, 255), 1));
                    world.spawnParticle(Particle.FALLING_WATER, splashLoc, 1, 1, 0, 1);

                    return true;
                });
            }, 15);
        }
    }

    @Override
    public void secondary(Player player) {
        if (checkPhase1Restriction(player, 2)) return;
    }
}