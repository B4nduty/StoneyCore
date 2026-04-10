package banduty.stoneycore.event;

import banduty.stoneycore.lands.visitor.*;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class VisitorUseBlock implements UseBlockCallback {
    @Override
    public InteractionResult interact(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (!(player.getMainHandItem().getItem() instanceof BlockItem blockItem) || !(level instanceof ServerLevel serverLevel)) return InteractionResult.PASS;
        BlockState placedState = blockItem.getBlock().defaultBlockState();
        BlockPos pos = hitResult.getBlockPos();
        VisitorTracker.onBlockPlace(player, serverLevel, placedState, pos);
        return InteractionResult.PASS;
    }
}
