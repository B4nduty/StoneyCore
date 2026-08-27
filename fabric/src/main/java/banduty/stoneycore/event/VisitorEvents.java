package banduty.stoneycore.event;

import banduty.stoneycore.lands.visitor.VisitorTracker;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class VisitorEvents implements ServerLivingEntityEvents.AfterDeath, UseBlockCallback, PlayerBlockBreakEvents.After {

    public static void register() {
        VisitorEvents events = new VisitorEvents();
        ServerLivingEntityEvents.AFTER_DEATH.register(events);
        UseBlockCallback.EVENT.register(events);
        PlayerBlockBreakEvents.AFTER.register(events);
    }

    @Override
    public void afterDeath(LivingEntity livingEntity, DamageSource damageSource) {
        if (livingEntity instanceof Villager villager && villager.level() instanceof ServerLevel serverLevel) {
            VisitorTracker.onVillagerDeath(villager, serverLevel);
        }
    }

    @Override
    public InteractionResult interact(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (!(player.getMainHandItem().getItem() instanceof BlockItem blockItem) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }
        BlockState placedState = blockItem.getBlock().defaultBlockState();
        BlockPos pos = hitResult.getBlockPos();
        VisitorTracker.onBlockPlace(player, serverLevel, placedState, pos);
        return InteractionResult.PASS;
    }

    @Override
    public void afterBlockBreak(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (level instanceof ServerLevel serverLevel) {
            VisitorTracker.onBlockBreak(player, serverLevel, state, pos);
        }
    }
}
