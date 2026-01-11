package banduty.stoneycore.entity.custom;

import banduty.stoneycore.entity.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;

public class FabricSCBulletEntityHelper implements SCBulletEntityHelper {
    @Override
    public EntityType<? extends AbstractArrow> getBulletEntity() {
        return ModEntities.SC_BULLET.get();
    }
}
