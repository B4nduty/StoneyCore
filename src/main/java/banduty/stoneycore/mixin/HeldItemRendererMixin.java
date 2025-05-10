package banduty.stoneycore.mixin;

import banduty.stoneycore.event.custom.RenderFirstPersonTrinketsArmorEvents;
import banduty.stoneycore.items.armor.underarmor.SCDyeableUnderArmor;
import banduty.stoneycore.model.UnderArmourArmModel;
import banduty.stoneycore.util.DyeUtil;
import banduty.stoneycore.util.definitionsloader.SCArmorDefinitionsLoader;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketItem;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.DyeableArmorItem;
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
        if (stack.getItem() instanceof ArmorItem armorItem && SCArmorDefinitionsLoader.containsItem(armorItem)
                && armorItem.getSlotType() == ArmorItem.Type.CHESTPLATE.getEquipmentSlot()) {
            UnderArmourArmModel model = new UnderArmourArmModel(UnderArmourArmModel.getTexturedModelData().createModel());
            VertexConsumer baseConsumer = vertexConsumers.getBuffer(
                    RenderLayer.getArmorCutoutNoCull(new Identifier(Registries.ITEM.getId(armorItem).getNamespace(), "textures/models/armor/" + armorItem.getMaterial().toString().toLowerCase() + ".png")));
            float[] color = new float[3];
            color[0] = 1;
            color[1] = 1;
            color[2] = 1;
            if (stack.getItem() instanceof SCDyeableUnderArmor) {
                color = DyeUtil.getFloatDyeColor(stack);
            }

            Identifier textureOverlayPath = getOverlayIdentifier(armorItem);

            if (arm == Arm.RIGHT) model.armorRightArm.render(matrices, baseConsumer, light, OverlayTexture.DEFAULT_UV, color[0], color[1], color[2], 1.0F);
            if (arm == Arm.LEFT) model.armorLeftArm.render(matrices, baseConsumer, light, OverlayTexture.DEFAULT_UV, color[0], color[1], color[2], 1.0F);

            if (!textureOverlayPath.equals(new Identifier(""))) ArmorRenderer.renderPart(matrices, vertexConsumers, light, stack, model, textureOverlayPath);
        }

        if (TrinketsApi.getTrinketComponent(player).isPresent()) {
            for (Pair<SlotReference, ItemStack> equipped : TrinketsApi.getTrinketComponent(player).get().getEquipped(trinketStack -> trinketStack.getItem() instanceof TrinketItem)) {
                ItemStack trinket = equipped.getRight();
                RenderFirstPersonTrinketsArmorEvents.EVENT.invoker().onRenderInFirstPerson(trinket, matrices, vertexConsumers, light, arm);
            }
        }
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