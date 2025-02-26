package banduty.stoneycore.mixin;

import banduty.stoneycore.items.armor.SCTrinketsItem;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity> {

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void stoneycore$onRenderLabelIfPresent(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (entity instanceof PlayerEntity player) {
            TrinketsApi.getTrinketComponent(player).ifPresent(trinketComponent ->
                    trinketComponent.getAllEquipped().forEach(pair -> {
                        ItemStack trinketStack = pair.getRight();
                        if (trinketStack.getItem() instanceof SCTrinketsItem scTrinketsItem && scTrinketsItem.unrenderCapeFeature()) ci.cancel();
            }));
        }
    }
}