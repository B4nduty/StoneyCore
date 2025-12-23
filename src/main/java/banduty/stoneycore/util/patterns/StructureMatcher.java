package banduty.stoneycore.util.patterns;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;

import java.util.function.Predicate;

public record StructureMatcher(BlockPatternBuilder builder, Direction direction) {

    // Basic block matchers
    public StructureMatcher match(char key, Block block) {
        builder.where(key, state -> state.getState().is(block));
        return this;
    }

    public StructureMatcher match(char key, Predicate<BlockState> predicate) {
        builder.where(key, BlockInWorld.hasState(predicate));
        return this;
    }

    // Specialized matchers
    public StructureMatcher matchLog(char key, Block logBlock, Direction.Axis axis) {
        return match(key, state ->
                state.is(logBlock) &&
                        state.hasProperty(BlockStateProperties.AXIS) &&
                        state.getValue(BlockStateProperties.AXIS) == axis
        );
    }

    public StructureMatcher matchTrapdoor(char key, Block trapdoorBlock, Direction facing) {
        return match(key, state ->
                state.is(trapdoorBlock) &&
                        state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) &&
                        !state.getValue(BlockStateProperties.WATERLOGGED) &&
                        !state.getValue(BlockStateProperties.POWERED) &&
                        state.getValue(BlockStateProperties.OPEN) &&
                        state.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing
        );
    }

    public StructureMatcher matchSlab(char key, Block slabBlock, SlabType slabType) {
        return match(key, state ->
                state.is(slabBlock) &&
                        state.getValue(BlockStateProperties.SLAB_TYPE) == slabType &&
                        !state.getValue(BlockStateProperties.WATERLOGGED)
        );
    }

    public StructureMatcher matchStairs(char key, Block stairsBlock, Direction facing) {
        return match(key, state ->
                state.is(stairsBlock) &&
                        state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) &&
                        !state.getValue(BlockStateProperties.WATERLOGGED) &&
                        state.getValue(BlockStateProperties.HALF) == Half.BOTTOM &&
                        state.getValue(BlockStateProperties.STAIRS_SHAPE) == StairsShape.STRAIGHT &&
                        state.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing
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