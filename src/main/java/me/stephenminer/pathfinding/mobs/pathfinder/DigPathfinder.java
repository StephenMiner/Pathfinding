package me.stephenminer.pathfinding.mobs.pathfinder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;

import java.util.*;

public class DigPathfinder {
    private final Level world;
    private final int maxFallDist;
    private final Set<Block> blacklist;

    private final float walkCost, buildCost, digCost;
    public DigPathfinder(Level world, int maxFallDist, Set<Block> blacklist){
        this.world = world;
        this.maxFallDist = maxFallDist;
        this.blacklist = blacklist;
        this.walkCost = 1.0f;
        this.buildCost = 3.0f;
        this.digCost = 4.5f;
    }


    public List<Node> findPath(BlockPos start, BlockPos goal){
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(Node::totalCost));
        Map<BlockPos, Node> visited = new HashMap<>();
        Node head = new Node(start, null, 0, 0,null, null, '.');
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
            BlockPos currentAboveHead = current.pos().above().above();

            BlockPos[] neighbors = getNeighbors(current.pos());
            for (BlockPos pos : neighbors){
                BlockPos above = pos.above();
                BlockPos below = pos.below();

                BlockState state = world.getBlockState(pos); //foot position
                BlockState aboveState = world.getBlockState(above); //head position
                BlockState belowState = world.getBlockState(below); //floor position

                int dy = pos.getY() - current.pos().getY();
                char type;
                float cost;
                BlockPos[] digTargets = null;
                BlockPos[] buildTargets = null;
                float extra = 0.0f;
                if (walkable(aboveState) && isSolid(below, belowState) && walkable(state)){
                    BlockPos extraPos = digExtraCeiling(dy, current.pos(), pos);
                    if (extraPos != null){
                        if (!canDig(world.getBlockState(extraPos))) continue;
                        cost = 4.5f;
                        digTargets = new BlockPos[]{extraPos};
                        type = 'b';
                    } else {
                        //There are no obstructions to us walking
                        cost = 1.0f;
                        type = 'a';
                    }
                }else if (walkable(state) && walkable(aboveState) && canBridge(belowState)){
                    int dx = current.pos().getX() - pos.getX();
                    int dz = current.pos().getZ() - pos.getZ();
                    if (dx == 0 && dz == 0){
                        System.out.println("NERD-POLE");
                        continue;
                    }
                    BlockPos extraPos = digExtraCeiling(dy, current.pos(),pos);

                    if (extraPos != null){
                        if (!canDig(world.getBlockState(extraPos))) continue;
                        extra = 4.5f;
                        digTargets = new BlockPos[]{extraPos};
                        type = 'g';
                    }
                    //The area in front of us is passable, but we need to create a platform
                    cost = extra + 3.0f;
                    buildTargets = new BlockPos[]{below};
                    type = 'c';
                }else if (isSolid(below, belowState) && walkable(state) && canDig(aboveState)){
                    //In a 2 block space at the feet and head, we only need to dig the one at our head
                    BlockPos digCeil = digExtraCeiling(dy, current.pos(), pos);
                    if (digCeil != null && !canDig(world.getBlockState(digCeil))) continue;
                    digTargets = digCeil == null ? new BlockPos[]{above} : new BlockPos[]{above, digCeil};
                    cost = digCeil == null ? 4.5f : 6f;
                    type = 'd';

                }else if (walkable(aboveState) && canDig(state)){
                    BlockPos digCeil = digExtraCeiling(dy, current.pos(), pos);
                    if (digCeil != null && !canDig(world.getBlockState(digCeil))) continue;
                    digTargets = digCeil == null ? new BlockPos[]{pos} : new BlockPos[]{pos, digCeil};
                    cost = digCeil == null ? 4.5f : 6f;
                    type = 'e';
                }else if (canDig(state) && canDig(aboveState)){
                    //In a 2 block space at the feet and head, we must dig both blocks (both at the feet and the head)
                    BlockPos digCeil = digExtraCeiling(dy, current.pos(), pos);
                    if (digCeil != null && !canDig(world.getBlockState(digCeil))) continue;
                    digTargets = digCeil == null ? new BlockPos[]{pos, above} : new BlockPos[]{pos, above, digCeil};
                    cost = digCeil == null ? 6f : 8f;
                    type = 'f';
                } else {
                    //We found no valid action for this neighbor
                    continue;
                }

                if (type == 'd' || type == 'e' || type == 'f'){
                    if (canBridge(belowState)){
                        buildTargets = new BlockPos[]{below};
                        cost += 1.0f;
                    }
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
                    Node node = new Node(pos, current, moveCost, estCost, buildTargets, digTargets, type);
                    visited.put(pos, node);
                    open.add(node);
                }

            }
        }
        //No path found
        return null;
    }

    /**
     * Gets the position of the block that we will need to dig out in order to reach
     * the specified future position
     * @param dy the difference in y-levels between the future position and current position
     * @param current where we currently are (as defined by our pathfinder)
     * @param future where we are trying to go next (as defined by our pathfinder)
     * @return A BlockPosition containing the position of the block we need to dig to allow us to
     *         to reach the future position. Checks cases for stairs only. Returns null if no digging needed
     */
    private BlockPos digExtraCeiling(int dy, BlockPos current, BlockPos future){
        BlockPos pos = null;
        if (dy >= 1 && !walkable(world.getBlockState(current.above().above()))){
            pos = current.above().above();
        }else if (dy <= -1 && !walkable(world.getBlockState(future.above().above()))){
            pos = future.above().above();
        }
        return pos;
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


    private boolean blockExposed(BlockPos pos){
        BlockPos[] check = new BlockPos[]{
                pos.north(),
                pos.east(),
                pos.south(),
                pos.west(),
                pos.above(),
                pos.below()
        };
        for (BlockPos neighbor : check){
            BlockState state = world.getBlockState(neighbor);
            if (walkable(state)) return true;
        }
        return false;
    }


    private float manhattanDist(BlockPos pos1, BlockPos pos2){
        return Math.abs(pos1.getX() - pos2.getX()) + Math.abs(pos1.getY() - pos2.getY()) + Math.abs(pos1.getZ() - pos2.getZ());
    }


    public boolean walkable(BlockState state){
        return state.isAir() || state.canBeReplaced();
    }

    public boolean isSolid(BlockPos pos, BlockState state){
        return !state.isAir() && !state.getCollisionShape(world, pos).isEmpty();
    }

    public boolean canDig(BlockState state){
        return !blacklist.contains(state.getBlock()) && !state.isAir() && !liquid(state);
    }

    public boolean liquid(BlockState state){
        return state.is(Blocks.WATER) || state.is(Blocks.LAVA);
    }

    public boolean canBridge(BlockState state){
        return state.isAir() || state.canBeReplaced() || liquid(state);
    }

}
