package banduty.stoneycore.mixin;

import banduty.stoneycore.util.itemdata.SCTags;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Inject(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V",
            at = @At("HEAD"),
            cancellable = true)
    public void stoneycore$onRenderItem(LivingEntity entity, ItemStack item, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, World world, int light, int overlay, int seed, CallbackInfo ci) {
        if (item.isEmpty() || !item.isIn(SCTags.WEAPONS_3D.getTag())) {
            return;
        }

        if (renderMode == ModelTransformationMode.GUI || renderMode == ModelTransformationMode.GROUND) {
            return;
        }

        BakedModel bakedModel = getCustomBakedModel(item, entity, seed);
        if (bakedModel != null) {
            ItemRenderer itemRenderer = (ItemRenderer) (Object) this;
            itemRenderer.renderItem(item, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, bakedModel);
            ci.cancel();
        }
    }

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
            at = @At("HEAD"), cancellable = true)
    public void stoneycore$renderGUIItem(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        if (stack.isIn(SCTags.GEO_2D_ITEMS.getTag()) && renderMode == ModelTransformationMode.GUI) {
            BakedModel guiBakedModel = getCustomBakedModel(stack, null, 0);
            if (guiBakedModel != null) {
                matrices.translate(-0.5F, -0.5F, -0.5F);
                VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getCutout());
                renderBakedItemModel(guiBakedModel, light, overlay, matrices, vertexConsumer);
                ci.cancel();
            }
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
        return stack.getItem() + (stack.isIn(SCTags.GEO_2D_ITEMS.getTag()) ? "_icon" : "_3d");
    }

    @Unique
    private void renderBakedItemModel(BakedModel model, int light, int overlay, MatrixStack matrices, VertexConsumer vertices) {
        Random random = Random.create();
        for (Direction direction : Direction.values()) {
            random.setSeed(42L);
            renderBakedItemQuads(matrices, vertices, model.getQuads(null, direction, random), light, overlay);
        }

        random.setSeed(42L);
        renderBakedItemQuads(matrices, vertices, model.getQuads(null, null, random), light, overlay);
    }

    @Unique
    private void renderBakedItemQuads(MatrixStack matrices, VertexConsumer vertices, List<BakedQuad> quads, int light, int overlay) {
        MatrixStack.Entry entry = matrices.peek();
        for (BakedQuad bakedQuad : quads) {
            int color = -1;
            float r = (float) (color >> 16 & 255) / 255.0F;
            float g = (float) (color >> 8 & 255) / 255.0F;
            float b = (float) (color & 255) / 255.0F;
            vertices.quad(entry, bakedQuad, r, g, b, light, overlay);
        }
    }
}