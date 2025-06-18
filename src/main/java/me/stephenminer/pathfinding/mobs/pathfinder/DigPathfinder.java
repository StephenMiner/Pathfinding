package me.stephenminer.pathfinding.mobs.pathfinder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class DigPathfinder {
    private final Level world;
    private final int maxFallDist;
    private final Set<Block> blacklist;

    public DigPathfinder(Level world, int maxFallDist, Set<Block> blacklist){
        this.world = world;
        this.maxFallDist = maxFallDist;
        this.blacklist = blacklist;
    }


    public List<Node> findPath(BlockPos start, BlockPos goal){
        PriorityQueue<Node> open = new PriorityQueue(Comparator.comparingDouble(Node::totalCost));
        Map<BlockPos, Node> visited = new HashMap<>();
        Node head = new Node(start, null, 0, 0, Node.MoveType.WALK);
        open.add(head);
        while (!open.isEmpty()){
            Node current = open.poll();
            Node best = visited.getOrDefault(current.pos(), null);
            /*
                If we already have a node at this position then it should be the best node
                so the current node we look at is old and sad :(
                (has a higher cost than the one in our visited Map if it isn't equal to the node in the map)
             */
            if (best != null && !current.equals(best)) continue;

            if (current.pos().equals(goal)) {
                return reconstructPath(current);
            }

            BlockPos[] neighbors = getNeighbors(current.pos());
            for (BlockPos pos : neighbors){
                BlockPos above = pos.above();
                BlockPos below = pos.below();

                BlockState state = world.getBlockState(pos); //foot position
                BlockState aboveState = world.getBlockState(above); //head position
                BlockState belowState = world.getBlockState(below); //floor position

                Node.MoveType action;
                float cost;

                if (walkable(aboveState) && isSolid(below, belowState) && walkable(state)){
                    //There are no obstructions to us walking
                    action = Node.MoveType.WALK;
                    cost = 1.0f;
                }else if (walkable(state) && walkable(aboveState) && canBridge(belowState)){
                    //The area in front of us is passable, but we need to create a platform
                    action = Node.MoveType.BUILD;
                    cost = 2.5f;
                }else if ((walkable(state) && canDig(aboveState)) || (walkable(aboveState) && canDig(state))){
                    //In a 2 block space at the feet and head, we only need to dig one block (at the foot or at the head)
                    action = Node.MoveType.DIG;
                    cost = 4.0f;
                }else if (canDig(state) && canDig(aboveState)){
                    //In a 2 block space at the feet and head, we must dig both blocks (both at the feet and the head)
                    action = Node.MoveType.DIG;
                    cost = 5.0f;
                } else {
                    //We found no valid action for this neighbor
                    continue;
                }
                double moveCost = current.moveCost() + cost;
                /*
                    Only add the node if we don't have one at the current block position
                    or
                    the cost from the start to this node is cheaper than the existing node's
                    cost from the start at this position
                 */

                if (!visited.containsKey(pos) || moveCost < visited.get(pos).moveCost()){
                    double estCost = manhattanDist(pos,goal);
                    Node node = new Node(pos, current, moveCost, estCost, action);
                    visited.put(pos,node);
                    open.add(node);
                }

            }
        }
        //No path found
        return null;
    }


    /**
     * Constructs a path based on a Node traversing the parent nodes inserting them
     * at the front of a linked list
     * @param node the end node for a given node chain
     * @return A LinkedList of Nodes in the proper order where the provided node is last
     *         and its upmost parent is first.
     */
    public List<Node> reconstructPath(Node node){
        LinkedList<Node> path = new LinkedList<>();
        while (node != null){
            path.addFirst(node);
            node = node.parent();
        }
        return path;
    }




    private BlockPos[] getNeighbors(BlockPos pos){
        BlockPos[] positions = new BlockPos[14];
        positions[0] = pos.north();
        positions[1] = pos.east();
        positions[2] = pos.south();
        positions[3] = pos.west();

        positions[4] = pos.above();
        positions[5] = pos.below();

        positions[6] = pos.north().above();
        positions[7] = pos.east().above();
        positions[8] = pos.west().above();
        positions[9] = pos.south().above();

        positions[10] = pos.north().below();
        positions[11] = pos.east().below();
        positions[12] = pos.south().below();
        positions[13] = pos.west().below();
        return positions;
    }


    private float manhattanDist(BlockPos pos1, BlockPos pos2){
        return Math.abs(pos1.getX() - pos2.getX()) + Math.abs(pos1.getY() - pos2.getY()) + Math.abs(pos1.getZ() - pos2.getZ());
    }


    private boolean walkable(BlockState state){
        return state.isAir() || state.canBeReplaced();
    }

    private boolean isSolid(BlockPos pos, BlockState state){
        return !state.isAir() && !state.getCollisionShape(world, pos).isEmpty();
    }

    private boolean canDig(BlockState state){
        return !blacklist.contains(state.getBlock()) && !state.isAir() && !liquid(state);
    }

    private boolean liquid(BlockState state){
        return state.is(Blocks.WATER) || state.is(Blocks.LAVA);
    }

    private boolean canBridge(BlockState state){
        return state.isAir() || state.canBeReplaced() || liquid(state);
    }

}
