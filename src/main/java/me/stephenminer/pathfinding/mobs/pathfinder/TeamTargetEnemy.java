package me.stephenminer.pathfinding.mobs.pathfinder;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

public class TeamTargetEnemy<T extends LivingEntity> extends NearestAttackableTargetGoal<T>{
    public TeamTargetEnemy(Mob mob, Class<T> clazz, boolean flag) {
        super(mob, clazz, flag);
    }


    public TeamTargetEnemy(Mob entityinsentient, Class<T> oclass, boolean flag, boolean flag1) {
        super(entityinsentient, oclass, 10, flag, flag1, (Predicate)null);
    }

    public TeamTargetEnemy(Mob mob, Class<T> clazz, int i, boolean flag, boolean flag1, @Nullable Predicate<LivingEntity> predicate) {
        super(mob,clazz,i, flag, flag1,predicate);
    }


    @Override
    protected void findTarget() {
        if (this.targetType != Player.class && this.targetType != ServerPlayer.class) {
            this.target = this.mob.level().getNearestEntity(this.mob.level().getEntitiesOfClass(this.targetType, this.getTargetSearchArea(this.getFollowDistance()), (entityliving) -> {
                return true;
            }), this.targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        } else {

            this.target = this.mob.level().getNearestPlayer(this.targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        }

    }

}
