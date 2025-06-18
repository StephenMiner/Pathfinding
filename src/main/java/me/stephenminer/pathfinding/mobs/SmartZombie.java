package me.stephenminer.pathfinding.mobs;

import me.stephenminer.pathfinding.mobs.pathfinder.NavToBlock;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;
import org.bukkit.scoreboard.Team;

public class SmartZombie extends Zombie {
    private NavToBlock navGoal;
    private Team team;
    public SmartZombie(Location loc) {
        this(loc, null);
    }
    public SmartZombie(Location loc, Team team){
        super(EntityType.ZOMBIE, ((CraftWorld) loc.getWorld()).getHandle());
        Level world = ((CraftWorld) loc.getWorld()).getHandle();
        this.setPos(loc.getX(),loc.getY(),loc.getZ());
        world.addFreshEntity(this);
        this.team = team;
    }


    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0,new FloatGoal(this));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0, false));
        navGoal = new NavToBlock(this,1.0d,null);
        this.goalSelector.addGoal(3, navGoal);
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, new Class[0])).setAlertOthers(new Class[]{ZombifiedPiglin.class}));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, (entity) -> {
            if (entity instanceof Player) {
                Player p = (Player) entity;
                org.bukkit.entity.Player player = Bukkit.getPlayer(p.getUUID());
                return team == null || (player != null && !team.hasPlayer(player));
            }
            return true;
        }));
    }


    public void setTargetBlock(double x, double y, double z){
        navGoal.setTarget(x,y,z);
    }

    public boolean hasTeam(){ return team != null; }
    public Team team(){ return team; }
    public void setTeam(Team team){ this.team = team; }
}