package banduty.stoneycore.client;

import banduty.stoneycore.StoneyCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
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

    @SubscribeEvent
    public static void onWorldRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int color = StoneyCore.getConfig().visualOptions().claimOutlineColor();
        float r = ((color >> 24) & 0xFF) / 255f;
        float g = ((color >> 16) & 0xFF) / 255f;
        float b = ((color >> 8) & 0xFF) / 255f;
        float a = (color & 0xFF) / 255f;

        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

        synchronized (OUTLINE_POSITIONS) {
            for (BlockPos pos : OUTLINE_POSITIONS) {
                DebugRenderer.renderFilledBox(
                        event.getPoseStack(),
                        mc.renderBuffers().bufferSource(),
                        pos.getX() - cameraPos.x,
                        pos.getY() - cameraPos.y,
                        pos.getZ() - cameraPos.z,
                        pos.getX() + 1 - cameraPos.x,
                        pos.getY() + 1 - cameraPos.y,
                        pos.getZ() + 1 - cameraPos.z,
                        r, g, b, pos.equals(FIX_BLOCKPOS_OUTLINE) ? 0 : a
                );
            }
        }
    }
}