package banduty.stoneycore.util.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public class SCRenderLayers extends RenderLayer {
    public static final Function<Identifier, RenderLayer> ARMOR_TRANSLUCENT_NO_CULL = Util.memoize(texture -> {
        var params = RenderLayer.MultiPhaseParameters.builder()
                .program(ARMOR_CUTOUT_NO_CULL_PROGRAM)
                .texture(new RenderPhase.Texture(texture, false, false))
                .transparency(TRANSLUCENT_TRANSPARENCY)
                .cull(DISABLE_CULLING)
                .lightmap(ENABLE_LIGHTMAP)
                .overlay(ENABLE_OVERLAY_COLOR)
                .layering(VIEW_OFFSET_Z_LAYERING)
                .build(true);
        return RenderLayer.of(
                "armor_translucent_no_cull", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                VertexFormat.DrawMode.QUADS, 256, true, true, params
        );
    });

    public SCRenderLayers(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
    }

    public static RenderLayer getArmorTranslucentNoCull(Identifier texture) {
        return ARMOR_TRANSLUCENT_NO_CULL.apply(texture);
    }
}