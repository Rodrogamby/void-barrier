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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.List;

public class Main extends JavaPlugin implements Listener {

    public String prefix = "§8[§bServer§8] ";
    public EntityStaticData EntityMaster = new EntityStaticData();
    private boolean firstPlayer = true;
    public boolean enabled = true;
    public String world = "world";
    public FileConfiguration config;
    public EntityType[] whitelist = {
            EntityType.PLAYER,
            EntityType.BOAT,
            EntityType.EXPERIENCE_ORB,
            EntityType.MINECART,
            EntityType.MINECART_CHEST,
            EntityType.MINECART_COMMAND,
            EntityType.MINECART_FURNACE,
            EntityType.MINECART_HOPPER,
            EntityType.MINECART_MOB_SPAWNER,
            EntityType.MINECART_TNT,
            EntityType.DROPPED_ITEM,
            EntityType.ITEM_FRAME,
            EntityType.PAINTING,
    };

    @Override
    public void onEnable(){
        getLogger().info(ChatColor.GREEN +"VoidBarrier has been enabled.");
        this.saveDefaultConfig();
        config = this.getConfig();
        enabled = config.getBoolean("enabled");
        world = config.getString("worldName");
        prefix = config.getString("prefix");
        if(Bukkit.getWorld(world) == null){
            world = Bukkit.getServer().getWorlds().get(0).getName();
            enabled = false;
        }
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                if(enabled) fixPositions(); //does having a condition here cause lag?
            }
        }, 0L, 1L);
    }

    @Override
    public void onDisable(){
        getLogger().info(ChatColor.GREEN + "VoidBarrier has been disabled.");
    }

    public boolean isVoidBelowLocation(Location loc){
        Location location = loc.clone().getBlock().getLocation();
        int x = location.getBlockX();
        int originalY = location.getBlockY();
        int z = location.getBlockZ();
        if(!enabled) return false;
        for(int y = 0; y <= originalY; y++){
            if(!Bukkit.getWorld(world).getBlockAt(x, y, z).getType().equals(Material.AIR)){
                return false;
            }
        }
        return true;
    }

    public boolean isTypeOnWhitelist(EntityType entiType){
        for(EntityType type : whitelist){
            if(entiType == type){
                return true;
            }
        }
        return false;
    }

    public void fixPositions(){
        List<Entity> entities = Bukkit.getWorld(world).getEntities();
        for(Entity entity : entities){
            if(!isTypeOnWhitelist(entity.getType())) {
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
    public void onPlayerJoin(PlayerJoinEvent event){
        if(Bukkit.getServer().getOnlinePlayers().size() == 1 && firstPlayer){
            firstPlayer = false;
            List<Entity> entities= Bukkit.getWorld(world).getEntities();
            for(int i = 0; i < entities.size(); i++){
                if(!isTypeOnWhitelist(entities.get(i).getType())) {
                    EntityMaster.saveEntity(entities.get(i));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntitySpawn(EntitySpawnEvent event){
        if(event.getEntity().getWorld().getName().equals(world)) {
            if(isVoidBelowLocation(event.getEntity().getLocation())){
                event.setCancelled(true); //Cancels the event to avoid void-spawning
            }else if (!isTypeOnWhitelist(event.getEntity().getType())) {
                EntityMaster.saveEntity(event.getEntity());
            }
        }
    }

    @EventHandler
    public void onEntityRemove(EntityRemoveFromWorldEvent event){
        if(event.getEntity().getWorld().getName().equals(world)) {
            if (!isTypeOnWhitelist(event.getEntity().getType())) {
                EntityMaster.removeEntity(event.getEntity().getEntityId());
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        if(isVoidBelowLocation(event.getTo())){
            if(!event.getPlayer().hasPermission("voidbarrier.override")) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(cmd.getName().equalsIgnoreCase("voidBarrier")){
            if (sender instanceof Player) {
                if(sender.hasPermission("voidbarrier.barrier.toggle")) {
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
                }
            } else {
                sender.sendMessage("This command must be used by a player");
                return false;
            }
            return false;
        }
        return false;
    }
}