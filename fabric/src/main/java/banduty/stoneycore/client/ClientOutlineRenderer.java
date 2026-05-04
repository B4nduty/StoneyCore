package banduty.stoneycore.client;

import banduty.stoneycore.StoneyCore;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientOutlineRenderer {
    private static final List<BlockPos> OUTLINE_POSITIONS = Collections.synchronizedList(new ArrayList<>());
    private static final BlockPos FIX_BLOCKPOS_OUTLINE = new BlockPos(0, 400, 0);

    public static void updatePositions(List<BlockPos> newPositions) {
        synchronized (OUTLINE_POSITIONS) {
            OUTLINE_POSITIONS.clear();
            OUTLINE_POSITIONS.addAll(newPositions);
            OUTLINE_POSITIONS.add(FIX_BLOCKPOS_OUTLINE);
        }
    }

    public static void register() {
        WorldRenderEvents.END.register(ctx -> {
            if (Minecraft.getInstance().player == null) return;

            int color = StoneyCore.getConfig().visualOptions().claimOutlineColor();
            float r = ((color >> 24) & 0xFF) / 255f;
            float g = ((color >> 16) & 0xFF) / 255f;
            float b = ((color >> 8) & 0xFF) / 255f;
            float a = (color & 0xFF) / 255f;

            synchronized (OUTLINE_POSITIONS) {
                for (BlockPos pos : OUTLINE_POSITIONS) {
                    DebugRenderer.renderFilledBox(
                            ctx.matrixStack(),
                            ctx.consumers(),
                            pos,
                            pos.offset(1, 1, 1),
                            r, g, b, pos.equals(FIX_BLOCKPOS_OUTLINE) ? 0 : a
                    );
                }
            }
        });
    }
}