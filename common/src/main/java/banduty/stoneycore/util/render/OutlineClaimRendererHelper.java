package banduty.stoneycore.util.render;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public interface OutlineClaimRendererHelper {
    void renderOutlineClaim(ServerPlayer player);
    void sendClearPacket(ServerPlayer player);
    void sendOutlinePacket(ServerPlayer player, List<BlockPos> borderPositions);
}