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
            add("Heat");
            add("Healthful Flare");
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
        };

        if (type == 0) {
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
        } else if (type == 1) {
            Players.setCooldown(player, type, 16, true);
            player.sendMessage("You have used " + getColor() + getAbilities().get(type) + "§f!");
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_DEATH, 1, 1);

            int delay = 5;
            int duration = 15;
            int radius = 12;

            Blaze blaze = (Blaze) player.getWorld().spawnEntity(player.getLocation(), EntityType.BLAZE);
            blaze.setMetadata("Owner", new FixedMetadataValue(NatureSMP.NATURE, player));
            blaze.customName(Component.text(player.getName() + "'s Pillar Base"));
            blaze.setAI(false);

            Runnable reset = () -> {
                Players.setCooldown(player, type, 90, false);

                if (blaze.isValid())
                    blaze.remove();
            };
            NatureSMP.ON_SHUTDOWN.add(reset);

            new BukkitRunnable() {
                int loops = 0;

                @Override
                public void run()
                {
                    if (loops++ >= (20f / delay * duration) || !Players.entityValid(blaze) || !player.getScoreboardTags().contains(getName()))
                    {
                        NatureSMP.ON_SHUTDOWN.remove(reset);
                        reset.run();

                        cancel();
                        return;
                    }

                    Location location = blaze.getLocation();
                    Location height = blaze.getLocation();

                    for (int i = 0; i <= 3; i++) {
                        Particles.sphere(height, 1.5f, 0, 16, 0, 0, alt -> {
                            player.getWorld().spawnParticle(Particle.FLAME, height, 1, 0, 0, 0, 0.001);
                            height.add(0, 0.2, 0);

                            return true;
                        });
                    }
                    Particles.sphere(location, radius, 3, 50, 1, 0, alt -> {
                        player.getWorld().spawnParticle(Particle.DUST, location, 1, new Particle.DustOptions(Color.ORANGE.setGreen(70), 1f));

                        return true;
                    });

                    Collection<Player> trusted = Players.getTrusted(player, true);

                    for (Player other : location.getNearbyPlayers(radius)) {
                        if (trusted.contains(other))
                            other.heal(0.6, EntityRegainHealthEvent.RegainReason.CUSTOM);
                        else
                            Players.trueDamage(other, player, 0.3);
                    }
                }
            }.runTaskTimer(NatureSMP.NATURE, 0, delay);
        }
    }
}
