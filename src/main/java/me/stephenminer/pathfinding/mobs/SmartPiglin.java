package me.stephenminer.pathfinding.mobs;

import me.stephenminer.pathfinding.Pathfinding;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;
import org.bukkit.plugin.java.JavaPlugin;

public class SmartPiglin extends Piglin {
    private final Pathfinding plugin;
    public SmartPiglin(Location loc){
        super(EntityType.PIGLIN, ((CraftWorld)loc.getWorld()).getHandle());
        this.plugin = JavaPlugin.getPlugin(Pathfinding.class);
    }


    @Override
    public void registerGoals(){

    }
}
