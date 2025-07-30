package banduty.stoneycore.datagen;

import banduty.stoneycore.items.SCItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {

    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(SCItems.SMITHING_HAMMER.get(), Models.HANDHELD);
        itemModelGenerator.register(SCItems.BLACK_POWDER.get(), Models.GENERATED);
        itemModelGenerator.register(SCItems.CROWN.get(), Models.GENERATED);
    }
}
