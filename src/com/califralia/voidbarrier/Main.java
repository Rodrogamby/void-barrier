package com.califralia.voidbarrier;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
    @Override
    public void onEnable(){
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        getLogger().info(ChatColor.GREEN + "VoidBarrier has been enabled.");
    }

    @Override
    public void onDisable(){
        getLogger().info(ChatColor.GREEN + "VoidBarrier has been disabled.");
    }

    public boolean isVoidBelowLocation(Location loc, Entity entity){
        //Takes the given location and removes decimals by getting the block location.
        Location location = loc.clone().getBlock().getLocation();
        int nonAirBlocks = 0;
        int x = location.getBlockX();
        int originalY = location.getBlockY();
        int z = location.getBlockZ();
        for(int y = 0; y <= originalY; y++){
            if(!entity.getWorld().getBlockAt(x, y, z).getType().equals(Material.AIR)){
                nonAirBlocks += 1;
            }
        }
        if(nonAirBlocks > 0){
            return false;
        } else return true;
    }

    @EventHandler
    public void isPlayerOnVoid(PlayerMoveEvent event){
        if(isVoidBelowLocation(event.getTo(), event.getPlayer())){
            event.setCancelled(true);
            getLogger().info(ChatColor.RED + event.getPlayer().getName() + " tried to go into void.");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(command.getName().equalsIgnoreCase("detectVoid")){
            if(!(sender instanceof Player)){
                sender.sendMessage(ChatColor.RED + "This command may only be used by players.");
                return true;
            }
            Player player = Bukkit.getServer().getPlayer(sender.getName());
            if(isVoidBelowLocation(player.getLocation(), player)){
                player.sendMessage(ChatColor.GREEN + "You are on void.");
                return true;
            } else{
                player.sendMessage(ChatColor.RED + "You are not on void.");
                return true;
            }
        } else
        return false;
    }
}
