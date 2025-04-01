package banduty.stoneycore.event.custom;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;

public interface RenderFirstPersonTrinketsArmorEvents {
    Event<RenderFirstPersonTrinketsArmorEvents> EVENT = EventFactory.createArrayBacked(
            RenderFirstPersonTrinketsArmorEvents.class,
            listeners -> (itemStack, matrices,
                          vertexConsumers, light, arm) -> {
                for (RenderFirstPersonTrinketsArmorEvents listener : listeners) {
                    listener.onRenderInFirstPerson(itemStack, matrices, vertexConsumers, light, arm);
                }
            }
    );

    void onRenderInFirstPerson(ItemStack itemStack, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Arm arm);
}