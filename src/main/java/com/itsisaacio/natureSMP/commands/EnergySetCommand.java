package com.itsisaacio.natureSMP.commands;

import com.itsisaacio.natureSMP.saveData.PlayerData;
import com.itsisaacio.natureSMP.saveData.PlayerSave;
import com.itsisaacio.natureSMP.Keys;
import me.kodysimpson.simpapi.command.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public class EnergySetCommand extends SubCommand {
    @Override
    public String getName() {
        return "set";
    }

    @Override
    public List<String> getAliases() {
        return List.of();
    }

    @Override
    public String getDescription() {
        return "Set the energy of a player";
    }

    @Override
    public String getSyntax() {
        return "/energy set <energy> <player>";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (sender instanceof Player player && player.isOp())
        {
            Player applied = args.length == 2 ? Bukkit.getServer().getPlayer(args[1]) : player;
            if (applied == null)
            {
                player.sendMessage("§cPlayer isn't online!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f);
                return;
            }

            int newEnergy = Integer.parseInt(args[1]);
            applied.getPersistentDataContainer().set(Keys.energyKey, PersistentDataType.INTEGER, newEnergy);
            player.sendMessage(applied.getName() + "'s energy is now " + newEnergy);
        }
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        if (args.length == 2 && player.isOp())
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
