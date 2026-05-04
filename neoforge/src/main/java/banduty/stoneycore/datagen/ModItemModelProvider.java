package banduty.stoneycore.datagen;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.block.SCBlocks;
import banduty.stoneycore.items.SCItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, StoneyCore.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleItem(SCItems.SMITHING_HAMMER);
        simpleItem(SCItems.BLACK_POWDER);
        simpleItem(SCItems.CROWN);
        simpleItem(SCItems.MANUSCRIPT);

        createTongsModel();
        createHotIronModel();

        blockItem(SCBlocks.CRAFTMAN_ANVIL);
    }

    private void createTongsModel() {
        Item tongs = SCItems.TONGS;
        String tongsPath = BuiltInRegistries.ITEM.getKey(tongs).getPath();

        getBuilder(tongsPath + "_hotiron")
                .parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", modLoc("item/tongs_hotiron"));

        getBuilder(tongsPath + "_finished")
                .parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", modLoc("item/tongs_finished"));

        ItemModelBuilder tongsBuilder = getBuilder(tongsPath)
                .parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", modLoc("item/" + tongsPath));

        tongsBuilder.override()
                .predicate(ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "hotiron"), 1.0f)
                .model(getExistingFile(modLoc("item/tongs_hotiron")))
                .end();

        tongsBuilder.override()
                .predicate(ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "finished"), 1.0f)
                .model(getExistingFile(modLoc("item/tongs_finished")))
                .end();
    }

    private void createHotIronModel() {
        Item hotIron = SCItems.HOT_IRON;
        String hotIronPath = BuiltInRegistries.ITEM.getKey(hotIron).getPath();

        getBuilder(hotIronPath + "_finished")
                .parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", modLoc("item/hot_iron_finished"));

        getBuilder(hotIronPath)
                .parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", modLoc("item/" + hotIronPath))
                .override()
                .predicate(ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "finished"), 1.0f)
                .model(getExistingFile(modLoc("item/hot_iron_finished")))
                .end();
    }

    private void simpleItem(Item item) {
        String path = BuiltInRegistries.ITEM.getKey(item).getPath();
        withExistingParent(path, mcLoc("item/generated"))
                .texture("layer0", modLoc("item/" + path));
    }

    private void blockItem(Block block) {
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        withExistingParent(path, modLoc("block/" + path));
    }
}