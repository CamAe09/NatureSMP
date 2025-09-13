package com.itsisaacio.natureSMP.commands;

import com.itsisaacio.natureSMP.NatureSMP;
import com.itsisaacio.natureSMP.PhaseManager;
import com.itsisaacio.natureSMP.custom.EnergizingPedestal;
import com.itsisaacio.natureSMP.custom.Energizer;
import com.itsisaacio.natureSMP.custom.EnergyCore;
import com.itsisaacio.natureSMP.custom.Swapper;
import com.itsisaacio.natureSMP.entrails.classes.ResetEntrail;
import com.itsisaacio.natureSMP.entrails.EntrailList;
import com.itsisaacio.natureSMP.entrails.BaseEntrail;
import com.itsisaacio.natureSMP.ui.EntrailSwapper;
import com.itsisaacio.natureSMP.Keys;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommandHandler implements TabCompleter, CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (sender instanceof Player player) {
            String cmdName = command.getName().toLowerCase();
            PersistentDataContainer data = player.getPersistentDataContainer();
            int energy = data.getOrDefault(Keys.energyKey, PersistentDataType.INTEGER, 3);

            if (cmdName.equals("rprimary") && energy > 0)
                NatureSMP.getEntrail(player).perform(player, 0);
            else if (cmdName.equals("rsecondary") && energy > 1)
                NatureSMP.getEntrail(player).perform(player, 1);
            else if (cmdName.equals("entrail") && player.isOp()) {
                Player applied = args.length == 2 ? Bukkit.getServer().getPlayer(args[1]) : player;
                if (applied == null) return true;

                for (BaseEntrail mob : EntrailList.entrails) {
                    if (args[0].equalsIgnoreCase(mob.getName().replace(" ", "")))
                        NatureSMP.setEntrail(applied, mob);
                }
                if (args[0].equalsIgnoreCase("none") || args[0].equalsIgnoreCase("reset"))
                    NatureSMP.setEntrail(applied, new ResetEntrail());
            } else if (cmdName.equals("energy")) {
                player.sendMessage(
                        "You have §b" +
                                player.getPersistentDataContainer().getOrDefault(Keys.energyKey, PersistentDataType.INTEGER, 3)
                                + " energy!");
            } else if (cmdName.equals("stat") && player.isOp()) {
                if (args.length >= 2)
                {
                    Player applied = args.length == 3 ? Bukkit.getServer().getPlayer(args[2]) : player;
                    Statistic stat = Statistic.valueOf(args[0]);

                    if (applied != null)
                        applied.setStatistic(stat, Integer.parseInt(args[1]));
                }
            } else if (cmdName.equals("reroll") && player.isOp())
            {
                Player applied = args.length == 1 ? Bukkit.getServer().getPlayer(args[0]) : player;
                if (applied == null) return true;
            } else if (cmdName.equals("phase")) {
                if (args.length == 1) {
                    try {
                        int phase = Integer.parseInt(args[0]);
                        PhaseManager.startPhase(phase);
                        player.sendMessage("§aStarted Phase " + phase + "!");
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cInvalid phase number!");
                    }
                } else if (args[0].equalsIgnoreCase("start") && args.length == 2) {
                    try {
                        int phase = Integer.parseInt(args[1]);
                        if (phase < 1) {
                            player.sendMessage("§cPhase must be 1 or higher!");
                            return true;
                        }

                        PhaseManager.startPhase(phase);
                        player.sendMessage("§aStarted Phase " + phase + "!");
                        return true;
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cInvalid phase number!");
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("item")) {
                    // Handle phase item command
                    PhaseItemCommand itemCommand = new PhaseItemCommand();
                    return itemCommand.onCommand(sender, command, label, args);
                }

                //EntrailSwapper.randomize(applied); // This line seems out of place here, consider its context.
            } else if (cmdName.equals("cooldown_reload") && player.isOp()) {
                NatureSMP.COOLDOWNS.WAITING.clear();
                NatureSMP.COOLDOWNS.USING.clear();
            } else if (cmdName.equals("give_item") && player.isOp()) {
                if (args.length >= 1) {
                    switch (args[0].toLowerCase()) {
                        case "swapper":
                            player.getInventory().addItem(Swapper.getItem());
                            break;
                        case "energizer":
                            player.getInventory().addItem(Energizer.getItem());
                            break;
                        case "energycore":
                            player.getInventory().addItem(EnergyCore.getItem());
                            break;
                        case "energizingpedestal":
                            player.getInventory().addItem(EnergizingPedestal.getItem());
                            break;
                    }
                }
            } else if (cmdName.equals("withdraw")) {
                if (energy > 0)
                {
                    data.set(Keys.energyKey, PersistentDataType.INTEGER, energy - 1);
                    player.give(EnergyCore.getItem());

                    player.sendMessage("You now have §a" + (energy - 1) + "§b energy§f!");
                }
                else player.sendMessage("You need energy to do this!");
            }
        }

        return true;
    }

    public static List<String> getAutofill(String[] args, int index, Collection<String> strings)
    {
        if (strings == null)
        {
            strings = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                strings.add(player.getName());
            }
        }

        return new ArrayList<>(strings.stream().filter(
                param -> param.toLowerCase().startsWith(args[index].toLowerCase())).toList()
        );
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args){
        List<String> suggestions = new ArrayList<>();
        int playerListIndex = -1;

        if (sender instanceof Player player && player.isOp())
        {
            if(command.getName().equalsIgnoreCase("entrail")) {
                if (args.length == 1) {
                    for (BaseEntrail mob : EntrailList.entrails)
                        suggestions.add(mob.getName().replace(" ", ""));
                    suggestions = getAutofill(args, 0, suggestions);
                } else if (args.length == 2)
                    playerListIndex = 1;
            }
            else if (command.getName().equalsIgnoreCase("reroll")) {
                if (args.length == 1)
                    playerListIndex = 0;
            }
            else if (command.getName().equalsIgnoreCase("stat")) {
                if (args.length == 1) {
                    for (Statistic value : Statistic.values())
                        suggestions.add(value.name());
                    suggestions = getAutofill(args, 0, suggestions);
                } else if (args.length == 2)
                    suggestions.add("<value>");
                else if (args.length == 3)
                    playerListIndex = 2;
            }
            else if (command.getName().equalsIgnoreCase("give_item"))
            {
                if (args.length == 1) {
                    suggestions.add("Swapper");
                    suggestions.add("EnergyCore");
                    suggestions.add("Energizer");
                    suggestions.add("EnergizingPedestal");
                    suggestions = getAutofill(args, 0, suggestions);
                }
            }
            else if (command.getName().equalsIgnoreCase("phase")) {
                if (args.length == 1) {
                    if ("start".startsWith(args[0].toLowerCase())) {
                        suggestions.add("start");
                    }
                    if ("item".startsWith(args[0].toLowerCase())) {
                        suggestions.add("item");
                    }
                } else if (args.length == 2 && args[0].equalsIgnoreCase("start")) {
                    suggestions.add("1");
                    suggestions.add("2");
                    suggestions.add("3");
                } else if (args[0].equalsIgnoreCase("item")) {
                    // Handle phase item tab completion
                    PhaseItemCommand itemCommand = new PhaseItemCommand();
                    return itemCommand.onTabComplete(sender, command, alias, args);
                }
            }
        }

        if (playerListIndex >= 0)
            suggestions = getAutofill(args, playerListIndex, null);

        return suggestions;
    }
}