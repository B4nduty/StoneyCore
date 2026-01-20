package banduty.stoneycore.client;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.entity.custom.SCBulletEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class SCBulletEntityRenderer extends EntityRenderer<SCBulletEntity> {
    public SCBulletEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SCBulletEntity entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource multiBufferSource, int light) {
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(SCBulletEntity entity) {
        return new ResourceLocation(StoneyCore.MOD_ID, "textures/models/armor/a_layer_1.png");
    }
}