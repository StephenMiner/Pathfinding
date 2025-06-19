package me.stephenminer.pathfinding.mobs.pathfinder;

import net.minecraft.core.BlockPos;

/**
 *
 * @param pos the positon of Node in a World (move target)
 * @param parent the previous Node in a path
 * @param moveCost the cost to move from the start Node to this Node
 * @param estCost the heuristic cost to travel from
 *                this node to the goal of the path
 * @param buildTargets  What blocks must we place to reach the specified position
 * @param digTargets What blocks must we place to reach the specified position
 */
public record Node(BlockPos pos, Node parent, double moveCost, double estCost, BlockPos[] buildTargets, BlockPos[] digTargets, char type) {

    public double totalCost(){
        return moveCost + estCost;
    }

}
