package banduty.stoneycore.mixin;

import banduty.stoneycore.items.armor.SCTrinketsItem;
import banduty.stoneycore.items.armor.SCUnderArmorItem;
import banduty.stoneycore.items.armor.underarmor.SCDyeableUnderArmor;
import banduty.stoneycore.model.UnderArmourArmModel;
import banduty.stoneycore.util.DyeUtil;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
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
        ItemStack stack = player.getInventory().getArmorStack(2);
        if (stack.getItem() instanceof SCUnderArmorItem &&
                stack.getItem() instanceof ArmorItem armorItem && armorItem.getSlotType() == ArmorItem.Type.CHESTPLATE.getEquipmentSlot()) {
            UnderArmourArmModel model = new UnderArmourArmModel(UnderArmourArmModel.getTexturedModelData().createModel());
            VertexConsumer baseConsumer = vertexConsumers.getBuffer(
                    RenderLayer.getArmorCutoutNoCull(new Identifier(Registries.ITEM.getId(armorItem).getNamespace(), "textures/models/armor/" + armorItem.getMaterial().toString().toLowerCase() + ".png")));
            float[] color = new float[3];
            color[0] = 1;
            color[1] = 1;
            color[2] = 1;
            if (stack.getItem() instanceof SCDyeableUnderArmor) {
                color = DyeUtil.getDyeColor(stack);
            }

            Identifier textureOverlayPath = getOverlayIdentifier(stack.getItem());

            if (arm == Arm.RIGHT) model.render(matrices, baseConsumer, light, OverlayTexture.DEFAULT_UV, color[0], color[1], color[2], 1.0F);

            if (!textureOverlayPath.equals(new Identifier(""))) ArmorRenderer.renderPart(matrices, vertexConsumers, light, stack, model, textureOverlayPath);
        }

        if (TrinketsApi.getTrinketComponent(player).isPresent()) {
            for (Pair<SlotReference, ItemStack> equipped : TrinketsApi.getTrinketComponent(player).get().getEquipped(trinketStack -> trinketStack.getItem() instanceof SCTrinketsItem)) {
                ItemStack trinket = equipped.getRight();
                if (trinket.getItem() instanceof SCTrinketsItem scTrinketsItem && scTrinketsItem.getFirstPersonModel() != null) {
                    BipedEntityModel<LivingEntity> model = scTrinketsItem.getFirstPersonModel();
                    float[] color = new float[3];
                    color[0] = 1;
                    color[1] = 1;
                    color[2] = 1;
                    if (scTrinketsItem.isDyeable()) {
                        color = DyeUtil.getDyeColor(stack);
                    }
                    VertexConsumer baseConsumer = vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(scTrinketsItem.getTexturePath()));
                    if (arm == Arm.RIGHT) {
                        model.render(matrices, baseConsumer, light, OverlayTexture.DEFAULT_UV, color[0], color[1], color[2], 1.0F);
                        if (scTrinketsItem.isDyeableWithOverlay()) {
                            VertexConsumer dyeableConsumer = vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(getOverlayIdentifier(trinket.getItem())));
                            model.render(matrices, dyeableConsumer, light, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1.0F);
                        }
                    }
                }
            }
        }
    }

    @Unique
    private @NotNull Identifier getOverlayIdentifier(Item item) {
        Identifier originalIdentifier = null;
        if (item instanceof ArmorItem armorItem) originalIdentifier = new Identifier(Registries.ITEM.getId(armorItem).getNamespace(), "textures/models/armor/" + armorItem.getMaterial().toString().toLowerCase() + ".png");
        if (item instanceof SCTrinketsItem scTrinketsItem) originalIdentifier = scTrinketsItem.getTexturePath();

        String textureOverlayString = null;
        if (originalIdentifier != null) {
            textureOverlayString = originalIdentifier.getPath();
        }

        if (textureOverlayString != null && textureOverlayString.endsWith(".png")) {
            textureOverlayString = textureOverlayString.substring(0, textureOverlayString.length() - 4);
        }

        if (item instanceof SCTrinketsItem scTrinketsItem && scTrinketsItem.isDyeableWithOverlay()) textureOverlayString += "_overlay.png";
        else return new Identifier("");

        return new Identifier(originalIdentifier.getNamespace(), textureOverlayString);
    }
}