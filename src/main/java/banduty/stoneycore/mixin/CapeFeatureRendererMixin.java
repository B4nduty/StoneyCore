package banduty.stoneycore.mixin;

import banduty.stoneycore.items.armor.SCTrinketsItem;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeFeatureRenderer.class)
public class CapeFeatureRendererMixin {
    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/network/AbstractClientPlayerEntity;FFFFFF)V", at = @At("HEAD"), cancellable = true)
    public void stoneycore$render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        TrinketsApi.getTrinketComponent(abstractClientPlayerEntity).ifPresent(trinketComponent ->
                trinketComponent.getAllEquipped().forEach(pair -> {
                    ItemStack trinketStack = pair.getRight();
                    if (trinketStack.getItem() instanceof SCTrinketsItem scTrinketsItem && scTrinketsItem.unrenderCapeFeature()) ci.cancel();
                }));
    }
}