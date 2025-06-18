package me.stephenminer.pathfinding.mobs.pathfinder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

public class NavToBlock extends Goal {
    private final PathfinderMob mob;
    private final double speedMod;
    private BlockPos pos;
    private Level level;

    public NavToBlock(PathfinderMob mob, double speedMod, BlockPos pos){
        this.mob = mob;
        this.speedMod = speedMod;
        this.level = mob.level();
        this.pos = pos;
    }

    @Override
    public boolean canUse() {
        return pos != null && distSqToTarget() > 100 && mob.getTarget() == null;
    }

    @Override
    public boolean canContinueToUse(){
        return !mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        if (pos == null) return;
        mob.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), speedMod);
    }

    /*
    @Override
    public void tick(){
        if (!canContinueToUse()) return;
        if (canUse()){
            System.out.println(111);
            mob.getNavigation().moveTo(pos.getX(),pos.getY(),pos.getZ(),speedMod);
        }
    }

     */



    public void setTarget(BlockPos pos){
        this.pos = pos;
        mob.getNavigation().moveTo(pos.getX(),pos.getY(),pos.getZ(),speedMod);
    }

    public void setTarget(double wantedX, double wantedY, double wantedZ){
        setTarget(new BlockPos((int)wantedX, (int) wantedY, (int) wantedZ));
    }

    private double distSqToTarget(){
        if (pos == null) return -100;
        else return Math.pow(mob.getX()-pos.getX(),2) + Math.pow(mob.getY()-pos.getY(),2) + Math.pow(mob.getZ()-pos.getZ(),2);
    }
}
