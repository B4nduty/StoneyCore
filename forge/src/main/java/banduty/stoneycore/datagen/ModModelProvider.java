package banduty.stoneycore.datagen;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.SCItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModModelProvider extends ItemModelProvider {
    public ModModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, StoneyCore.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // Smithing Hammer (simple handheld)
        simpleItem(SCItems.SMITHING_HAMMER);

        // Simple items
        simpleItem(SCItems.BLACK_POWDER);
        simpleItem(SCItems.CROWN);
        simpleItem(SCItems.MANUSCRIPT);

        // Tongs with overrides
        createTongsModel();

        // Hot Iron with override
        createHotIronModel();
    }

    private void createTongsModel() {
        Item tongs = SCItems.TONGS.get();
        String tongsPath = ForgeRegistries.ITEMS.getKey(tongs).getPath();

        // Create variant models first
        // tongs_hotiron
        getBuilder(tongsPath + "_hotiron")
                .parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", modLoc("item/tongs_hotiron"));

        // tongs_finished
        getBuilder(tongsPath + "_finished")
                .parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", modLoc("item/tongs_finished"));

        // Main model with overrides
        ItemModelBuilder tongsBuilder = getBuilder(tongsPath)
                .parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", modLoc("item/" + tongsPath));

        // Add overrides
        tongsBuilder.override()
                .predicate(new ResourceLocation("hotiron"), 1.0f)
                .model(getExistingFile(modLoc("item/tongs_hotiron")))
                .end();

        tongsBuilder.override()
                .predicate(new ResourceLocation("finished"), 1.0f)
                .model(getExistingFile(modLoc("item/tongs_finished")))
                .end();
    }

    private void createHotIronModel() {
        Item hotIron = SCItems.HOT_IRON.get();
        String hotIronPath = ForgeRegistries.ITEMS.getKey(hotIron).getPath();

        // Create finished variant
        getBuilder(hotIronPath + "_finished")
                .parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", modLoc("item/hot_iron_finished"));

        // Main model with override
        getBuilder(hotIronPath)
                .parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", modLoc("item/" + hotIronPath))
                .override()
                .predicate(new ResourceLocation("finished"), 1.0f)
                .model(getExistingFile(modLoc("item/hot_iron_finished")))
                .end();
    }

    private void simpleItem(RegistryObject<Item> item) {
        withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(StoneyCore.MOD_ID, "item/" + item.getId().getPath()));
    }
}