package banduty.stoneycore.util.patterns;

import banduty.stoneycore.structure.StructureSpawnRegistry;
import banduty.stoneycore.structure.StructureSpawner;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;

public class StructureHelper {
    @Unique
    private static Map<Block, List<StructureSpawner>> blockToSpawners = null;

    @Unique
    private static final Map<StructureSpawner, EnumMap<Direction, BlockPattern>> patternCache = new IdentityHashMap<>();

    public static void trySpawnEntity(Level level, BlockPos pos, BlockState state) {
        if (state == null) return;

        if (blockToSpawners == null) {
            blockToSpawners = new HashMap<>();
            for (StructureSpawner spawner : StructureSpawnRegistry.getAll()) {
                for (Block block : spawner.getBlockFinders()) {
                    blockToSpawners.computeIfAbsent(block, b -> new ArrayList<>()).add(spawner);
                }
            }
        }

        Block block = state.getBlock();
        List<StructureSpawner> spawners = blockToSpawners.get(block);
        if (spawners == null || spawners.isEmpty()) return;

        for (StructureSpawner spawner : spawners) {
            String[][] baseAisle = spawner.getBaseAisles();

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPattern pattern = getPattern(spawner, baseAisle, dir);
                if (pattern == null) continue;

                MatchResult match = searchAround(level, pos, pattern, dir);
                if (match != null) {
                    BlockPattern.BlockPatternMatch result = match.result();

                    Entity entity = spawner.createEntity(level);
                    if (entity != null) {
                        spawnEntity(level, result, entity);
                        return;
                    }
                }
            }
        }
    }

    public static @Nullable MatchResult searchAround(LevelReader levelReader, BlockPos pos, BlockPattern pattern, Direction forward) {
        int i = Math.max(Math.max(pattern.getWidth(), pattern.getHeight()), pattern.getDepth());
        BlockPos start = pos.subtract(new BlockPos(i, i, i));
        BlockPos end = pos.offset(i, i, i);

        for (BlockPos blockPos : BlockPos.betweenClosed(start, end)) {
            for (Direction up : Direction.Plane.VERTICAL) {
                if (up == forward || up == forward.getOpposite()) continue;
                BlockPattern.BlockPatternMatch result = pattern.matches(levelReader, blockPos, forward, up);
                if (result != null) {
                    return new MatchResult(result, forward);
                }
            }
        }

        return null;
    }

    private static void spawnEntity(Level level, BlockPattern.BlockPatternMatch patternResult, Entity entity) {
        breakPatternBlocks(level, patternResult);

        // Calculate center of pattern
        int centerX = patternResult.getWidth() / 2;
        int centerY = patternResult.getHeight() / 2;
        int centerZ = patternResult.getDepth() / 2;

        BlockPos centerPos = patternResult.getBlock(centerX, centerY, centerZ).getPos();
        entity.setPos(centerPos.getX() + 0.5, centerPos.above().getY() + 0.5, centerPos.getZ() + 0.5);

        level.addFreshEntity(entity);

        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, entity.getBoundingBox().inflate(5.0F))) {
            CriteriaTriggers.SUMMONED_ENTITY.trigger(player, entity);
        }

        level.updateNeighborsAt(centerPos, Blocks.AIR);
    }

    private static void breakPatternBlocks(Level level, BlockPattern.BlockPatternMatch result) {
        for (int i = 0; i < result.getWidth(); i++) {
            for (int j = 0; j < result.getHeight(); j++) {
                for (int k = 0; k < result.getDepth(); k++) {
                    BlockInWorld pos = result.getBlock(i, j, k);
                    level.setBlock(pos.getPos(), Blocks.AIR.defaultBlockState(), 2);
                    level.levelEvent(2001, pos.getPos(), Block.getId(pos.getState()));
                }
            }
        }
    }

    private static BlockPattern getPattern(StructureSpawner spawner, String[][] baseAisle, Direction dir) {
        EnumMap<Direction, BlockPattern> patterns = patternCache.computeIfAbsent(spawner, k -> new EnumMap<>(Direction.class));
        if (patterns.containsKey(dir)) return patterns.get(dir);

        String[][] rotated;
        switch (dir) {
            case SOUTH -> rotated = baseAisle;
            case WEST -> rotated = rotateAislesY90(baseAisle);
            case NORTH -> rotated = rotateAislesY90(rotateAislesY90(baseAisle));
            case EAST -> rotated = rotateAislesY90(rotateAislesY90(rotateAislesY90(baseAisle)));
            default -> { rotated = baseAisle; }
        }

        BlockPatternBuilder builder = BlockPatternBuilder.start();
        for (String[] layer : rotated) {
            builder.aisle(layer);
        }

        spawner.applyKeyMatcher(builder, dir);
        BlockPattern pattern = builder.build();
        patterns.put(dir, pattern);
        return pattern;
    }

    private static String[][] rotateAislesY90(String[][] layers) {
        int depth = layers.length;
        int height = layers[0].length;
        int width = layers[0][0].length();

        String[][] rotated = new String[depth][height];

        for (int z = 0; z < depth; z++) {
            String[] newLayer = new String[height];
            for (int y = 0; y < height; y++) {
                StringBuilder newRow = new StringBuilder();
                for (int x = 0; x < width; x++) {
                    newRow.append(layers[z][height - 1 - y].charAt(x));
                }
                newLayer[y] = newRow.reverse().toString();
            }
            rotated[z] = newLayer;
        }

        return rotated;
    }

    public record MatchResult(BlockPattern.BlockPatternMatch result, Direction forward) {}
}
