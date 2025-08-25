package com.itsisaacio.natureSMP.entrails;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.intellij.lang.annotations.Subst;

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
}
