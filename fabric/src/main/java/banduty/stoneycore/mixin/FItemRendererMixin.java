package banduty.stoneycore.mixin;

import banduty.stoneycore.items.manuscript.Manuscript;
import banduty.stoneycore.util.DyeUtil;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.patterns.PatternHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.MatrixUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static net.minecraft.client.renderer.entity.ItemRenderer.getFoilBufferDirect;

@Mixin(ItemRenderer.class)
public abstract class FItemRendererMixin {

    @Inject(method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V",
            at = @At("HEAD"),
            cancellable = true)
    public void stoneycore$onRenderItem(LivingEntity entity, ItemStack itemStack, ItemDisplayContext itemDisplayContext, boolean leftHanded, PoseStack poseStack, MultiBufferSource multiBufferSource, Level level, int light, int overlay, int seed, CallbackInfo ci) {
        BakedModel bakedModel = getCustomBakedModel(itemStack, entity, seed, null);
        if (itemStack.getItem() instanceof Manuscript && Manuscript.hasTargetStack(itemStack)) {
            String modelPath = "manuscript_" + Manuscript.getTargetItemPath(itemStack);
            bakedModel = getCustomBakedModel(itemStack, entity, seed,
                    new ModelResourceLocation(Manuscript.getTargetItemNamespace(itemStack), modelPath, "inventory"));
            if (bakedModel != null) {
                ItemRenderer itemRenderer = (ItemRenderer) (Object) this;
                itemRenderer.render(itemStack, itemDisplayContext, leftHanded, poseStack, multiBufferSource, light, overlay, bakedModel);
                ci.cancel();
                return;
            }
        }

        boolean isValid = itemStack.is(SCTags.WEAPONS_3D.getTag())
                || !PatternHelper.getBannerPatterns(itemStack).isEmpty()
                || NBTDataHelper.get(itemStack, INBTKeys.DYE_COLOR_R, null) != null;

        if (itemStack.isEmpty() || !isValid) {
            return;
        }

        if (itemDisplayContext == ItemDisplayContext.GUI || itemDisplayContext == ItemDisplayContext.FIXED || itemDisplayContext == ItemDisplayContext.GROUND || itemStack.is(SCTags.GEO_2D_ITEMS.getTag())) {
            return;
        }

        if (bakedModel != null) {
            ItemRenderer itemRenderer = (ItemRenderer) (Object) this;
            itemRenderer.render(itemStack, itemDisplayContext, leftHanded, poseStack, multiBufferSource, light, overlay, bakedModel);
            ci.cancel();
        }
    }

    @Inject(method = "render",
            at = @At("HEAD"), cancellable = true)
    public void stoneycore$renderGUIItem(ItemStack itemStack, ItemDisplayContext renderMode, boolean leftHanded, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, int overlay, BakedModel model, CallbackInfo ci) {
        BakedModel guiBakedModel = getCustomBakedModel(itemStack, Minecraft.getInstance().player, 0, null);

        if (itemStack.getItem() instanceof Manuscript && Manuscript.hasTargetStack(itemStack)) {
            String modelPath = "manuscript_" + Manuscript.getTargetItemPath(itemStack);
            guiBakedModel = getCustomBakedModel(itemStack, Minecraft.getInstance().player, 0,
                    new ModelResourceLocation(Manuscript.getTargetItemNamespace(itemStack), modelPath, "inventory"));
            if (guiBakedModel == null) return;
            guiBakedModel.getTransforms().getTransform(renderMode).apply(leftHanded, poseStack);

            poseStack.translate(-0.5F, -0.5F, -0.5F);

            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.cutout());
            renderBakedItemModel(guiBakedModel, light, overlay, poseStack, vertexConsumer, new float[]{1, 1, 1});
            ci.cancel();
            return;
        }

