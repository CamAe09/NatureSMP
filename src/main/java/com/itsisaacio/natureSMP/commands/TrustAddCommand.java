package com.itsisaacio.natureSMP.commands;

import com.itsisaacio.natureSMP.saveData.PlayerData;
import com.itsisaacio.natureSMP.saveData.PlayerSave;
import me.kodysimpson.simpapi.command.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TrustAddCommand extends SubCommand {
    @Override
    public String getName() {
        return "add";
    }

    @Override
    public List<String> getAliases() {
        return List.of();
    }

    @Override
    public String getDescription() {
        return "Trust a player.";
    }

    @Override
    public String getSyntax() {
        return "/trust add <player>";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (sender instanceof Player player)
        {
            Player applied = args.length > 1 ? Bukkit.getServer().getPlayer(args[1]) : player;
            if (applied == null)
            {
                player.sendMessage("§cPlayer isn't online!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f);
                return;
            }

            if (applied.equals(player))
                return;

            PlayerData data = PlayerSave.getData(player);

            if (data == null)
            {
                ArrayList<String> trusted = new ArrayList<>(){{
                    add(applied.getUniqueId().toString());
                }};
                PlayerSave.createData(player, trusted);
                player.sendMessage(applied.getName() + "§a has been trusted!");
            }
            else
            {
                ArrayList<String> trusted = data.getTrusted();

                if (trusted != null && !trusted.contains(applied.getUniqueId().toString())) {
                    trusted.add(applied.getUniqueId().toString());
                    data.setTrusted(trusted);
                    PlayerSave.updateData(player, data);

                    player.sendMessage(applied.getName() + "§a has been trusted!");
                }
                else if (trusted != null)
                {
                    player.sendMessage("§aPlayer is already trusted!");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f);
                }
            }
        }
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        if (args.length > 1)
        {
            List<String> list = CommandHandler.getAutofill(args, 1, null);
            PlayerData data = PlayerSave.getData(player);

            if (data != null) {
                for (String s : data.getTrusted()) {
                    Player trusted = Bukkit.getPlayer(UUID.fromString(s));

                    if (trusted != null)
                        list.remove(trusted.getName());
                }
            }

            return list;
        }

        return null;
    }
}
