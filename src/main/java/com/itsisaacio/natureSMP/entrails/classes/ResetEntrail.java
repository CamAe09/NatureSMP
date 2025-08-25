package com.itsisaacio.natureSMP.entrails.classes;

import com.itsisaacio.natureSMP.entrails.BaseEntrail;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class ResetEntrail extends BaseEntrail {
    @Override
    public String getName() {
        return "None";
    }

    @Override
    public String getColor() {
        return "§r";
    }

    @Override
    public ArrayList<String> getAbilities() {
        return new ArrayList<>();
    }

    @Override
    public ArrayList<String> getLore() {
        return new ArrayList<>();
    }

    @Override
    public ArrayList<String> getPassive() {
        return new ArrayList<>();
    }

    @Override
    public Effect[] getEffects() {
        return new Effect[]{};
    }

    @Override
    public void perform(Player player, int type) {}
}
