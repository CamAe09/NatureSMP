package com.itsisaacio.natureSMP;

import com.itsisaacio.natureSMP.commands.TrustAddCommand;
import com.itsisaacio.natureSMP.commands.TrustRemoveCommand;
import com.itsisaacio.natureSMP.custom.Energizer;
import com.itsisaacio.natureSMP.entrails.classes.Blazeborne;
import com.itsisaacio.natureSMP.entrails.classes.Frosted;
import com.itsisaacio.natureSMP.entrails.classes.ResetEntrail;
import com.itsisaacio.natureSMP.entrails.classes.Shaded;
import com.itsisaacio.natureSMP.events.*;
import com.itsisaacio.natureSMP.entrails.BaseEntrail;
import com.itsisaacio.natureSMP.entrails.Cooldowns;
import com.itsisaacio.natureSMP.entrails.EntrailList;
import com.itsisaacio.natureSMP.utils.*;
import com.itsisaacio.natureSMP.saveData.PlayerSave;
import com.itsisaacio.natureSMP.commands.CommandHandler;
import com.itsisaacio.natureSMP.saveData.ServerData;
import com.itsisaacio.natureSMP.saveData.ServerSave;
import me.kodysimpson.simpapi.command.CommandManager;
import me.kodysimpson.simpapi.command.SubCommand;
import me.kodysimpson.simpapi.menu.MenuManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

import static org.bukkit.potion.PotionEffect.INFINITE_DURATION;

public final class NatureSMP extends JavaPlugin {
    public static NatureSMP NATURE;
    public static Cooldowns COOLDOWNS;

    public static ArrayList<Runnable> ON_SHUTDOWN = new ArrayList<>();

    @Override
    public void onDisable() {
        for (Runnable reset : ON_SHUTDOWN)
            reset.run();

        BlockEvents.REPLACED.forEach((location, blockData) -> BlockEvents.setOriginal(location));

        ServerSave.removeAllData();

        BlockEvents.rechargers.forEach((recharger, active) -> {
            ServerSave.createData(recharger);
        });
    }

    public void setupCommands(String[] commands)
    {
        for (String command : commands) {
            Objects.requireNonNull(getCommand(command)).setExecutor(new CommandHandler());
            Objects.requireNonNull(getCommand(command)).setTabCompleter(new CommandHandler());
        }
    }

