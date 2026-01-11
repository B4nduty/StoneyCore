package banduty.stoneycore.util.render;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ForgeRenderTypeHelper implements SCRenderTypeHelper {
    @Override
    public RenderType getArmorTranslucentNoCull(ResourceLocation texture) {
        return ForgeSCRenderTypes.ARMOR_TRANSLUCENT_NO_CULL.apply(texture);
    }
}
