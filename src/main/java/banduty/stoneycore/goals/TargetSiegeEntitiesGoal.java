package banduty.stoneycore.goals;

import banduty.stoneycore.entity.custom.AbstractSiegeEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.VindicatorEntity;

public class TargetSiegeEntitiesGoal extends TrackTargetGoal {
    private final VindicatorEntity vindicator;

    public TargetSiegeEntitiesGoal(VindicatorEntity vindicator) {
        super(vindicator, false);
        this.vindicator = vindicator;
    }

    @Override
    public boolean canStart() {
        TargetPredicate predicate = TargetPredicate.createAttackable().ignoreDistanceScalingFactor();

        AbstractSiegeEntity target = this.vindicator.getWorld().getClosestEntity(
                AbstractSiegeEntity.class,
                predicate,
                vindicator,
                vindicator.getX(),
                vindicator.getY(),
                vindicator.getZ(),
                vindicator.getBoundingBox().expand(16.0)
        );

        if (target != null) {
            this.vindicator.setTarget(target);
            return true;
        }
        return false;
    }
}
