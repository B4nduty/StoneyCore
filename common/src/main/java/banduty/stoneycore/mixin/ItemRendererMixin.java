package banduty.stoneycore.mixin;

import banduty.stoneycore.items.custom.manuscript.Manuscript;
import com.llamalad7.mixinextras.sugar.Local;
import banduty.stoneycore.util.data.itemdata.SCTags;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Shadow
    @Final
    private ItemModelShaper itemModelShaper;

    @Shadow
    public abstract ItemModelShaper getItemModelShaper();

    @ModifyVariable(
            method = "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V",
            at = @At(value = "HEAD"),
            argsOnly = true
    )
    public BakedModel stoneycore$renderItem(BakedModel bakedModel, @Local(argsOnly = true) ItemStack stack, @Local(argsOnly = true) ItemDisplayContext renderMode) {
        if (stack.getItem() instanceof Manuscript && Manuscript.hasTargetStack(stack)) {
            String modelPath = "manuscript_" + Manuscript.getTargetItemPath(stack);
            ModelResourceLocation manuscriptModelId = ModelResourceLocation.inventory(
                    ResourceLocation.fromNamespaceAndPath(Manuscript.getTargetItemNamespace(stack), modelPath));
            return getItemModelShaper().getModelManager().getModel(manuscriptModelId);
        }

        if (stack.is(SCTags.GEO_2D_ITEMS.getTag())) {
            if (renderMode == ItemDisplayContext.GUI || renderMode == ItemDisplayContext.GROUND || renderMode == ItemDisplayContext.FIXED) {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                ModelResourceLocation flatModelId = ModelResourceLocation.inventory(
                        ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), itemId.getPath() + "_icon"));
                return getItemModelShaper().getModelManager().getModel(flatModelId);
            }
        }

        if (stack.is(SCTags.WEAPONS_3D.getTag())) {
            if (renderMode == ItemDisplayContext.GUI || renderMode == ItemDisplayContext.GROUND || renderMode == ItemDisplayContext.FIXED) {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                ModelResourceLocation model3dId = ModelResourceLocation.inventory(
                        ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), itemId.getPath()));
                return getItemModelShaper().getModelManager().getModel(model3dId);
            }
        }

        return bakedModel;
    }

    @ModifyVariable(
            method = "getModel",
            at = @At(value = "STORE"),
            ordinal = 1
    )
    public BakedModel getHeldItemModelMixin(BakedModel bakedModel, @Local(argsOnly = true) ItemStack stack) {
        if (stack.is(SCTags.WEAPONS_3D.getTag())) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            ModelResourceLocation model3dId = ModelResourceLocation.inventory(
                    ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), itemId.getPath() + "_3d"));
            return this.itemModelShaper.getModelManager().getModel(model3dId);
        }

        return bakedModel;
    }
}