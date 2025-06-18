package me.stephenminer.pathfinding.commands;

import me.stephenminer.pathfinding.Pathfinding;
import me.stephenminer.pathfinding.mobs.EvilZombie;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SpawnCmd implements CommandExecutor {
    private final Pathfinding plugin;
    public SpawnCmd(){
        this.plugin = JavaPlugin.getPlugin(Pathfinding.class);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (plugin.targetLoc == null) {
            sender.sendMessage("You need to set a target location");
            return false;
        }
        if (sender instanceof Player){
            Player player = (Player) sender;
            Location loc = player.getLocation();
            EvilZombie evilZombie = new EvilZombie(loc,plugin.targetLoc);
           // SmartZombie smartZombie = new SmartZombie(loc, plugin.board.getTeam());
          //  plugin.cachedSpawns.put(smartZombie.getUUID(),smartZombie);
            return true;
        }else return false;
    }
}
