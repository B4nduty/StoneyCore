package banduty.stoneycore.event;

import banduty.stoneycore.lands.visitor.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class VisitorEvents {
    @SubscribeEvent
    public void onVillagerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Villager villager && villager.level() instanceof ServerLevel level) {
            VisitorTracker.onVillagerDeath(villager, level);
        }
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player &&
                event.getLevel() instanceof ServerLevel level) {
            BlockState placedState = event.getPlacedBlock();
            BlockPos pos = event.getPos();

            VisitorTracker.onBlockPlace(player, level, placedState, pos);
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() != null && event.getLevel() instanceof ServerLevel level) {
            Player player = event.getPlayer();
            BlockPos pos = event.getPos();
            BlockState brokenState = event.getState();

            VisitorTracker.onBlockBreak(player, level, brokenState, pos);
        }
    }
}