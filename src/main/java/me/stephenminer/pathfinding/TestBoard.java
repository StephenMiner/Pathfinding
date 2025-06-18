package me.stephenminer.pathfinding;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TestBoard {
    private Scoreboard scoreboard;
    private final Pathfinding plugin;
    public TestBoard(){
        this.plugin = JavaPlugin.getPlugin(Pathfinding.class);
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        initTeams();
    }


    private void initTeams(){
        Team team = scoreboard.registerNewTeam("testing");
        team.setColor(ChatColor.AQUA);
    }

    public Team getTeam(){
        return scoreboard.getTeam("testing");
    }

    public Scoreboard getBoard() {
        return scoreboard;
    }
}
