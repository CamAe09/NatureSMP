package com.itsisaacio.natureSMP.ui;

import com.itsisaacio.natureSMP.entrails.EntrailList;
import com.itsisaacio.natureSMP.NatureSMP;
import com.itsisaacio.natureSMP.entrails.BaseEntrail;
import com.itsisaacio.natureSMP.entrails.classes.ResetEntrail;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Random;

public class EntrailSwapper {
    public static ArrayList<Player> RANDOMIZING = new ArrayList<>();

    public static void randomize(Player player) {
        if (RANDOMIZING.contains(player)) return;
        RANDOMIZING.add(player);

        final int loops = 99;

        NatureSMP.setEntrail(player, new ResetEntrail());

        new BukkitRunnable() {
            int loop = 0;
            BaseEntrail lastEntrail = null;

            @Override
            public void run() {
                BaseEntrail entrail = lastEntrail;

                while (entrail == lastEntrail || entrail instanceof ResetEntrail)
                    entrail = EntrailList.entrails.get(new Random().nextInt(EntrailList.entrails.size()));

                lastEntrail = entrail;

                ComponentBuilder<TextComponent, TextComponent.Builder> message = Component.text();
                TextComponent finalMessage = null;
                String name = entrail.getName();
                name = name.toLowerCase();

                int[] order = new int[]{1, 2}; // stupid shit
                for (int i : order) {
                    TextComponent abilityText = (TextComponent) Component.text(i).font(Key.key("naturesmp:" + name));
                    message.append(abilityText);

                    if (i < order.length)
                        message.append(Component.text("    "));

                    finalMessage = message.build();
                }
                player.sendActionBar(finalMessage);

                if (loop++ > loops) {
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
                    player.playSound(player, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 1, 1);
                    player.playSound(player, Sound.BLOCK_VAULT_EJECT_ITEM, 1, 1);

                    NatureSMP.setEntrail(player, entrail);

                    RANDOMIZING.remove(player);
                    cancel();
                } else
                    player.playSound(player, Sound.BLOCK_DECORATED_POT_INSERT, 1, 0.5f + ((float) loop / loops) * 1.5f);
            }
        }.runTaskTimer(NatureSMP.NATURE, 0, 1);
    }
}
