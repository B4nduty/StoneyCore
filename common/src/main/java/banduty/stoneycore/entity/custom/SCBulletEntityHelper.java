package banduty.stoneycore.entity.custom;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;

public interface SCBulletEntityHelper {
    EntityType<? extends AbstractArrow> getBulletEntity();
}
