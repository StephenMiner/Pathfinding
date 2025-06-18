package me.stephenminer.pathfinding.commands;

import me.stephenminer.pathfinding.Pathfinding;
import me.stephenminer.pathfinding.mobs.SmartZombie;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SetTargetCmd implements CommandExecutor {
    private final Pathfinding plugin;

    public SetTargetCmd(){
        this.plugin = JavaPlugin.getPlugin(Pathfinding.class);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        Location loc = player.getLocation();
        for (SmartZombie zombie : plugin.cachedSpawns.values()){
            zombie.setTargetBlock(loc.getX(), loc.getY(), loc.getZ());
        }
        plugin.locFile.getConfig().set("target-loc", plugin.locToString(loc));
        plugin.locFile.saveConfig();
        plugin.targetLoc = loc;
        sender.sendMessage(ChatColor.GREEN + "set target");

        return true;
    }
}
