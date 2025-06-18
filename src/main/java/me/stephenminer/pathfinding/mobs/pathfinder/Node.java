package me.stephenminer.pathfinding.mobs.pathfinder;

import net.minecraft.core.BlockPos;

/**
 *
 * @param pos the positon of Node in a World
 * @param parent the previous Node in a path
 * @param moveCost the cost to move from the start Node to this Node
 * @param estCost the heuristic cost to travel from
 *                this node to the goal of the path
 * @param action  What kind of action does this node entail
 */
public record Node(BlockPos pos, Node parent, double moveCost, double estCost, MoveType action) {

    public double totalCost(){
        return moveCost + estCost;
    }

    public boolean walk(){ return action == MoveType.WALK; }
    public boolean dig(){ return action == MoveType.DIG; }
    public boolean build(){ return action == MoveType.BUILD; }


    public enum MoveType{
        WALK,
        DIG,
        BUILD
    }
}
