package banduty.stoneycore.event;

import banduty.stoneycore.lands.visitor.VisitorTracker;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;

public class VisitorDeath implements ServerLivingEntityEvents.AfterDeath {
    @Override
    public void afterDeath(LivingEntity livingEntity, DamageSource damageSource) {
        if (livingEntity instanceof Villager villager && villager.level() instanceof ServerLevel serverLevel)
            VisitorTracker.onVillagerDeath(villager, serverLevel);
    }
}
