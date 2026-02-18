package banduty.stoneycore.datagen;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.block.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, StoneyCore.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        horizontalBlock(ModBlocks.CRAFTMAN_ANVIL.get(),
                new ModelFile.UncheckedModelFile(modLoc("block/craftman_anvil")));

        /*simpleBlock(ModBlocks.WATER_BARREL.get(),
                new ModelFile.UncheckedModelFile(modLoc("block/water_barrel")));*/
    }

    private void blockWithItem(RegistryObject<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
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


    private void blockWithItemDirectional(RegistryObject<Block> blockRegistryObject) {
    }
}