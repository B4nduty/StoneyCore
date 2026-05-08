package banduty.stoneycore.datagen;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.block.SCBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, StoneyCore.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        horizontalBlock(SCBlocks.CRAFTMAN_ANVIL.get(),
                new ModelFile.UncheckedModelFile(modLoc("block/craftman_anvil")));
    }

    protected void facingLitBlock(Block block, String baseModelName, String litModelName) {
        DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
        BooleanProperty LIT = BlockStateProperties.LIT;

        ModelFile baseModel = models().getExistingFile(modLoc("block/" + baseModelName));
        ModelFile litModel = models().getExistingFile(modLoc("block/" + litModelName));

        getVariantBuilder(block).forAllStates(state -> {
            Direction dir = state.getValue(FACING);
            boolean lit = state.getValue(LIT);

            return ConfiguredModel.builder()
                    .modelFile(lit ? litModel : baseModel)
                    .rotationY(((int) dir.toYRot()) % 360)
                    .build();
        });

        simpleBlockItem(block, baseModel);
    }
}