    @SafeVarargs
    public final void makeCommand(String name, String desc, String usage, Class<? extends SubCommand>... commands)
    {
        try {
            CommandManager.createCoreCommand(this, name, desc, usage, null, commands);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Logger log = Logger.getLogger("Minecraft");
    final String font = "naturesmp:";
    final TextComponent lockedAbility = (TextComponent) Component.text("9").font(Key.key(font + "energy"));

    @Override
    public void onEnable() {
        NATURE = this;
        COOLDOWNS = new Cooldowns();

        this.saveDefaultConfig();

        setupCommands(new String[]{
                "rprimary",
                "rsecondary",

                "entrail",
                "withdraw",
                "energy",
                "reroll",
                "stat",
                "give_item",
                "cooldown_reload",
        });

        makeCommand("trust", "Used to handle trusted players.", "/trust", TrustAddCommand.class, TrustRemoveCommand.class);
        //makeCommand("energy", "Used to handle player's energy.", "/energy", EnergySetCommand.class);

        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new MainEvents(), this);
        manager.registerEvents(new BlockEvents(), this);
        manager.registerEvents(new EntrailEvents(), this);
        MenuManager.setup(getServer(), this);

        //manager.registerEvents(new RecipeManager(), this);
        //RecipeManager.loadRecipes();

        PlayerSave.loadData();
        ServerSave.loadData();

        for (Player player : Bukkit.getServer().getOnlinePlayers())
            updateItems(player);

        NatureSMP.NATURE.delay(() -> {
            World world = Bukkit.getWorld("world");

            if (world != null) {
                for (ServerData recharger : ServerSave.getAllData()) {
                    BlockEvents.rechargers.putIfAbsent(new Location(world, recharger.getX(), recharger.getY(), recharger.getZ()), false);

                    /*world.loadChunk(recharger.getX(), recharger.getZ());
                    world.setChunkForceLoaded(recharger.getX(), recharger.getZ(), true);

                    for (BlockDisplay display : world.getNearbyEntitiesByType(BlockDisplay.class, new Location(world, recharger.getX(), recharger.getY(), recharger.getZ()), 4)) {
                        if (display.getPersistentDataContainer().has(Keys.rechargeStationKey))
                        {
                            BlockEvents.rechargers.putIfAbsent(display, false);
                        }
                    }*/
                }
            }
        }, 20);

        new BukkitRunnable() {
            @Override
            public void run() {
                BlockEvents.rechargers.forEach((location, active) -> {
                    if (!active)
                    {
                        BlockEvents.checkRecharger(location);

                        Location particle = location.getBlock().getLocation().add(0.5, 0.3, 0.5);
                        Particles.sphere(particle, 2.5f, 0, 20, 0, 0, alt -> {
                            particle.getWorld().spawnParticle(Particle.TRIAL_SPAWNER_DETECTION, particle, 1, 0, 0, 0, 0);
                            return true;
                        });
                    }
                });

                for (Player player : Bukkit.getServer().getOnlinePlayers())
                {
                    BaseEntrail entrail = getEntrail(player);
                    if (entrail.getName().equals("None"))
                        continue;

                    Set<String> tags = player.getScoreboardTags();
                    addEffects(player, entrail);

                    PlayerInventory inventory = player.getInventory();
                    PersistentDataContainer data = player.getPersistentDataContainer();

                    AttributeInstance speed = player.getAttribute(Attribute.MOVEMENT_SPEED);
                    AttributeInstance damage = player.getAttribute(Attribute.ATTACK_DAMAGE);
                    AttributeInstance sneak = player.getAttribute(Attribute.SNEAKING_SPEED);
                    AttributeInstance health = player.getAttribute(Attribute.MAX_HEALTH);

                    if (tags.contains("Blazeborne"))
                    {
                        boolean hasSpeed = speed != null && speed.getModifiers().contains(Blazeborne.speedMod);
                        boolean hasDamage = damage != null && damage.getModifiers().contains(Blazeborne.damageMod);

                        if (player.getFireTicks() > 0 && speed != null && !hasSpeed)
                            speed.addModifier(Blazeborne.speedMod);
                        else if (player.getFireTicks() <= 0 && hasSpeed)
                            speed.removeModifier(Blazeborne.speedMod);

                        if (damage != null && !hasDamage && player.hasPotionEffect(PotionEffectType.STRENGTH) &&
                                player.getWorld().getEnvironment() == World.Environment.NETHER)
                            damage.addModifier(Blazeborne.damageMod);
                        else if (hasDamage)
                            damage.removeModifier(Blazeborne.damageMod);

                        if (player.isInRain())
                            player.damage(1);
                    } else if (tags.contains("Atlantean")) {
                        if (Utilities.warmBiomes.contains(player.getLocation().getBlock().getBiome()))
                            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20, 0));

                        if (player.isInWater() && player.getLocation().toVector().getY() <= 20)
                        {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20, 2));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 0));
                        }

