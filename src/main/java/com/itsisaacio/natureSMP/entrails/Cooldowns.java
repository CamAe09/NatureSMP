package com.itsisaacio.natureSMP.entrails;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class Cooldowns {
    public HashMap<String, Long> WAITING = new HashMap<>();
    public ArrayList<String> USING = new ArrayList<>();

    public int getCooldown(int type, Player player) {
        String name = player.getName();
        String key = name + "|" + type;

        if (WAITING.containsKey(key))
            return Math.clamp(Math.round((float) (WAITING.get(key) - System.currentTimeMillis() / 1000)), 0, 999999999);

        return 0;
    }

    public void setCooldown(int type, Player player, int time, boolean using) {
        String name = player.getName();
        String key = name + "|" + type;

        if (WAITING.containsKey(key))
            WAITING.replace(key, System.currentTimeMillis() / 1000 + time);
        else
            WAITING.put(key, System.currentTimeMillis() / 1000 + time);

        if (using && !USING.contains(key))
            USING.add(key);
        else if (!using)
            USING.remove(key);
    }
}
