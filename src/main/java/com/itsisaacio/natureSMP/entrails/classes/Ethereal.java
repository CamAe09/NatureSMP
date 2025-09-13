package com.itsisaacio.natureSMP.entrails.classes;

import com.itsisaacio.natureSMP.NatureSMP;
import com.itsisaacio.natureSMP.entrails.BaseEntrail;
import com.itsisaacio.natureSMP.events.BlockEvents;
import com.itsisaacio.natureSMP.events.EntrailEvents;
import com.itsisaacio.natureSMP.utils.BlockUtils;
import com.itsisaacio.natureSMP.utils.Players;
import com.itsisaacio.natureSMP.utils.Utilities;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class Ethereal extends BaseEntrail {
    @Override
    public String getName() {
        return "Ethereal";
    }

    @Override
    public String getColor() {
        return Utilities.hex("#bdbcd4");
    }

    @Override
    public ArrayList<String> getAbilities() {
        return new ArrayList<>() {{
            add("Groundbreak");
            add("Clouded");
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
                new Effect(PotionEffectType.HEALTH_BOOST, 0),
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
            Players.setCooldown(player, type, 999999999, true);
            player.sendMessage("You have used " + getColor() + getAbilities().get(type) + "§f!");
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 0.6f, 1.2f);

            int delay = 2;
            int duration = 8;

            EntrailEvents.GROUND_BREAK.add(player);

            new BukkitRunnable() {
                int loops = 0;
                LivingEntity target = null;

                final Runnable reset = () -> {
                    EntrailEvents.GROUND_BREAKING.remove(player);
                    Players.setCooldown(player, type, 1, true);
                    Players.setCooldown(player, type, 120, false);
                };
                boolean added = false;

                @Override
                public void run()
                {
                    if (!added) {
                        added = true;
                        NatureSMP.ON_SHUTDOWN.add(reset);
                    }

                    if (!Players.entityValid(player) || !player.getScoreboardTags().contains(getName()))
                    {
                        NatureSMP.ON_SHUTDOWN.remove(reset);
                        reset.run();

                        cancel();
                        return;
                    }

                    if (EntrailEvents.GROUND_BREAKING.containsKey(player) | target != null)
                    {
                        if (target == null)
                        {
                            target = EntrailEvents.GROUND_BREAKING.get(player);
                            target.sendMessage("You have been affected with " + getColor() + getAbilities().get(type) + "§fby " + player.getName() + "§f!");
                            target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 8, 80));
                        }

                        if (loops >= 16) {
                            target.setVelocity(new Vector(0, -4, 0));
                        }

                        if (loops++ >= (20f / delay * duration) || !target.isValid() || target.isDead() || (loops > 4 && target.isOnGround())) {
                            target.getWorld().spawnParticle(Particle.DUST_PLUME, target.getLocation(), 50, 2, 1, 2, 0.01);
                            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

                            NatureSMP.ON_SHUTDOWN.remove(reset);
                            reset.run();
                            cancel();
                        }
                    }
                }
            }.runTaskTimer(NatureSMP.NATURE, 0, delay);
        } else if (type == 1) {
            Players.setCooldown(player, type, 80, false);
            player.sendMessage("You have used " + getColor() + getAbilities().get(type) + "§f!");
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.0f);
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 60, 2));

            int delay = 1;
            int duration = 5;

            new BukkitRunnable()
            {
                int loops = 0;
                Location lastGlass = null;

                final ArrayList<Block> toReset = new ArrayList<>();
                final Runnable reset = () -> {
                    for (Block block : toReset)
                        BlockEvents.setOriginal(block.getLocation());
                };
                boolean apply = false;

                @Override
                public void run()
                {
                    if (!apply)
                    {
                        apply = true;
                        NatureSMP.ON_SHUTDOWN.add(reset);
                    }

                    if (lastGlass != null)
                        BlockEvents.setOriginal(lastGlass);

                    Block below = player.getLocation().subtract(0, 1, 0).getBlock();
                    BlockEvents.REPLACED.putIfAbsent(below.getLocation(), below.getBlockData());
                    below.setType(Material.GLASS);
                    lastGlass = below.getLocation();
                    toReset.clear();
                    toReset.add(below);

                    if (loops++ >= (20f / delay * duration))
                    {
                        ArrayList<Block> cloud = BlockUtils.getNearbyBlocks(lastGlass, 3, 0, 3);
                        for (Block block : cloud) {
                            BlockEvents.REPLACED.putIfAbsent(block.getLocation(), block.getBlockData());
                            block.setType(Material.WHITE_WOOL);
                            toReset.add(block);
                        }

                        NatureSMP.NATURE.delay(() -> {
                            NatureSMP.ON_SHUTDOWN.remove(reset);
                            reset.run();
                        }, 2400);

                        cancel();
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