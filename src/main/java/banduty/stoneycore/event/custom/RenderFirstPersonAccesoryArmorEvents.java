package banduty.stoneycore.event.custom;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;

public interface RenderFirstPersonAccesoryArmorEvents {
    Event<RenderFirstPersonAccesoryArmorEvents> EVENT = EventFactory.createArrayBacked(
            RenderFirstPersonAccesoryArmorEvents.class,
            listeners -> (player, itemStack, matrices,
                          vertexConsumers, light, arm) -> {
                for (RenderFirstPersonAccesoryArmorEvents listener : listeners) {
                    listener.onRenderInFirstPerson(player, itemStack, matrices, vertexConsumers, light, arm);
                }
            }
    );

    void onRenderInFirstPerson(ClientPlayerEntity player, ItemStack itemStack, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Arm arm);
}