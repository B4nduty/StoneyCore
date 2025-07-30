package banduty.stoneycore.networking.packet;

import banduty.stoneycore.StoneyCore;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class OutlineClaimS2CPacket {
    private static final List<BlockPos> OUTLINE_POSITIONS = new ArrayList<>();
    private static final BlockPos FIX_BLOCKPOS_OUTLINE = new BlockPos(0, 400, 0);

    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender) {
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
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) {
                synchronized (OUTLINE_POSITIONS) {
                    OUTLINE_POSITIONS.clear();
                }
                return;
            }

            float[] rgba = intToRgba(StoneyCore.getConfig().visualOptions.claimOutlineColor());

            MatrixStack matrices = ctx.matrixStack();
            VertexConsumerProvider consumers = ctx.consumers();
            for (BlockPos pos : OUTLINE_POSITIONS) {
                matrices.push();
                DebugRenderer.drawBox(
                        matrices,
                        consumers,
                        pos,
                        pos.add(1, 1, 1),
                        rgba[0], rgba[1], rgba[2], pos == FIX_BLOCKPOS_OUTLINE ? 0 : rgba[3]
                );
                matrices.pop();
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
