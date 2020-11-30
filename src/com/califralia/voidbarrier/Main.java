package com.califralia.voidbarrier;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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

    public EntityStaticData EntityMaster = new EntityStaticData();
    private boolean firstPlayer = true;

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
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                fixPositions();
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
        for(int y = 0; y <= originalY; y++){
            if(!Bukkit.getWorld("world").getBlockAt(x, y, z).getType().equals(Material.AIR)){
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
        List<Entity> entities = Bukkit.getWorld("world").getEntities();
        for(Entity entity : entities){
            if(!isTypeOnWhitelist(entity.getType())) {
                if (isVoidBelowLocation(entity.getLocation())) {
                    if (EntityMaster.isEntityOnMaster(entity.getEntityId())) {
                        entity.teleport(EntityMaster.getLocation(entity.getEntityId()));
                    } else entity.remove(); //Removed for spawning in void
                }
            }
        }

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        if(Bukkit.getServer().getOnlinePlayers().size() == 1 && firstPlayer){ //makes sure this is only executed of the 1st player.
            firstPlayer = false;
            List<Entity> entities= Bukkit.getWorld("world").getEntities();
            for(int i = 0; i < entities.size(); i++){
                if(!isTypeOnWhitelist(entities.get(i).getType())) {
                    EntityMaster.saveEntity(entities.get(i));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntitySpawn(EntitySpawnEvent event){
        if(event.getEntity().getWorld().getName().equals("world")) {
            if(isVoidBelowLocation(event.getEntity().getLocation())){
                event.setCancelled(true); //Cancels the event to avoid void-spawning
            }else if (!isTypeOnWhitelist(event.getEntity().getType())) {
                EntityMaster.saveEntity(event.getEntity());
            }
        }
    }

    @EventHandler
    public void onEntityRemove(EntityRemoveFromWorldEvent event){
        if(event.getEntity().getWorld().getName().equals("world")) {
            if (!isTypeOnWhitelist(event.getEntity().getType())) {
                EntityMaster.removeEntity(event.getEntity().getEntityId());
            }
        }
    }

    @EventHandler
    public void isPlayerOnVoid(PlayerMoveEvent event){
        if(isVoidBelowLocation(event.getTo())){
            event.setCancelled(true);
        }
    }

}
