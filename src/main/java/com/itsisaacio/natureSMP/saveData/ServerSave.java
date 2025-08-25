package com.itsisaacio.natureSMP.saveData;

import com.google.gson.Gson;
import com.itsisaacio.natureSMP.NatureSMP;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServerSave {
    private static ArrayList<ServerData> serverData = new ArrayList<>();

    public static ServerData createData(Location location) {
        ServerData data = new ServerData(location.toVector());
        serverData.add(data);

        try {
            saveData();
        } catch (IOException error) {
            error.printStackTrace();
        }

        return data;
    }

    public static boolean isSameData(ServerData data, Location location)
    {
        return data.getX() == location.getBlockX() && data.getY() == location.getBlockY() && data.getZ() == location.getBlockZ();
    }

    public static ServerData getData(Location location) {
        for (ServerData data : serverData) {
            if (isSameData(data, location))
                return data;
        }

        return null;
    }

    public static void deleteData(ServerData toDelete) {
        for (ServerData data : serverData) {
            if (data.equals(toDelete)) {
                serverData.remove(data);
                break;
            }
        }

        try {
            saveData();
        } catch (IOException error) {
            error.printStackTrace();
        }
    }

    public static void removeAllData() {
        serverData.clear();
    }

    public static List<ServerData> getAllData() {
        return serverData;
    }

    public static void saveData() throws IOException {
        Gson gson = new Gson();
        File file = new File(NatureSMP.NATURE.getDataFolder().getAbsolutePath() + "/serverData.json");
        file.getParentFile().mkdir();
        file.createNewFile();
        Writer writer = new FileWriter(file);
        gson.toJson(serverData, writer);
        writer.flush();
        writer.close();
    }

    public static void loadData() {
        Gson gson = new Gson();
        File file = new File(NatureSMP.NATURE.getDataFolder().getAbsolutePath() + "/serverData.json");

        if (file.exists()) {
            try {
                Reader reader = new FileReader(file);
                ServerData[] data = gson.fromJson(reader, ServerData[].class);
                serverData = new ArrayList<>(Arrays.asList(data));
            } catch (IOException error) {
                error.printStackTrace();
            }
        }
    }
}
