package banduty.stoneycore.structure;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;

import java.util.List;

public abstract class StructureSpawner {

    public abstract String[][] getBaseAisles();

    public abstract void applyKeyMatcher(BlockPatternBuilder builder, Direction dir);

    public abstract Entity createEntity(Level level);

    public abstract List<Block> getBlockFinders();
}
