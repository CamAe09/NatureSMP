
package com.itsisaacio.natureSMP.commands;

import com.itsisaacio.natureSMP.custom.EnchantedFlintAndSteel;
import com.itsisaacio.natureSMP.custom.EnchantedReinforcedDeepslate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PhaseItemCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (!player.isOp()) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length < 2 || !args[0].equalsIgnoreCase("item")) {
            player.sendMessage("§cUsage: /phase item <item_name>");
            return true;
        }

        String itemName = args[1].toLowerCase();
        ItemStack item = null;

        switch (itemName) {
            case "enchanted_reinforced_deepslate":
            case "deepslate":
                item = EnchantedReinforcedDeepslate.getItem();
                break;
            case "enchanted_flint_and_steel":
            case "flint":
                item = EnchantedFlintAndSteel.getItem();
                break;
            default:
                player.sendMessage("§cUnknown item: " + itemName);
                player.sendMessage("§eAvailable items: enchanted_reinforced_deepslate, enchanted_flint_and_steel");
                return true;
        }

        if (item != null) {
            player.getInventory().addItem(item);
            player.sendMessage("§aGiven you " + item.getItemMeta().getDisplayName());
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("item");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("item")) {
            completions.addAll(Arrays.asList(
                "enchanted_reinforced_deepslate",
                "enchanted_flint_and_steel"
            ));
        }

        return completions;
    }
}
