package banduty.stoneycore.networking.packet;

import banduty.stoneycore.StoneyCore;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public class OutlineClaimS2CPacket {
    private static final List<BlockPos> OUTLINE_POSITIONS = new ArrayList<>();
    private static final BlockPos FIX_BLOCKPOS_OUTLINE = new BlockPos(0, 400, 0);

    public static void receive(Minecraft client, ClientPacketListener handler,
                               FriendlyByteBuf buf, PacketSender responseSender) {
        int count = buf.readInt();
        List<BlockPos> positions = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            positions.add(buf.readBlockPos());
        }

        positions.add(FIX_BLOCKPOS_OUTLINE);

        client.execute(() -> {
            synchronized (OUTLINE_POSITIONS) {
                OUTLINE_POSITIONS.clear();
                OUTLINE_POSITIONS.addAll(positions);
            }
        });
    }

    public static void registerRenderer() {
        WorldRenderEvents.END.register(ctx -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) {
                synchronized (OUTLINE_POSITIONS) {
                    OUTLINE_POSITIONS.clear();
                }
                return;
            }

            float[] rgba = intToRgba(StoneyCore.getConfig().visualOptions().claimOutlineColor());

            PoseStack poseStack = ctx.matrixStack();
            MultiBufferSource consumers = ctx.consumers();
            if (consumers == null) return;
            for (BlockPos pos : OUTLINE_POSITIONS) {
                poseStack.pushPose();
                DebugRenderer.renderFilledBox(
                        poseStack,
                        consumers,
                        pos,
                        pos.offset(1, 1, 1),
                        rgba[0], rgba[1], rgba[2], pos == FIX_BLOCKPOS_OUTLINE ? 0 : rgba[3]
                );
                poseStack.popPose();
            }
        });
    }

    public static float[] intToRgba(int color) {
        int r = (color >> 24) & 0xFF;
        int g = (color >> 16) & 0xFF;
        int b = (color >> 8) & 0xFF;
        int a = color & 0xFF;

        return new float[] {
                r / 255f,
                g / 255f,
                b / 255f,
                a / 255f
        };
    }
}
