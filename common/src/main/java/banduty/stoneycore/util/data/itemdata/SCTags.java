package banduty.stoneycore.util.data.itemdata;

import banduty.stoneycore.StoneyCore;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public enum SCTags {

    // Weapon Tags
    /**
     * Only Fabric
     * <p>
     * If you want to have your Weapon 3D
     * <p>
     * You will need to make another model file with the name "your_item_3d.json",
     * <p>
     * Mixin {@link ModelBakery} and Inject in method ModelBakery#init:
     * <pre>{@code
     * @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/ModelLoader;addModel(Lnet/minecraft/client/util/ModelIdentifier;)V", ordinal = 3, shift = At.Shift.AFTER))
     * public void mod_id$add3dModels(BlockColors blockColors, Profiler profiler, Map<Identifier, JsonUnbakedModel> jsonUnbakedModels, Map<Identifier, List<ModelLoader.SourceTrackedData>> blockStates, CallbackInfo ci) {
     *     String[] modelNames = {
     *         "your_item_3d",
     *         // Add other model names here
     *     };
     *
     *     for (String modelName : modelNames) {
     *         this.addModel(new ModelIdentifier(yourModId, modelName, "inventory"));
     *     }
     * }
     * }</pre>
     */
    WEAPONS_3D("weapons_3d"),
    /**
     * If you want to have your Weapon act as a Shield
     * <p>
     * If you want to change the position of the Weapon,
     * <p>
     * you will need to add in your 3D Model file the predicate "blocking"
     * <p>
     * It's compatible with "bludgeoning" predicate
     */
    WEAPONS_SHIELD("weapons_shield"),
    /**
     * If you want to have your Weapon deals x2 of its original baseDamage when hit from Behind
     */
    WEAPONS_DAMAGE_BEHIND("weapons_damage_behind"),
    /**
     * If you want to have your Weapon ignore Armor
     */
    WEAPONS_IGNORES_ARMOR("weapons_ignores_armor"),
    /**
     * If you want your Weapon disable the Shield/Parrying of another Weapon as Axes with the Shield
     */
    WEAPONS_DISABLE_SHIELD("weapons_disable_shield"),
    /**
     * If you want your Weapon bypass Shield/Parrying
     */
    WEAPONS_BYPASS_BLOCK("weapons_bypass_block"),
    /**
     * If you want your Weapon can Harvest
     */
    WEAPONS_HARVEST("weapons_harvest"),
    /**
     * If you want your Geo Weapon (Geckolib Weapon) has a 2D Texture in HUD
     * <p>
     * You will need to make another model file with the name "your_item_icon.json"
     * <p>
     * Mixin {@link ModelBakery} and Inject in method ModelBakery#init:
     * <pre>{@code
     * @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/ModelLoader;addModel(Lnet/minecraft/client/util/ModelIdentifier;)V", ordinal = 3, shift = At.Shift.AFTER))
     * public void mod_id$add3dModels(BlockColors blockColors, Profiler profiler, Map<Identifier, JsonUnbakedModel> jsonUnbakedModels, Map<Identifier, List<ModelLoader.SourceTrackedData>> blockStates, CallbackInfo ci) {
     *     String[] modelNames = {
     *         "your_item_icon",
     *         // Add other model names here
     *     };
     *
     *     for (String modelName : modelNames) {
     *         this.addModel(new ModelIdentifier("yourWeaponModId", modelName, "inventory"));
     *     }
     * }
     * }</pre>
     */
    GEO_2D_ITEMS("geo_2d_items"),
    BROKEN_WEAPONS("broken_weapons"),

    // Armor Tags
    /**
     * Armor that can Hide Name Tag
     */
    HIDE_NAME_TAG("hide_name_tag"),
    /**
     * Armor that are banner compatible
     */
    BANNER_COMPATIBLE("banner_compatible");

    private final TagKey<Item> tag;

    SCTags(String name) {
        this.tag = TagKey.create(Registries.ITEM, new ResourceLocation(StoneyCore.MOD_ID, name));
    }

    public TagKey<Item> getTag() {
        return tag;
    }
}