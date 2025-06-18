package me.stephenminer.pathfinding.mobs;

import me.stephenminer.pathfinding.mobs.pathfinder.DigNavGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class EvilZombie extends Zombie {
    private final BlockPos targetPos;
    private Block[] blacklist;
    private DigNavGoal customGoal;
    public EvilZombie(org.bukkit.Location loc, org.bukkit.Location targetLoc) {
        super(EntityType.ZOMBIE, ((CraftWorld) loc.getWorld()).getHandle());
        Level world = ((CraftWorld) loc.getWorld()).getHandle();
        this.setPos(loc.getX(), loc.getY(), loc.getZ());
        world.addFreshEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
        this.targetPos = new BlockPos(targetLoc.getBlockX(), targetLoc.getBlockY(), targetLoc.getBlockZ());
        customGoal.setTargetPos(targetPos);
    }

    @Override
    public void registerGoals(){
        this.goalSelector.addGoal(0,new FloatGoal(this));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.blacklist = new Block[]{
                Blocks.BEDROCK,
                Blocks.OBSIDIAN
        };
        customGoal = new DigNavGoal(this, blacklist);
        this.goalSelector.addGoal(1, customGoal);
    }
}
