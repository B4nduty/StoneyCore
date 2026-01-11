package banduty.stoneycore.event.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class RenderFirstPersonAccesoryArmorEvents extends Event {
    private final LocalPlayer player;
    private final ItemStack itemStack;
    private final PoseStack poseStack;
    private final MultiBufferSource multiBufferSource;
    private final int light;
    private final HumanoidArm arm;

    public RenderFirstPersonAccesoryArmorEvents(LocalPlayer player, ItemStack itemStack, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, HumanoidArm arm) {
        this.player = player;
        this.itemStack = itemStack;
        this.poseStack = poseStack;
        this.multiBufferSource = multiBufferSource;
        this.light = light;
        this.arm = arm;
    }

    public LocalPlayer getPlayer() {
        return player;
    }

    public ItemStack getItemStack() {
        return itemStack;
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

    public HumanoidArm getArm() {
        return arm;
    }
}