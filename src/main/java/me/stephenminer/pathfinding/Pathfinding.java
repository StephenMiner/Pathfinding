package me.stephenminer.pathfinding;

import me.stephenminer.pathfinding.commands.SetTargetCmd;
import me.stephenminer.pathfinding.commands.SpawnCmd;
import me.stephenminer.pathfinding.mobs.SmartZombie;
import me.stephenminer.pathfinding.commands.TeamPlace;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;


import java.util.HashMap;
import java.util.UUID;

public final class Pathfinding extends JavaPlugin implements Listener {
    public HashMap<UUID, SmartZombie> cachedSpawns;
    public TestBoard board;

    public ConfigFile locFile;

    public Location targetLoc;

    @Override
    public void onEnable() {
        this.locFile = new ConfigFile(this, "loc");
        loadTargetLoc();
        cachedSpawns = new HashMap<>();
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("settarget").setExecutor(new SetTargetCmd());
        getCommand("spawnzomb").setExecutor(new SpawnCmd());
        getCommand("teamplace").setExecutor(new TeamPlace());
        board = new TestBoard();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    @EventHandler
    public void onDeath(EntityDeathEvent event){
        if (cachedSpawns.containsKey(event.getEntity().getUniqueId())){
            cachedSpawns.remove(event.getEntity().getUniqueId());
        }
    }

    public Location fromStr(String str){
        String[] unbox = str.split(",");
        String worldName = unbox[0];
        double x = Double.parseDouble(unbox[1]);
        double y = Double.parseDouble(unbox[2]);
        double z = Double.parseDouble(unbox[3]);
        World world = Bukkit.getWorld(worldName);
        return new Location(world, x, y, z);

    }

    public String locToString(Location loc){
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
    }


    public void loadTargetLoc(){
        if (!this.locFile.getConfig().contains("target-loc")) return;
        String entry = this.locFile.getConfig().getString("target-loc");
        this.targetLoc = this.fromStr(entry);
    }
}
