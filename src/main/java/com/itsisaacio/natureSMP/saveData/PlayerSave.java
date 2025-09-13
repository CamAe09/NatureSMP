package com.itsisaacio.natureSMP.saveData;

import com.google.gson.Gson;
import com.itsisaacio.natureSMP.NatureSMP;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.*;

public class PlayerSave {
    private static ArrayList<PlayerData> playerData = new ArrayList<>();

    public static PlayerData createData(Player player, ArrayList<String> trusted) {
        PlayerData data = new PlayerData(player, trusted);
        playerData.add(data);

        try {
            saveData();
        } catch (IOException error) {
            error.printStackTrace();
        }

        return data;
    }

    public static PlayerData getData(Player player) {
        for (PlayerData data : playerData) {
            if (data.getId().equalsIgnoreCase(player.getUniqueId().toString())) {
                return data;
            }
        }

        return null;
    }

    public static void deleteData(PlayerData toDelete) {
        for (PlayerData data : playerData) {
            if (data.equals(toDelete)) {
                playerData.remove(data);
                break;
            }
        }

        try {
            saveData();
        } catch (IOException error) {
            error.printStackTrace();
        }
    }

    public static PlayerData updateData(Player player, PlayerData newData) {
        for (PlayerData data : playerData) {
            if (data.getId().equalsIgnoreCase(player.getUniqueId().toString())) {
                data.setTrusted(newData.getTrusted());

                try {
                    saveData();
                } catch (IOException error) {
                    error.printStackTrace();
                }

                return data;
            }
        }

        return null;
    }

    public static List<PlayerData> getAllData() {
        return playerData;
    }

    public static void saveData() throws IOException {
        Gson gson = new Gson();
        File file = new File(NatureSMP.NATURE.getDataFolder().getAbsolutePath() + "/playerData.json");
        file.getParentFile().mkdir();
        file.createNewFile();
        Writer writer = new FileWriter(file);
        gson.toJson(playerData, writer);
        writer.flush();
        writer.close();
    }

    public static void loadData() {
        Gson gson = new Gson();
        File file = new File(NatureSMP.NATURE.getDataFolder().getAbsolutePath() + "/playerData.json");

        if (file.exists()) {
            try {
                Reader reader = new FileReader(file);
                PlayerData[] data = gson.fromJson(reader, PlayerData[].class);
                playerData = new ArrayList<>(Arrays.asList(data));
            } catch (IOException error) {
                error.printStackTrace();
            }
        }
    }

    public static Location getPlayerLocation(Player player) {
        return null;
    }

    public static void setPlayerLocation(Player player, Location currentLocation) {

    }
}
