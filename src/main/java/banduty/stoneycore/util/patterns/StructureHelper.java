package banduty.stoneycore.util.patterns;

import banduty.stoneycore.structure.StructureSpawnRegistry;
import banduty.stoneycore.structure.StructureSpawner;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;

public class StructureHelper {
    @Unique
    private static Map<Block, List<StructureSpawner>> blockToSpawners = null;

    @Unique
    private static final Map<StructureSpawner, EnumMap<Direction, BlockPattern>> patternCache = new IdentityHashMap<>();

    public static void trySpawnEntity(World world, BlockPos pos, BlockState state) {
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

            for (Direction dir : Direction.Type.HORIZONTAL) {
                BlockPattern pattern = getPattern(spawner, baseAisle, dir);
                if (pattern == null) continue;

                MatchResult match = searchAround(world, pos, pattern, dir);
                if (match != null) {
                    BlockPattern.Result result = match.result();

                    Entity entity = spawner.createEntity(world);
                    if (entity != null) {
                        spawnEntity(world, result, entity);
                        return;
                    }
                }
            }
        }
    }

    public static @Nullable MatchResult searchAround(WorldView world, BlockPos pos, BlockPattern pattern, Direction forward) {
        int i = Math.max(Math.max(pattern.getWidth(), pattern.getHeight()), pattern.getDepth());
        BlockPos start = pos.subtract(new BlockPos(i, i, i));
        BlockPos end = pos.add(i, i, i);

        for (BlockPos blockPos : BlockPos.iterate(start, end)) {
            for (Direction up : Direction.Type.VERTICAL) {
                if (up == forward || up == forward.getOpposite()) continue;
                BlockPattern.Result result = pattern.testTransform(world, blockPos, forward, up);
                if (result != null) {
                    return new MatchResult(result, forward);
                }
            }
        }

        return null;
    }

    private static void spawnEntity(World world, BlockPattern.Result patternResult, Entity entity) {
        breakPatternBlocks(world, patternResult);

        // Calculate center of pattern
        int centerX = patternResult.getWidth() / 2;
        int centerY = patternResult.getHeight() / 2;
        int centerZ = patternResult.getDepth() / 2;

        BlockPos centerPos = patternResult.translate(centerX, centerY, centerZ).getBlockPos();
        entity.setPosition(centerPos.getX() + 0.5, centerPos.up().getY() + 0.5, centerPos.getZ() + 0.5);

        world.spawnEntity(entity);

        for (ServerPlayerEntity player : world.getNonSpectatingEntities(ServerPlayerEntity.class, entity.getBoundingBox().expand(5.0F))) {
            Criteria.SUMMONED_ENTITY.trigger(player, entity);
        }

        world.updateNeighbors(centerPos, Blocks.AIR);
    }

    private static void breakPatternBlocks(World world, BlockPattern.Result result) {
        for (int i = 0; i < result.getWidth(); i++) {
            for (int j = 0; j < result.getHeight(); j++) {
                for (int k = 0; k < result.getDepth(); k++) {
                    CachedBlockPosition pos = result.translate(i, j, k);
                    world.setBlockState(pos.getBlockPos(), Blocks.AIR.getDefaultState(), 2);
                    world.syncWorldEvent(2001, pos.getBlockPos(), Block.getRawIdFromState(pos.getBlockState()));
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

    public record MatchResult(BlockPattern.Result result, Direction forward) {}
}
