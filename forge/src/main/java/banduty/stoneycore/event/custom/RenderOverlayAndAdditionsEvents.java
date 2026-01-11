package banduty.stoneycore.event.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class RenderOverlayAndAdditionsEvents extends Event {
    private final LivingEntity entity;
    private final ItemStack stack;
    private final PoseStack poseStack;
    private final MultiBufferSource multiBufferSource;
    private final int light;
    private final HumanoidModel<LivingEntity> model;

    public RenderOverlayAndAdditionsEvents(LivingEntity entity, ItemStack stack, PoseStack poseStack,
                                           MultiBufferSource multiBufferSource, int light,
                                           HumanoidModel<LivingEntity> model) {
        this.entity = entity;
        this.stack = stack;
        this.poseStack = poseStack;
        this.multiBufferSource = multiBufferSource;
        this.light = light;
        this.model = model;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public ItemStack getStack() {
        return stack;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public MultiBufferSource getMultiBufferSource() {
        return multiBufferSource;
    }

    public int getLight() {
        return light;
    }

    public HumanoidModel<LivingEntity> getModel() {
        return model;
    }
}