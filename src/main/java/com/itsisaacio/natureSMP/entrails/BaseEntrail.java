package com.itsisaacio.natureSMP.entrails;

import com.itsisaacio.natureSMP.Keys;
import com.itsisaacio.natureSMP.PhaseManager;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.intellij.lang.annotations.Subst;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;

public abstract class BaseEntrail {
    public static class Effect
    {
        public PotionEffectType type;
        public int amplifier;

        public Effect(PotionEffectType type, int amplifier)
        {
            this.type = type;
            this.amplifier = amplifier;
        }
    }

    @Subst("")
    public abstract String getName();

    public abstract String getColor();

    public abstract ArrayList<String> getAbilities();

    public abstract ArrayList<String> getLore();

    public abstract Effect[] getEffects();

    public abstract ArrayList<String> getPassive();

    public abstract void perform(Player player, int type);

    public abstract void secondary(Player player);

    protected boolean isPhase1Active() {
        return PhaseManager.isPhaseActive() && PhaseManager.getCurrentPhase() == 1;
    }

    protected boolean checkPhase1Restriction(Player player, int abilityType) {
        // Block second ability (type 1) during phase 1
        if (PhaseManager.getCurrentPhase() == 1 && PhaseManager.isPhaseActive() && abilityType == 1) {
            player.sendMessage("§cYou Are Not Allowed To Use This During Phase 1");
            return true; // Blocked
        }
        return false; // Not blocked
    }
}