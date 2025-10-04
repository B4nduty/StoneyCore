package banduty.stoneycore.mixin;

import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.patterns.PatternHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MatrixUtil;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static net.minecraft.client.render.item.ItemRenderer.getDirectItemGlintConsumer;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Inject(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V",
            at = @At("HEAD"),
            cancellable = true)
    public void stoneycore$onRenderItem(LivingEntity entity, ItemStack itemStack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, World world, int light, int overlay, int seed, CallbackInfo ci) {
        boolean isValid = itemStack.isIn(SCTags.WEAPONS_3D.getTag())
                || !PatternHelper.getBannerPatterns(itemStack).isEmpty()
                || NBTDataHelper.get(itemStack, INBTKeys.DYE_COLOR_R, null) != null;

        if (itemStack.isEmpty() || !isValid) {
            return;
        }

        if (renderMode == ModelTransformationMode.GUI || renderMode == ModelTransformationMode.GROUND || renderMode == ModelTransformationMode.FIXED || itemStack.isIn(SCTags.GEO_2D_ITEMS.getTag())) {
            return;
        }

        BakedModel bakedModel = getCustomBakedModel(itemStack, entity, seed);
        if (bakedModel != null) {
            ItemRenderer itemRenderer = (ItemRenderer) (Object) this;
            itemRenderer.renderItem(itemStack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, bakedModel);
            ci.cancel();
        }
    }

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
            at = @At("HEAD"), cancellable = true)
    public void stoneycore$renderGUIItem(ItemStack itemStack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        BakedModel guiBakedModel = getCustomBakedModel(itemStack, MinecraftClient.getInstance().player, 0);
        if (itemStack.isIn(SCTags.GEO_2D_ITEMS.getTag())) {
            if ((renderMode == ModelTransformationMode.GUI || renderMode == ModelTransformationMode.GROUND) && model != null) {
                matrices.translate(-0.5F, -0.5F, -0.5F);
                VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getCutout());
                renderBakedItemModel(guiBakedModel, light, overlay, matrices, vertexConsumer, new float[]{1, 1, 1});
                ci.cancel();
            } else return;
        }

        if (!PatternHelper.getBannerPatterns(itemStack).isEmpty() || NBTDataHelper.get(itemStack, INBTKeys.DYE_COLOR_R, null) != null) {
            model.getTransformation().getTransformation(renderMode).apply(leftHanded, matrices);
            matrices.translate(-0.5F, -0.5F, -0.5F);
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getCutout());
            renderBakedItemModel(guiBakedModel, light, overlay, matrices, vertexConsumer, PatternHelper.getBannerDyeColor(itemStack));

            List<Pair<Identifier, DyeColor>> bannerPatterns = PatternHelper.getBannerPatterns(itemStack);
            if (!bannerPatterns.isEmpty()) {
                MinecraftClient client = MinecraftClient.getInstance();
                BakedModelManager modelManager = client.getItemRenderer().getModels().getModelManager();

                for (Pair<Identifier, DyeColor> patternPair : bannerPatterns) {
                    Identifier patternId = patternPair.getLeft();
                    DyeColor dyeColor = patternPair.getRight();

                    String[] pathParts = patternId.getPath().split("/");
                    String patternName = pathParts[pathParts.length - 1];
                    if (patternName.endsWith(".png")) {
                        patternName = patternName.substring(0, patternName.length() - 4);
                    }

                    String modelPath = itemStack.getItem() + "/" + patternName;

                    float[] rgb = dyeColor.getColorComponents();
                    ModelIdentifier modelId = new ModelIdentifier(
                            patternId.getNamespace(),
                            modelPath,
                            "inventory"
                    );

                    RenderLayer renderLayer = RenderLayers.getItemLayer(itemStack, true);
                    vertexConsumer = getDirectItemGlintConsumer(vertexConsumers, renderLayer, true, itemStack.hasGlint());
                    renderBakedItemModel(modelManager.getModel(modelId), light, overlay, matrices, vertexConsumer, rgb);
                }
            }
            MatrixStack.Entry entry = matrices.peek();
            if (renderMode == ModelTransformationMode.GUI) {
                MatrixUtil.scale(entry.getPositionMatrix(), 0.5F);
            } else if (renderMode.isFirstPerson()) {
                MatrixUtil.scale(entry.getPositionMatrix(), 0.75F);
            }
            ci.cancel();
        }
    }

    @Unique
    private BakedModel getCustomBakedModel(ItemStack itemStack, LivingEntity entity, int seed) {
        MinecraftClient client = MinecraftClient.getInstance();
        BakedModelManager modelManager = client.getItemRenderer().getModels().getModelManager();
        BakedModel bakedModel = modelManager.getMissingModel();

        String modelPath = determineModelPath(itemStack);
        if (!modelPath.isEmpty()) {
            bakedModel = modelManager.getModel(new ModelIdentifier(Registries.ITEM.getId(itemStack.getItem()).getNamespace(), modelPath, "inventory"));
        }

        if (entity != null) {
            ClientWorld clientWorld = entity.getWorld() instanceof ClientWorld ? (ClientWorld) entity.getWorld() : null;
            BakedModel overrideModel = bakedModel.getOverrides().apply(bakedModel, itemStack, clientWorld, entity, seed);
            if (overrideModel != null) {
                bakedModel = overrideModel;
            }
        }

        return bakedModel;
    }

    @Unique
    private String determineModelPath(ItemStack stack) {
        if (stack.isIn(SCTags.GEO_2D_ITEMS.getTag())) return stack.getItem() + "_icon";
        if (stack.isIn(SCTags.WEAPONS_3D.getTag())) return stack.getItem() + "_3d";
        return stack.getItem().toString();
    }

    @Unique
    private void renderBakedItemModel(BakedModel model, int light, int overlay, MatrixStack matrices, VertexConsumer vertices, float[] color) {
        Random random = Random.create();
        for (Direction direction : Direction.values()) {
            random.setSeed(42L);
            renderBakedItemQuads(matrices, vertices, model.getQuads(null, direction, random), light, overlay, color);
        }

        random.setSeed(42L);
        renderBakedItemQuads(matrices, vertices, model.getQuads(null, null, random), light, overlay, color);
    }

    @Unique
    private void renderBakedItemQuads(MatrixStack matrices, VertexConsumer vertices, List<BakedQuad> quads, int light, int overlay, float[] color) {
        MatrixStack.Entry entry = matrices.peek();
        for (BakedQuad bakedQuad : quads) {
            float r = color[0];
            float g = color[1];
            float b = color[2];
            vertices.quad(entry, bakedQuad, r, g, b, light, overlay);
        }
    }
}