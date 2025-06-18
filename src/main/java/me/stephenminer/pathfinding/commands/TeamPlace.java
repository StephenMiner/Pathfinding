package me.stephenminer.pathfinding.commands;

import me.stephenminer.pathfinding.Pathfinding;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TeamPlace implements CommandExecutor {
    private final Pathfinding plugin;
    public TeamPlace(){
        this.plugin = JavaPlugin.getPlugin(Pathfinding.class);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (sender instanceof Player){
            Player player = (Player) sender;
            plugin.board.getTeam().addPlayer(player);
            player.sendMessage(ChatColor.GREEN + "Checkmark");
            return true;
        }
        return false;
    }
}
