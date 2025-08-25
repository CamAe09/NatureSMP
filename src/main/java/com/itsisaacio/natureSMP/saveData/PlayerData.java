package com.itsisaacio.natureSMP.saveData;

import org.bukkit.entity.Player;
import java.util.ArrayList;

public class PlayerData {
    private String name;
    private String id;

    private ArrayList<String> trusted;

    public PlayerData(Player player, ArrayList<String> trusted) {
        this.name = player.getName();
        this.id = player.getUniqueId().toString();
        this.trusted = trusted;
    }

    public String getName() {
        return name;
    }
    public void setName(String id) {
        this.name = name;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<String> getTrusted() {
        return trusted;
    }
    public void setTrusted(ArrayList<String> trusted) {
        this.trusted = trusted;
    }
}
