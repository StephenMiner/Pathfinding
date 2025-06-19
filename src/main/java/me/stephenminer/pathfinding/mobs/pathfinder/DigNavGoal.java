package me.stephenminer.pathfinding.mobs.pathfinder;

import com.google.common.collect.Sets;
import me.stephenminer.pathfinding.Pathfinding;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.List;

public class DigNavGoal extends Goal {
    protected final Mob mob;
    protected BlockPos targetPos;
    protected List<Node> path;
    protected int actionCooldown = 40;
    protected int stepIndex = 0;
    protected int digIndex, buildIndex = 0;
    protected int breakProg = 0;
    protected int maxBreakTime = 40;
    protected boolean digging = false;
    protected boolean moveFlag = false;
    protected int recalcCooldown = 0;
    protected Vec3 prevPos = null;
    protected int stuck = 0;
    protected final DigPathfinder pathfinder;
    protected static final int MAX_STUCK_TIME = 40; //in ticks
    protected static final double MAX_STUCK_THRESHHOLD = 0.05;

    public DigNavGoal(Mob mob, Block[] blacklist){
        this.mob = mob;
        pathfinder = new DigPathfinder(mob.level(),3, Sets.newHashSet(blacklist));
    }


    @Override
    public void start(){
        recalcPath();
        //System.out.println(path);
        stepIndex = 0;
        prevPos = mob.position();
        stuck = 0;
        breakProg = 0;
        recalcCooldown = 0;
        buildIndex= 0;
        digIndex = 0;
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
        BlockPos[] build = current.buildTargets();
        BlockPos[] dig = current.digTargets();
        int buildLength = build == null ? 0 : build.length;
        int digLength = dig == null ? 0 : dig.length;
        if (!moveFlag) {
            /*
                There are no blocks to dig or place so we may move to our destination
             */
            if (buildIndex >= buildLength && digIndex >= digLength) {
                System.out.println(2);
                moveFlag = true;
                buildIndex = 0;
                digIndex = 0;
            }
            if (digIndex < digLength) {
                System.out.println(5);
                mob.getNavigation().stop();
                if (actionCooldown != 0) {
                    System.out.println(9);
                    return;
                }
                digging = true;
                BlockState state = mob.level().getBlockState(dig[digIndex]);
                if (!pathfinder.canDig(state)) {
                    System.out.println("RECALCULATING PATH");
                    recalcPath();
                    return;
                }
                if (breakProg % 10 == 0)
                    mob.swing(InteractionHand.MAIN_HAND);
                if (breakProg < maxBreakTime) {
                    System.out.println(10);
                    return;
                }
                digging = false;
                Level level = mob.level();
                mob.swing(InteractionHand.MAIN_HAND);

                //Since digIndex < digLength, we know at this point dig isn't null
                level.destroyBlock(dig[digIndex], true, mob);
                breakProg = 0;
                actionCooldown = 25;
                digIndex++;
                stuck = 0;
                System.out.println(4);
            }
            if (buildIndex < buildLength) {
                System.out.println(5);
                mob.getNavigation().stop();
                if (actionCooldown > 0) return;
                actionCooldown = 25;
                Level level = mob.level();
                //Since buildIndex < buildLength, we know build isn't null here
                level.setBlockAndUpdate(build[buildIndex], Blocks.OAK_PLANKS.defaultBlockState());
                buildIndex++;
                stuck = 0;
                System.out.println(6);
            }
        }else {
           // System.out.println(Arrays.toString(build) + "||||" + Arrays.toString(dig));
            boolean res = mob.getNavigation().moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 1.0);
          //  System.out.println(mob.position().distanceToSqr(pos.getCenter()));
            double distSqr = mob.position().distanceToSqr(pos.getCenter());
            if (mob.position().distanceToSqr(prevPos) < MAX_STUCK_THRESHHOLD){
                stuck++;
            }else stuck = 0;
            if (distSqr < 2.1) {
                stepIndex++;
                moveFlag = false;
                stuck = 0;
            }
        }
        if (stuck >= MAX_STUCK_TIME){
            System.out.println("RECALCULATING PATH");
            recalcPath();
            stuck = 0;
        }
        if (path != null)
            System.out.println("type: " + current.type() + " position: " + stepIndex + "/" + path.size());
        prevPos = mob.position();
    }

    @Override
    public void stop(){
        path = null;
        stepIndex = 0;
        buildIndex = 0;
        digIndex = 0;
        moveFlag = false;
    }


    private void recalcPath(){
        path = pathfinder.findPath(mob.blockPosition(), targetPos);
        stepIndex = 0;
        buildIndex = 0;
        digIndex = 0;
        stuck = 0;
        moveFlag = false;
    }


    public void setTargetPos(BlockPos pos){
        this.targetPos = pos;
        recalcPath();
    }

}
