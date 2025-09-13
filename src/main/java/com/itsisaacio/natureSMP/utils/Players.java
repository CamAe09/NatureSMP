package com.itsisaacio.natureSMP.utils;

import com.itsisaacio.natureSMP.Keys;
import com.itsisaacio.natureSMP.NatureSMP;
import com.itsisaacio.natureSMP.saveData.PlayerData;
import com.itsisaacio.natureSMP.saveData.PlayerSave;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class Players {
    private static boolean using;

    public static boolean onCooldown(Player player, int type)
    {
        PersistentDataContainer data = player.getPersistentDataContainer();
        return NatureSMP.COOLDOWNS.getCooldown(type, player) > 0f || NatureSMP.COOLDOWNS.USING.contains(player.getName() + "|" + type)
                || data.getOrDefault(Keys.energyKey, PersistentDataType.INTEGER, 3) < type;
    }

    public static void setCooldown(Player player, int type, int time, boolean b)
    {
        int reduction = 1;
        NatureSMP.COOLDOWNS.setCooldown(type, player, time / reduction, using);
    }

    public static String cooldownText(Player player, int type)
    {
        String key = player.getName() + "|" + type;

        if (NatureSMP.COOLDOWNS.USING.contains(key))
            return " is currently in use!";
        else
            return " is on cooldown for §c" + NatureSMP.COOLDOWNS.getCooldown(type, player) + "§f seconds!";
    }

    public static void trueDamage(LivingEntity entity, Entity attacker, double damage)
    {
        if ((entity instanceof Player player && player.getGameMode() == GameMode.CREATIVE) || entity.isDead()) return;
        if (damage <= 0) return;

        double absorption = entity.getAbsorptionAmount();
        double hurt = absorption - damage;
        if (absorption > 0)
            entity.setAbsorptionAmount(Math.clamp(hurt, 0, 999));

        if (hurt < 0)
            hurt = Math.abs(hurt);
        else hurt = 0;

        double health = entity.getHealth() - hurt;
        if (health <= 0)
        {
            entity.setHealth(0.001);
            entity.damage(99, attacker);
        }
        else
        {
            entity.setHealth(health);
            entity.damage(0.01, attacker);
        }
    }

    public static boolean entityValid(@Nullable LivingEntity entity)
    {
        boolean online = !(entity instanceof Player player) || player.isOnline();
        return online && entity != null && entity.isValid() && !entity.isDead();
    }

    public static ArrayList<Player> getTrusted(Player player, boolean includePlayer)
    {
        PlayerData data = PlayerSave.getData(player);
        ArrayList<Player> trustList = new ArrayList<>();

        if (data != null) {
            for (String uuid : data.getTrusted())
            {
                UUID id = UUID.fromString(uuid);
                Player trusted = Bukkit.getPlayer(id);

                if (trusted != null)
                    trustList.add(trusted);
            }
        }
        if (includePlayer && !trustList.contains(player)) trustList.add(player);
        return trustList;
    }

    public static Collection<LivingEntity> getEnemies(Player player, Location location, double range)
    {
        if (location == null)
            location = player.getLocation();

        Collection<LivingEntity> enemies = location.getNearbyLivingEntities(range);
        enemies.remove(player); // js make sure
        enemies.removeAll(getTrusted(player, true));

        return enemies;
    }
}
