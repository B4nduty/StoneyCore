package banduty.stoneycore.util.patterns;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.block.enums.StairShape;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import java.util.function.Predicate;

public class StructureMatcher {
    private final BlockPatternBuilder builder;
    private final Direction direction;

    public StructureMatcher(BlockPatternBuilder builder, Direction direction) {
        this.builder = builder;
        this.direction = direction;
    }

    // Basic block matchers
    public StructureMatcher match(char key, Block block) {
        builder.where(key, state -> state.getBlockState().isOf(block));
        return this;
    }

    public StructureMatcher match(char key, Predicate<BlockState> predicate) {
        builder.where(key, CachedBlockPosition.matchesBlockState(predicate));
        return this;
    }

    // Specialized matchers
    public StructureMatcher matchLog(char key, Block logBlock, Direction.Axis axis) {
        return match(key, state ->
                state.isOf(logBlock) &&
                        state.contains(Properties.AXIS) &&
                        state.get(Properties.AXIS) == axis
        );
    }

    public StructureMatcher matchTrapdoor(char key, Block trapdoorBlock, Direction facing) {
        return match(key, state ->
                state.isOf(trapdoorBlock) &&
                        state.contains(Properties.HORIZONTAL_FACING) &&
                        !state.get(Properties.WATERLOGGED) &&
                        !state.get(Properties.POWERED) &&
                        state.get(Properties.OPEN) &&
                        state.get(Properties.HORIZONTAL_FACING) == facing
        );
    }

    public StructureMatcher matchSlab(char key, Block slabBlock, SlabType slabType) {
        return match(key, state ->
                state.isOf(slabBlock) &&
                        state.get(Properties.SLAB_TYPE) == slabType &&
                        !state.get(Properties.WATERLOGGED)
        );
    }

    public StructureMatcher matchStairs(char key, Block stairsBlock, Direction facing) {
        return match(key, state ->
                state.isOf(stairsBlock) &&
                        state.contains(Properties.HORIZONTAL_FACING) &&
                        !state.get(Properties.WATERLOGGED) &&
                        state.get(Properties.BLOCK_HALF) == BlockHalf.BOTTOM &&
                        state.get(Properties.STAIR_SHAPE) == StairShape.STRAIGHT &&
                        state.get(Properties.HORIZONTAL_FACING) == facing
        );
    }

    // Direction-relative matchers
    public StructureMatcher matchTrapdoorRelative(char key, Block block, boolean opposite) {
        return matchTrapdoor(key, block, opposite ? direction.getOpposite() : direction);
    }

    public StructureMatcher matchStairsRelative(char key, Block block, boolean opposite) {
        return matchStairs(key, block, opposite ? direction.getOpposite() : direction);
    }

    public StructureMatcher matchStairsPerpendicular(char key, Block block, boolean clockwise) {
        Direction facing = switch (direction) {
            case NORTH -> clockwise ? Direction.EAST : Direction.WEST;
            case EAST -> clockwise ? Direction.SOUTH : Direction.NORTH;
            case SOUTH -> clockwise ? Direction.WEST : Direction.EAST;
            case WEST -> clockwise ? Direction.NORTH : Direction.SOUTH;
            default -> Direction.NORTH;
        };
        return matchStairs(key, block, facing);
    }
}