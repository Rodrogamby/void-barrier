package com.califralia.voidbarrier;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Main extends JavaPlugin implements Listener {

    public String prefix = "§8[§bServer§8] "; //Available in config
    public boolean enabled = true; //Available in config
    public String world = "world"; //Available in config
    public List<EntityType> whiteList = new ArrayList<>(); //Available in config
    public EntityStaticData EntityMaster = new EntityStaticData();
    public FileConfiguration config;

    @Override
    public void onEnable() {
        getLogger().info(ChatColor.GREEN + "VoidBarrier has been enabled.");
        this.saveDefaultConfig();
        config = this.getConfig();
        enabled = config.getBoolean("enabled");
        world = config.getString("worldName");
        prefix = config.getString("prefix");
        for (String s : config.getStringList("whitelisted")) {
            whiteList.add(EntityType.valueOf(s));
        }
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                if (enabled) fixPositions();
            }
        }, 0L, 1L);
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatColor.GREEN + "VoidBarrier has been disabled.");
    }

    public boolean isVoidBelowLocation(Location location) {
        Vector direction = new Vector();
        direction.setY(-1);
        RayTraceResult raytrace = location.getWorld().rayTraceBlocks(location, direction, location.getY(), FluidCollisionMode.SOURCE_ONLY, true);
        return raytrace == null && enabled;
    }

    public void fixPositions() {
        List<Entity> entities = Bukkit.getWorld(world).getEntities();
        for (Entity entity : entities) {
            if (!whiteList.contains(entity.getType())) {
                if (isVoidBelowLocation(entity.getLocation())) {
                    if (EntityMaster.isEntityOnMaster(entity.getEntityId())) {
                        Location current = entity.getLocation();
                        Location original = EntityMaster.getLocation(entity.getEntityId(), getServer().getWorld(world));
                        entity.getWorld().spawnParticle(Particle.PORTAL, original.getX(), original.getY(), original.getZ(), 100);
                        entity.getWorld().spawnParticle(Particle.PORTAL, current.getX(), current.getY(), current.getZ(), 100);
                        entity.teleport(original);
                    } else entity.remove(); //Removed for spawning in void
                }
            }
        }

    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (event.getWorld().getName().equals(world)) {
            List<Entity> entities = event.getWorld().getEntities();
            for (Entity entity : entities) {
                if (!whiteList.contains(entity.getType())) {
                    EntityMaster.saveEntity(entity);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity().getWorld().getName().equals(world)) {
            if (isVoidBelowLocation(event.getEntity().getLocation())) {
                event.setCancelled(true); //Cancels the event to avoid void-spawning
            } else if (!whiteList.contains(event.getEntity().getType())) {
                EntityMaster.saveEntity(event.getEntity());
            }
        }
    }

    @EventHandler
    public void onEntityRemove(EntityRemoveFromWorldEvent event) {
        if (event.getEntity().getWorld().getName().equals(world)) {
            if (!whiteList.contains(event.getEntity().getType())) {
                EntityMaster.removeEntity(event.getEntity().getEntityId());
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.getPlayer().hasPermission("voidbarrier.override")) {
            if (isVoidBelowLocation(event.getTo())) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("voidBarrier")) {
            if (sender instanceof Player) {
                if (sender.hasPermission("voidbarrier.barrier.toggle")) {
                    if (args.length > 0) {
                        switch (args[0]) {
                            case "enable":
                                enabled = true;
                                sender.sendMessage(prefix + ChatColor.GREEN + "The void barrier has been enabled.");
                                getLogger().info(ChatColor.GREEN + "VoidBarrier was enabled by " + sender.getName() + " on " + world);
                                return true;
                            case "disable":
                                enabled = false;
                                sender.sendMessage(prefix + ChatColor.YELLOW + "The void barrier has been disabled.");
                                getLogger().info(ChatColor.RED + "VoidBarrier was disabled by " + sender.getName() + " on " + world);
                                return true;
                            default:
                                return false;
                        }
                    } else {
                        sender.sendMessage(prefix + ChatColor.GRAY + "Running VoidBarrier " + ChatColor.YELLOW + "v1.1.0");
                        return true;
                    }
                }
            } else {
                sender.sendMessage("This command must be used by a player");
                return true;
            }
        }
        return false;
    }

}
