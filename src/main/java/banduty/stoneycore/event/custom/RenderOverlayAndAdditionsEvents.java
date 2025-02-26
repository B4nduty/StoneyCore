package banduty.stoneycore.event.custom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
public interface RenderOverlayAndAdditionsEvents {
    Event<RenderOverlayAndAdditionsEvents> EVENT = EventFactory.createArrayBacked(
            RenderOverlayAndAdditionsEvents.class,
            listeners -> (entity, stack, matrices,
                          vertexConsumers, light, model) -> {
                for (RenderOverlayAndAdditionsEvents listener : listeners) {
                    listener.onRenderOverlayAndAdditionsEvents(entity, stack, matrices, vertexConsumers,  light, model);
                }
            }
    );

    void onRenderOverlayAndAdditionsEvents(LivingEntity entity, ItemStack stack, MatrixStack matrices,
                                            VertexConsumerProvider vertexConsumers, int light,
                                            BipedEntityModel<LivingEntity> model);
}