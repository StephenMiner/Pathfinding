package me.stephenminer.pathfinding.mobs.pathfinder;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DigNavGoal extends Goal {
    protected final Mob mob;
    protected BlockPos targetPos;
    protected List<Node> path;
    protected int actionCooldown = 40;
    protected int stepIndex = 0;
    protected int breakProg = 0;
    protected int maxBreakTime = 40;
    protected boolean digging = false;
    protected int recalcCooldown = 0;
    protected Vec3 prevPos = null;
    protected int stuck = 0;
    protected final DigPathfinder pathfinder;
    protected static final int MAX_STUCK_TIME = 40; //in ticks

    public DigNavGoal(Mob mob, Block[] blacklist){
        this.mob = mob;
        pathfinder = new DigPathfinder(mob.level(),3, Sets.newHashSet(blacklist));
    }


    @Override
    public void start(){
        recalcPath();
        System.out.println(path);
        stepIndex = 0;
        prevPos = mob.position();
        stuck = 0;
        breakProg = 0;
        recalcCooldown = 0;
    }

    @Override
    public boolean canUse() {
        return targetPos != null && path != null && stepIndex < path.size();
    }


    @Override
    public void tick(){
        if (!digging && actionCooldown > 0){
            actionCooldown--;
        }
        if (digging && breakProg < maxBreakTime){
            breakProg++;
        }

        if (stepIndex >= path.size()) return;
        System.out.println(1);
        Node current = path.get(stepIndex);
        BlockPos pos = current.pos();
        switch (current.action()){
            case WALK -> {
                System.out.println(2);
                boolean res = mob.getNavigation().moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 1.0);
                System.out.println(mob.position().distanceToSqr(pos.getCenter()));
                if (mob.position().distanceToSqr(pos.getCenter()) < 2)
                    stepIndex++;
            }
            case DIG -> {
                System.out.println(3);
                mob.getNavigation().stop();
                digging = true;
                if (actionCooldown != 0) {
                    System.out.println(9);
                    break;
                }
                if (breakProg < maxBreakTime) {
                    System.out.println(10);
                    break;
                }
                digging = false;
                Level level = mob.level();
                level.destroyBlock(pos,true, mob);
                breakProg = 0;
                actionCooldown = 25;
                stepIndex++;
                System.out.println(4);
            }
            case BUILD -> {
                System.out.println(5);
                mob.getNavigation().stop();
                if (actionCooldown > 0) break;
                actionCooldown = 25;
                Level level = mob.level();
                level.setBlockAndUpdate(pos.below(), Blocks.OAK_PLANKS.defaultBlockState());
                stepIndex++;
                System.out.println(6);
            }


        }
        if (path != null)
            System.out.println("position: " + stepIndex + "/" + path.size());

    }

    @Override
    public void stop(){
        path = null;
        stepIndex = 0;
    }


    private void recalcPath(){
        path = pathfinder.findPath(mob.blockPosition(), targetPos);
        stepIndex = 0;
    }


    public void setTargetPos(BlockPos pos){
        this.targetPos = pos;
        recalcPath();
    }

}
