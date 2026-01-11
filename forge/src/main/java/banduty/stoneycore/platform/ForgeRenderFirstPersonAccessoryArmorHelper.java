package banduty.stoneycore.platform;

import banduty.stoneycore.event.custom.RenderFirstPersonAccesoryArmorEvents;
import banduty.stoneycore.platform.services.RenderFirstPersonAccessoryArmorHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

public class ForgeRenderFirstPersonAccessoryArmorHelper implements RenderFirstPersonAccessoryArmorHelper {
    @Override
    public void onRenderInFirstPerson(LocalPlayer player, ItemStack itemStack, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, HumanoidArm arm) {
        MinecraftForge.EVENT_BUS.post(new RenderFirstPersonAccesoryArmorEvents(player, itemStack, poseStack, multiBufferSource, light, arm));
    }
}
