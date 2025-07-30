package banduty.stoneycore.structure;

import net.minecraft.block.Block;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

public abstract class StructureSpawner {

    public abstract String[][] getBaseAisles();

    public abstract void applyKeyMatcher(BlockPatternBuilder builder, Direction dir);

    public abstract Entity createEntity(World world);

    public abstract List<Block> getBlockFinders();
}
