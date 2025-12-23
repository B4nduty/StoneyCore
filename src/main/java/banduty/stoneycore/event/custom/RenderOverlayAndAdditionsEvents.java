package banduty.stoneycore.event.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

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

    void onRenderOverlayAndAdditionsEvents(LivingEntity entity, ItemStack stack, PoseStack poseStack,
                                           MultiBufferSource multiBufferSource, int light,
                                           HumanoidModel<LivingEntity> model);
}