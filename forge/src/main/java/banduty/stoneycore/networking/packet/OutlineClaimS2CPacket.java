package banduty.stoneycore.networking.packet;

import banduty.stoneycore.StoneyCore;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record OutlineClaimS2CPacket(List<BlockPos> positions) {
    private static final List<BlockPos> OUTLINE_POSITIONS = new ArrayList<>();
    private static final BlockPos FIX_BLOCKPOS_OUTLINE = new BlockPos(0, 400, 0);

    public static void handle(OutlineClaimS2CPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            synchronized (OUTLINE_POSITIONS) {
                OUTLINE_POSITIONS.clear();
                OUTLINE_POSITIONS.addAll(msg.positions);
                OUTLINE_POSITIONS.add(FIX_BLOCKPOS_OUTLINE);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static OutlineClaimS2CPacket decode(FriendlyByteBuf buf) {
        int count = buf.readInt();
        List<BlockPos> positions = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            positions.add(buf.readBlockPos());
        }
        return new OutlineClaimS2CPacket(positions);
    }

    public static void encode(OutlineClaimS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.positions.size());
        for (BlockPos pos : msg.positions) {
            buf.writeBlockPos(pos);
        }
    }

    @Mod.EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientHandler {
        @SubscribeEvent
        public static void onRenderLevel(RenderLevelStageEvent event) {
            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) return;

            Minecraft client = Minecraft.getInstance();
            if (client.player == null) {
                synchronized (OUTLINE_POSITIONS) {
                    OUTLINE_POSITIONS.clear();
                }
                return;
            }

            float[] rgba = intToRgba(StoneyCore.getConfig().visualOptions().claimOutlineColor());

            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource.BufferSource bufferSource = client.renderBuffers().bufferSource();

            for (BlockPos pos : OUTLINE_POSITIONS) {
                poseStack.pushPose();
                DebugRenderer.renderFilledBox(
                        poseStack,
                        bufferSource,
                        pos,
                        pos.offset(1, 1, 1),
                        rgba[0], rgba[1], rgba[2], pos == FIX_BLOCKPOS_OUTLINE ? 0 : rgba[3]
                );
                poseStack.popPose();
            }

            bufferSource.endBatch();
        }
    }

    public static float[] intToRgba(int color) {
        int r = (color >> 24) & 0xFF;
        int g = (color >> 16) & 0xFF;
        int b = (color >> 8) & 0xFF;
        int a = color & 0xFF;

        return new float[]{
                r / 255f,
                g / 255f,
                b / 255f,
                a / 255f
        };
    }
}