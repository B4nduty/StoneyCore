package banduty.stoneycore.event.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

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

    void onRenderInFirstPerson(LocalPlayer player, ItemStack itemStack, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, HumanoidArm arm);
}