                        if (player.hasPotionEffect(PotionEffectType.WATER_BREATHING)
                                && (player.getInventory().getBoots() == null || player.getInventory().getBoots().getType() == Material.AIR))
                            player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 20, 0));
                        if (player.hasPotionEffect(PotionEffectType.WEAVING) && player.getLocation().getBlock().getType() == Material.COBWEB && MathUtils.random.nextInt(1, 5) == 1)
                            player.getLocation().getBlock().setType(Material.AIR);
                    } else if (tags.contains("Frosted")) {
                        boolean hasSpeed = speed != null && speed.getModifiers().contains(Frosted.speedMod);

                        Material below = player.getLocation().subtract(0, 0.6, 0).getBlock().getType();
                        boolean isOnIce = below == Material.ICE || below == Material.BLUE_ICE || below == Material.FROSTED_ICE || below == Material.PACKED_ICE;

                        if (speed != null && !hasSpeed && isOnIce && player.hasPotionEffect(PotionEffectType.SPEED))
                            speed.addModifier(Frosted.speedMod);
                        else if (hasSpeed && (!isOnIce || !player.hasPotionEffect(PotionEffectType.SPEED)))
                            speed.removeModifier(Frosted.speedMod);

                        Location location = player.getLocation();

                        if (player.getWorld().hasStorm() && player.getWorld().getTemperature(location.getBlockX(), location.getBlockY(), location.getBlockZ()) <= 0.15
                            && player.getWorld().getHighestBlockAt(location).getY() < location.getY())
                            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20, 1));
                    } else if (tags.contains("Naturen")) {
                        for (Block block : BlockUtils.getNearbyBlocks(player.getLocation(), 20)) {
                            if (block.getType() == Material.SPORE_BLOSSOM)
                            {
                                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, 1));
                                break;
                            }
                        }
                    } else if (tags.contains("Shaded")) {
                        boolean hasSpeed = speed != null && speed.getModifiers().contains(Shaded.speedMod);
                        boolean hasDark = speed != null && speed.getModifiers().contains(Shaded.darkMod);
                        boolean hasHealth = health != null && health.getModifiers().contains(Shaded.healthMod);
                        boolean invisible = player.hasPotionEffect(PotionEffectType.INVISIBILITY);

                        if (speed != null && !hasSpeed && invisible)
                            speed.addModifier(Shaded.speedMod);
                        else if (hasSpeed && !invisible)
                            speed.removeModifier(Shaded.speedMod);

                        if (sneak != null && !sneak.getModifiers().contains(Shaded.sneakMod))
                            sneak.addModifier(Shaded.sneakMod);

                        if (player.getWorld().getBiome(player.getLocation()) == Biome.DEEP_DARK && speed != null && !hasDark)
                            speed.addModifier(Shaded.darkMod);
                        else if (hasDark && player.getWorld().getBiome(player.getLocation()) != Biome.DEEP_DARK)
                            speed.removeModifier(Shaded.darkMod);

                        if (player.getLocation().getY() <= 0)
                            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 20, 1));

                        boolean found = false;
                        for (Block block : BlockUtils.getNearbyBlocks(player.getLocation(), 10)) {
                            if (block.getType() == Material.AMETHYST_CLUSTER) {
                                found = true;
                                break;
                            }
                        }

                        if (health != null && !hasHealth && found)
                            health.addModifier(Shaded.healthMod);
                        else if (hasHealth && !found)
                            health.removeModifier(Shaded.healthMod);
                    } else if (tags.contains("Ethereal")) {
                        if (!player.getWorld().isDayTime())
                            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20, 0));
                    }

                    if (EntrailEvents.LEECHED.containsValue(player) && player.getFireTicks() > 0)
                    {
                        ArrayList<Player> toRemove = new ArrayList<>();
                        EntrailEvents.LEECHED.forEach((attacker, thisGuy) -> {
                            if (thisGuy.equals(player))
                                toRemove.add(attacker);
                        });
                        for (Player key : toRemove)
                            EntrailEvents.LEECHED.remove(key);
                    }

                    // ACTION BAR

                    ComponentBuilder<TextComponent, TextComponent.Builder> message = Component.text();
                    TextComponent finalMessage = null;
                    String name = entrail.getName();
                    name = name.toLowerCase();

                    int energy = data.getOrDefault(Keys.energyKey, PersistentDataType.INTEGER, 0);

                    int[] order = new int[]{1, 2}; // stupid shit
                    for (int i : order) {
                        int timeLeft = COOLDOWNS.getCooldown(i - 1, player);
                        final String key = String.valueOf((timeLeft > 0 || Players.onCooldown(player, i - 1)) ? i + 2 : i);
                        TextComponent abilityText = (TextComponent) Component.text(key).font(Key.key(font + name));

                        if (energy >= i)
                            message.append(abilityText);
                        else
                            message.append(lockedAbility);

                        if (i < order.length)
                            message.append(Component.text("    "));

                        finalMessage = message.build();
                    }
                    player.sendActionBar(finalMessage);
                }
            }
        }.runTaskTimer(this, 0, 10);

        new BukkitRunnable() {
            final ArrayList<Location> updated = new ArrayList<>();
            int helix = -5;
            int helix2 = 5;

            @Override
            public void run() {
                helix++;
                helix2--;
                if (helix > 6) helix = -5;
                if (helix2 < -6) helix2 = 5;

                updated.clear();

                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    Set<String> tags = player.getScoreboardTags();

                    PlayerInventory inventory = player.getInventory();
                    if (inventory.contains(Material.DRAGON_EGG))
                    {
                        Location particle = player.getLocation().add(0, 0.15, 0);
                        Particles.sphere(particle, 0.5f, 0, 12, 0, 0, alt -> {
                            if (alt != helix + 5) return true;
                            particle.add(0, Math.abs(helix / 20f), 0);
                            particle.getWorld().spawnParticle(Particle.DUST, particle, 1, 0, 0, 0,
                                    new Particle.DustOptions(Color.fromRGB(255, 0, 255), 1.2f));
                            return true;
                        });
                        Location particle2 = player.getLocation().add(0, 0.05, 0);
                        Particles.sphere(particle2, 1f, 0, 12, 0, 0, alt -> {
                            if (alt != helix2 + 6) return true;
                            particle2.add(0, Math.abs(helix2 / 20f), 0);
                            particle2.getWorld().spawnParticle(Particle.DUST, particle2, 1, 0, 0, 0,
                                    new Particle.DustOptions(Color.BLUE.setRed(100), 1.2f));
                            return true;
                        });
                    }

                    if (tags.contains("Atlantean"))
                    {
                        ItemStack boots = player.getInventory().getBoots();
                        if (boots != null && boots.getType() == Material.LEATHER_BOOTS)
                        {
                            ArrayList<Block> lava = BlockUtils.getSurfaceBlocks(BlockUtils.getNearbyBlocks(player.getLocation().subtract(0, 2, 0),
                                    4, 2, 4), false);

                            for (Block block : lava) {
                                if (BlockEvents.REPLACED.containsKey(block.getLocation()) && block.getType() == Material.OBSIDIAN) updated.add(block.getLocation());
                                if (block.getType() != Material.LAVA) continue;
                                updated.add(block.getLocation());

                                BlockEvents.REPLACED.putIfAbsent(block.getLocation(), block.getBlockData());
                                block.setType(Material.OBSIDIAN);

                                new BukkitRunnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        if (!updated.contains(block.getLocation()))
                                        {
                                            BlockEvents.setOriginal(block.getLocation());
                                            cancel();
                                        }
                                    }
                                }.runTaskTimer(NatureSMP.NATURE, 0, 100);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(this, 0, 3);
    }

    public static boolean isItem(ItemStack original, ItemStack check)
    {
        ItemMeta itemLore = null;
        ItemMeta newLore = null;

        if (original.hasItemMeta())
            itemLore = original.getItemMeta();

        if (check.hasItemMeta())
            newLore = check.getItemMeta();

        return (Objects.equals(itemLore, newLore));
    }

    public static void updateItems(Player player)
    {
        for (int i = -1; i < 40; i++)
        {
            ItemStack item;
            if (i >= 0) item = player.getInventory().getItem(i);
            else item = player.getInventory().getItemInOffHand();
            if (item == null) continue;

            ItemStack newItem = null;

            if (item.getPersistentDataContainer().has(Energizer.getKey()))
                newItem = Energizer.getItem();

            if (newItem != null && newItem.hasItemMeta() && !isItem(item, newItem))
            {
                if (i >= 0) player.getInventory().setItem(i, newItem);
                else player.getInventory().setItemInOffHand(newItem);
            }
        }
    }

    public static void setEntrail(Player player, BaseEntrail entrail) {
        BaseEntrail oldEntrail = getEntrail(player);

        for (BaseEntrail remove : EntrailList.entrails)
            player.removeScoreboardTag(remove.getName());

        if (!(entrail instanceof ResetEntrail))
            player.addScoreboardTag(entrail.getName());

        for (BaseEntrail.Effect oldMobEffect : oldEntrail.getEffects()) {
            for (PotionEffect effect : player.getActivePotionEffects())
            {
                if (oldMobEffect.type == effect.getType())
                    player.removePotionEffect(effect.getType());
            }
        }

        AttributeInstance speed = player.getAttribute(Attribute.MOVEMENT_SPEED);
        AttributeInstance damage = player.getAttribute(Attribute.ATTACK_DAMAGE);
        AttributeInstance sneak = player.getAttribute(Attribute.SNEAKING_SPEED);
        AttributeInstance health = player.getAttribute(Attribute.MAX_HEALTH);

        ArrayList<Object[]> modifiers = new ArrayList<>() {{
            add(new Object[]{Blazeborne.speedMod, speed});
            add(new Object[]{Blazeborne.damageMod, damage});
            add(new Object[]{Frosted.speedMod, speed});
            add(new Object[]{Shaded.speedMod, speed});
            add(new Object[]{Shaded.sneakMod, sneak});
            add(new Object[]{Shaded.darkMod, speed});
            add(new Object[]{Shaded.healthMod, health});
        }};

        for (Object[] modifier : modifiers) {
            AttributeModifier mod = (AttributeModifier) modifier[0];
            AttributeInstance instance = (AttributeInstance) modifier[1];

            if (instance != null && instance.getModifiers().contains(mod))
                instance.removeModifier(mod);
        }
    }

    public static void addEffects(Player player, BaseEntrail mob) {
        for (BaseEntrail.Effect effect : mob.getEffects())
            player.addPotionEffect(new PotionEffect(effect.type, INFINITE_DURATION, effect.amplifier, false, false, false));
    }

    public static BaseEntrail getEntrail(Player player)
    {
        for (BaseEntrail entrail : EntrailList.entrails) {
            if (player.getScoreboardTags().contains(entrail.getName())) {
                return entrail;
            }
        }
        return new ResetEntrail();
    }

    public void delay(Runnable task, long delayTicks) {
        getServer().getScheduler().runTaskLater(this, task, delayTicks);
    }
}
