package com.califralia.voidbarrier;

import org.bukkit.Location;
import org.bukkit.World;
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
        if(entityIds.contains(id)){
            return true;
        }
        return false;
    }

    public Location getLocation(int id, World world){
        int index = entityIds.indexOf(id);
        return new Location(world, entX.get(index), entY.get(index), entZ.get(index));
    }

    public void saveEntity(Entity entity){
        double x = entity.getLocation().getX();
        double y = entity.getLocation().getY();
        double z = entity.getLocation().getZ();
        int id = entity.getEntityId();
        if(id > -1) {
            entityIds.add(id);
            entX.add(x);
            entY.add(y);
            entZ.add(z);
        }
    }

    public void removeEntity(int id) {
        int index = entityIds.indexOf(id);
        if (index > -1) {
            entityIds.remove(index);
            entX.remove(index);
            entY.remove(index);
            entZ.remove(index);
        }
    }

}
