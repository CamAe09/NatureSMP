package com.itsisaacio.natureSMP.events;

import com.itsisaacio.natureSMP.NatureSMP;
import com.itsisaacio.natureSMP.PhaseManager;
import com.itsisaacio.natureSMP.custom.EnergyCore;
import com.itsisaacio.natureSMP.entrails.BaseEntrail;
import com.itsisaacio.natureSMP.ui.EntrailSwapper;
import com.itsisaacio.natureSMP.Keys;
import com.itsisaacio.natureSMP.utils.Players;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class MainEvents implements Listener {
    public static boolean doesSelection = true;
    public static ArrayList<Player> INVISIBLE = new ArrayList<>();
    public static HashMap<Player, Boolean> GROUNDED = new HashMap<>();

    @EventHandler
    public void onPlayerFall(PlayerMoveEvent event)
    {
        Player player = event.getPlayer();
        boolean grounded = ((LivingEntity) player).isOnGround();
        GROUNDED.putIfAbsent(player, grounded);

        if (GROUNDED.get(player) != grounded)
        {
            GROUNDED.replace(player, grounded);

            //if (grounded && Players.entityValid(player))
                //IronLung.passive(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        BaseEntrail entrail = NatureSMP.getEntrail(player);
        PersistentDataContainer data = player.getPersistentDataContainer();

        if (entrail.getName().equals("None") && doesSelection)
            NatureSMP.NATURE.delay(() -> EntrailSwapper.randomize(player), 60);
        if (!data.has(Keys.energyKey))
            data.set(Keys.energyKey, PersistentDataType.INTEGER, 3);

        NatureSMP.updateItems(player);

        // Only auto-assign entrail if phase 1 has been started at least once
        // This prevents players from getting entrails before the game officially begins
        if (PhaseManager.getCurrentPhase() >= 1 || PhaseManager.hasPhaseRerollCompleted()) {
            // Auto-assign entrail if player doesn't have one
            if (NatureSMP.getEntrail(player).getName().equals("None")) {
                NatureSMP.NATURE.getEntrailSwapper().randomize(player);
            } else if (NatureSMP.NATURE.getConfig().getBoolean("auto_reroll_on_join", true) && !PhaseManager.isPhaseActive()) {
                // Only auto-reroll if not in an active phase and config allows it
                NatureSMP.NATURE.getEntrailSwapper().randomize(player);
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();

        for (Player invisible : INVISIBLE)
            player.showPlayer(NatureSMP.NATURE, invisible);

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (INVISIBLE.contains(player))
                other.showPlayer(NatureSMP.NATURE, other);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        Player player = event.getPlayer();
        Player killer = player.getKiller();

        if (killer != null)
        {
            PersistentDataContainer playerData = player.getPersistentDataContainer();
            PersistentDataContainer killerData = killer.getPersistentDataContainer();

            int gainedEnergy = killerData.getOrDefault(Keys.energyKey, PersistentDataType.INTEGER, 3);
            int lostEnergy = playerData.getOrDefault(Keys.energyKey, PersistentDataType.INTEGER, 3);
            playerData.set(Keys.energyKey, PersistentDataType.INTEGER, Math.clamp(lostEnergy - 1, 0, 5));

            if (gainedEnergy >= 5 || gainedEnergy <= 0)
                event.getDrops().add(EnergyCore.getItem());
            else
                killerData.set(Keys.energyKey, PersistentDataType.INTEGER, Math.clamp(gainedEnergy + 1, 0, 5));

            if (lostEnergy <= 0)
            {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.sendMessage(player.getName() + Color.RED + " has lost all of their energy!");
                    online.playSound(online.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1, 1);
                }
            }
        }
    }

    @EventHandler
    public void serverListMOTD(ServerListPingEvent event) {
        String name = NatureSMP.NATURE.getConfig().getString("server_name");
        if (name == null) return;
        event.motd(Component.text(name));
    }
}