package banduty.stoneycore.mixin;

import banduty.stoneycore.event.custom.RenderFirstPersonAccesoryArmorEvents;
import banduty.stoneycore.items.armor.underarmor.SCDyeableUnderArmor;
import banduty.stoneycore.model.UnderArmourArmModel;
import banduty.stoneycore.util.DyeUtil;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsLoader;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    @Unique
    private final MinecraftClient client = MinecraftClient.getInstance();

    @Inject(method = "renderArm", at = @At("TAIL"))
    private void stoneycore$renderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Arm arm, CallbackInfo ci) {
        matrices.push();
        float f = arm == Arm.RIGHT ? 1.0F : -1.0F;
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(92.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(45.0F));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f * -41.0F));
        matrices.translate(f * 0.3F, -1.1F, 0.45F);
        modelLoader(matrices, vertexConsumers, light, arm);
        matrices.pop();
    }

    @Inject(method = "renderArmHoldingItem", at = @At("TAIL"))
    private void stoneycore$renderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm, CallbackInfo ci) {
        modelLoader(matrices, vertexConsumers, light, arm);
    }

    @Unique
    private void modelLoader(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Arm arm) {
        ClientPlayerEntity player = this.client.player;
        if (player == null) return;

        ItemStack chestStack = player.getInventory().getArmorStack(2); // Chestplate
        if (chestStack.getItem() instanceof ArmorItem armorItem &&
                ArmorDefinitionsLoader.containsItem(armorItem) &&
                armorItem.getSlotType() == ArmorItem.Type.CHESTPLATE.getEquipmentSlot()) {

            // Load your custom model
            UnderArmourArmModel model = new UnderArmourArmModel(UnderArmourArmModel.getTexturedModelData().createModel());

            // Get structureId for the base layer
            Identifier baseTexture = new Identifier(
                    Registries.ITEM.getId(armorItem).getNamespace(),
                    "textures/models/armor/" + armorItem.getMaterial().toString().toLowerCase() + ".png"
            );
            VertexConsumer baseConsumer = vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(baseTexture));

            // Handle color if it's dyeable
            float[] color = new float[] { 1f, 1f, 1f };
            if (chestStack.getItem() instanceof SCDyeableUnderArmor) {
                color = DyeUtil.getFloatDyeColor(chestStack);
            }

            // Render the arm part based on left/right
            PlayerEntityModel<?> playerModel = ((PlayerEntityModel<?>) ((LivingEntityRenderer<?, ?>)
                    MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(player)).getModel());

            float armOffset = 0.0625f; // This is 1px in model units

            matrices.push();
            if (arm == Arm.RIGHT) {
                matrices.translate(-armOffset, 0.0F, 0.0F);
                model.armorRightArm.copyTransform(playerModel.rightArm);
                model.armorRightArm.render(matrices, baseConsumer, light, OverlayTexture.DEFAULT_UV, color[0], color[1], color[2], 1.0F);
            } else {
                matrices.translate(armOffset, 0.0F, 0.0F);
                model.armorLeftArm.copyTransform(playerModel.leftArm);
                model.armorLeftArm.render(matrices, baseConsumer, light, OverlayTexture.DEFAULT_UV, color[0], color[1], color[2], 1.0F);
            }
            matrices.pop();

            // Render overlay if available
            Identifier overlayTexture = getOverlayIdentifier(armorItem);
            if (!overlayTexture.getPath().isEmpty()) {
                ArmorRenderer.renderPart(matrices, vertexConsumers, light, chestStack, model, overlayTexture);
            }
        }

        // Accessories rendering support
        AccessoriesCapability.getOptionally(player).ifPresent(accessories -> {
            for (SlotEntryReference equipped : accessories.getAllEquipped()) {
                ItemStack itemStack = equipped.stack();
                RenderFirstPersonAccesoryArmorEvents.EVENT.invoker().onRenderInFirstPerson(player, itemStack, matrices, vertexConsumers, light, arm);
            }
        });
    }

    @Unique
    private @NotNull Identifier getOverlayIdentifier(ArmorItem armorItem) {
        Identifier originalIdentifier = new Identifier(Registries.ITEM.getId(armorItem).getNamespace(), "textures/models/armor/" + armorItem.getMaterial().toString().toLowerCase() + ".png");

        String textureOverlayString = originalIdentifier.getPath();

        if (textureOverlayString != null && textureOverlayString.endsWith(".png") && armorItem instanceof DyeableArmorItem) {
            textureOverlayString = textureOverlayString.substring(0, textureOverlayString.length() - 4);
        }

        else return new Identifier("");

        return new Identifier(originalIdentifier.getNamespace(), textureOverlayString);
    }
}