        if (itemStack.is(SCTags.GEO_2D_ITEMS.getTag())) {
            if ((renderMode == ItemDisplayContext.GUI || renderMode == ItemDisplayContext.GROUND) && model != null) {
                poseStack.translate(-0.5F, -0.5F, -0.5F);
                VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.cutout());
                renderBakedItemModel(guiBakedModel, light, overlay, poseStack, vertexConsumer, new float[]{1, 1, 1});
                ci.cancel();
            } else return;
        }

        if (!PatternHelper.getBannerPatterns(itemStack).isEmpty() || NBTDataHelper.get(itemStack, INBTKeys.DYE_COLOR_R, null) != null) {
            model.getTransforms().getTransform(renderMode).apply(leftHanded, poseStack);
            poseStack.translate(-0.5F, -0.5F, -0.5F);
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.cutout());
            renderBakedItemModel(guiBakedModel, light, overlay, poseStack, vertexConsumer, DyeUtil.getFloatDyeColor(itemStack));

            List<Tuple<ResourceLocation, DyeColor>> bannerPatterns = PatternHelper.getBannerPatterns(itemStack);
            if (!bannerPatterns.isEmpty()) {
                Minecraft client = Minecraft.getInstance();
                ModelManager modelManager = client.getItemRenderer().getItemModelShaper().getModelManager();

                for (Tuple<ResourceLocation, DyeColor> patternPair : bannerPatterns) {
                    ResourceLocation patternId = patternPair.getA();
                    DyeColor dyeColor = patternPair.getB();

                    String[] pathParts = patternId.getPath().split("/");
                    String patternName = pathParts[pathParts.length - 1];
                    if (patternName.endsWith(".png")) {
                        patternName = patternName.substring(0, patternName.length() - 4);
                    }

                    String modelPath = itemStack.getItem() + "/" + patternName;

                    float[] rgb = dyeColor.getTextureDiffuseColors();
                    ModelResourceLocation modelId = new ModelResourceLocation(patternId.getNamespace(), modelPath, "inventory");

                    RenderType renderLayer = ItemBlockRenderTypes.getRenderType(itemStack, true);
                    vertexConsumer = getFoilBufferDirect(multiBufferSource, renderLayer, true, itemStack.hasFoil());
                    renderBakedItemModel(modelManager.getModel(modelId), light, overlay, poseStack, vertexConsumer, rgb);
                }
            }
            PoseStack.Pose pose = poseStack.last();
            if (renderMode == ItemDisplayContext.GUI) {
                MatrixUtil.mulComponentWise(pose.pose(), 0.5F);
            } else if (renderMode.firstPerson()) {
                MatrixUtil.mulComponentWise(pose.pose(), 0.75F);
            }
            ci.cancel();
        }
    }

    @Unique
    private BakedModel getCustomBakedModel(ItemStack itemStack, LivingEntity entity, int seed, ModelResourceLocation newModelPath) {
        Minecraft minecraft = Minecraft.getInstance();
        ModelManager modelManager = minecraft.getItemRenderer().getItemModelShaper().getModelManager();
        BakedModel bakedModel = modelManager.getMissingModel();

        String modelPath = determineModelPath(itemStack);
        ModelResourceLocation iModelPath = new ModelResourceLocation(BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getNamespace(), modelPath, "inventory");
        if (newModelPath != null) iModelPath = newModelPath;
        if (!modelPath.isEmpty()) {
            bakedModel = modelManager.getModel(iModelPath);
        }

        if (entity != null && bakedModel != null) {
            ClientLevel clientLevel = entity.level() instanceof ClientLevel ? (ClientLevel) entity.level() : null;
            BakedModel overrideModel = bakedModel.getOverrides().resolve(bakedModel, itemStack, clientLevel, entity, seed);
            if (overrideModel != null) {
                bakedModel = overrideModel;
            }
        }

        return bakedModel;
    }

    @Unique
    private String determineModelPath(ItemStack stack) {
        if (stack.is(SCTags.GEO_2D_ITEMS.getTag())) return stack.getItem() + "_icon";
        if (stack.is(SCTags.WEAPONS_3D.getTag())) return stack.getItem() + "_3d";
        return stack.getItem().toString();
    }

    @Unique
    private void renderBakedItemModel(BakedModel model, int light, int overlay, PoseStack matrices, VertexConsumer vertices, float[] color) {
        RandomSource random = RandomSource.create();
        for (Direction direction : Direction.values()) {
            random.setSeed(42L);
            renderBakedItemQuads(matrices, vertices, model.getQuads(null, direction, random), light, overlay, color);
        }

        random.setSeed(42L);
        renderBakedItemQuads(matrices, vertices, model.getQuads(null, null, random), light, overlay, color);
    }

    @Unique
    private void renderBakedItemQuads(PoseStack poseStack, VertexConsumer vertices, List<BakedQuad> quads, int light, int overlay, float[] color) {
        PoseStack.Pose pose = poseStack.last();
        for (BakedQuad bakedQuad : quads) {
            float r = color[0];
            float g = color[1];
            float b = color[2];
            vertices.putBulkData(pose, bakedQuad, r, g, b, light, overlay);
        }
    }
}