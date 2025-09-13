
package com.itsisaacio.natureSMP;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;

public class PhaseManager {
    private static int currentPhase = 0;
    private static boolean phaseActive = false;
    private static boolean phaseRerollCompleted = false;

    public static void startPhase(int phase) {
        if (phaseActive) {
            return; // Prevent starting multiple phases at once
        }

        currentPhase = phase;
        phaseActive = true;
        phaseRerollCompleted = false;

        // Update config to prevent auto-reroll during phase
        NatureSMP.NATURE.getConfig().set("auto_reroll_on_join", false);
        NatureSMP.NATURE.saveConfig();

        // Show title to all online players
        Component title = Component.text("§e§lPhase " + phase);
        Title titleDisplay = Title.title(title, Component.empty(),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500)));

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(titleDisplay);
        }

        // After 5 seconds, reroll entrails for all players (only once)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!phaseRerollCompleted) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        NatureSMP.NATURE.getEntrailSwapper().randomize(player);
                        player.sendMessage("§6Your entrail has been rerolled for Phase " + phase + "!");
                    }
                    phaseRerollCompleted = true;
                }
            }
        }.runTaskLater(NatureSMP.NATURE, 100); // 100 ticks = 5 seconds
    }

    public static int getCurrentPhase() {
        return currentPhase;
    }

    public static boolean isPhaseActive() {
        return phaseActive;
    }

    public static void setPhaseActive(boolean active) {
        phaseActive = active;
        if (!active) {
            // Re-enable auto reroll when phase ends
            NatureSMP.NATURE.getConfig().set("auto_reroll_on_join", true);
            NatureSMP.NATURE.saveConfig();
            phaseRerollCompleted = false;
        }
    }

    public static boolean hasPhaseRerollCompleted() {
        return phaseRerollCompleted;
    }

    public static void endPhase() {
        phaseActive = false;
        phaseRerollCompleted = false;
        // Re-enable auto reroll
        NatureSMP.NATURE.getConfig().set("auto_reroll_on_join", true);
        NatureSMP.NATURE.saveConfig();
    }
}
