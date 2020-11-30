package com.califralia.voidbarrier;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class EntityStaticData{

    public EntityStaticData(){
        //constructor
    }

    private List<Integer> entityIds = new ArrayList<>();
    private List<Double> entX = new ArrayList<>();
    private List<Double> entY = new ArrayList<>();
    private List<Double> entZ = new ArrayList<>();

    public boolean isEntityOnMaster(int id){
        for(int entId : entityIds){
            if(entId == id){
                return true;
            }
        }
        return false;
    }

    public Location getLocation(int id){ 
        int counter = -1;
        for(int i = 0; i < entityIds.size(); i++){ //use indexOf instead?
            counter += 1;
            if(entityIds.get(i) == id){
                break;
            }
        }
        return new Location(Bukkit.getServer().getWorld("world"), entX.get(counter), entY.get(counter), entZ.get(counter));
    }

    public void saveEntity(Entity entity){
        double x = entity.getLocation().getX();
        double y = entity.getLocation().getY();
        double z = entity.getLocation().getZ();
        int id = entity.getEntityId();
        entityIds.add(id);
        entX.add(x);
        entY.add(y);
        entZ.add(z);
        Bukkit.getServer().getLogger().info("Entity " + id + " was added: " + ChatColor.YELLOW + "" + x + ", " + y + ", " + z);
    }

    public void removeEntity(int id){ 
        for(int i = 0; i < entityIds.size(); i++){ //use indexOf?
            if(entityIds.get(i) == id){
                entityIds.remove(i);
                entX.remove(i);
                entY.remove(i);
                entZ.remove(i);
                Bukkit.getServer().getLogger().info("Entity " + id + " was removed.");
                return;
            }
        }
        Bukkit.getServer().getLogger().info(ChatColor.RED + "Entity " + id + " could not be removed.");
    }


}
