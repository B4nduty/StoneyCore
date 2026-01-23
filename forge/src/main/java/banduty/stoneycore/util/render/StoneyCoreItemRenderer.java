package banduty.stoneycore.util.render;

import banduty.stoneycore.items.manuscript.Manuscript;
import banduty.stoneycore.util.DyeUtil;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.patterns.PatternHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class StoneyCoreItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final StoneyCoreItemRenderer INSTANCE = new StoneyCoreItemRenderer();

    public StoneyCoreItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();

        BakedModel model = getCustomModel(stack, mc);

        if (stack.is(SCTags.GEO_2D_ITEMS.getTag())) {
            if (context == ItemDisplayContext.GUI || context == ItemDisplayContext.GROUND || context == ItemDisplayContext.FIXED) {
                ResourceLocation baseId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                model = mc.getModelManager().getModel(new ModelResourceLocation(baseId.getNamespace(), baseId.getPath() + "_icon", "inventory"));
            }
        }

        poseStack.pushPose();

        model = model.applyTransform(context, poseStack, false);

        poseStack.translate(-0.5F, -0.5F, -0.5F);

        float[] color = {1.0f, 1.0f, 1.0f};
        if (NBTDataHelper.get(stack, INBTKeys.DYE_COLOR_R, null) != null) {
            color = DyeUtil.getFloatDyeColor(stack);
        }

        renderBakedModel(model, light, overlay, poseStack, buffer.getBuffer(ItemBlockRenderTypes.getRenderType(stack, true)), color);

        renderBannerPatterns(stack, poseStack, buffer, light, overlay);

        poseStack.popPose();
    }

    private BakedModel getCustomModel(ItemStack stack, Minecraft mc) {
        if (stack.getItem() instanceof Manuscript && Manuscript.hasTargetStack(stack)) {
            String modelPath = "manuscript_" + Manuscript.getTargetItemPath(stack);
            return mc.getModelManager().getModel(new ModelResourceLocation(
                    Manuscript.getTargetItemNamespace(stack), modelPath, "inventory"));
        }

        if (stack.is(SCTags.WEAPONS_3D.getTag())) {
            return mc.getModelManager().getModel(new ModelResourceLocation(
                    BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace(),
                    stack.getItem() + "_3d", "inventory"));
        }

        return mc.getItemRenderer().getModel(stack, null, null, 0);
    }

    private void renderBakedModel(BakedModel model, int light, int overlay, PoseStack poseStack, VertexConsumer vertices, float[] color) {
        for (Direction direction : Direction.values()) {
            renderQuads(poseStack, vertices, model.getQuads(null, direction, RandomSource.create(42L)), light, overlay, color);
        }
        renderQuads(poseStack, vertices, model.getQuads(null, null, RandomSource.create(42L)), light, overlay, color);
    }

    private void renderQuads(PoseStack poseStack, VertexConsumer vertices, List<BakedQuad> quads, int light, int overlay, float[] color) {
        PoseStack.Pose pose = poseStack.last();
        for (BakedQuad quad : quads) {
            vertices.putBulkData(pose, quad, color[0], color[1], color[2], light, overlay);
        }
    }

    private void renderBannerPatterns(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        List<Tuple<ResourceLocation, DyeColor>> bannerPatterns = PatternHelper.getBannerPatterns(stack);

        if (!bannerPatterns.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            ModelManager modelManager = mc.getItemRenderer().getItemModelShaper().getModelManager();

            RenderType renderType = ItemBlockRenderTypes.getRenderType(stack, true);
            VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(
                    buffer,
                    renderType,
                    true,
                    stack.hasFoil()
            );

            for (Tuple<ResourceLocation, DyeColor> patternPair : bannerPatterns) {
                ResourceLocation patternId = patternPair.getA();
                DyeColor dyeColor = patternPair.getB();

                String path = patternId.getPath();
                String patternName = path.substring(path.lastIndexOf('/') + 1);
                if (patternName.endsWith(".png")) {
                    patternName = patternName.substring(0, patternName.length() - 4);
                }

                String modelPath = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + "/" + patternName;
                ModelResourceLocation modelId = new ModelResourceLocation(patternId.getNamespace(), modelPath, "inventory");

                BakedModel patternModel = modelManager.getModel(modelId);
                float[] rgb = dyeColor.getTextureDiffuseColors();

                if (patternModel != modelManager.getMissingModel()) {
                    renderBakedModel(patternModel, light, overlay, poseStack, vertexConsumer, rgb);
                }
            }
        }
    }
}