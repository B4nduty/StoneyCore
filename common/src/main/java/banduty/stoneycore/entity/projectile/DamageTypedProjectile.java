package banduty.stoneycore.entity.projectile;

import banduty.stoneycore.combat.damagetype.SCDamageType;
import org.jetbrains.annotations.Nullable;

public interface DamageTypedProjectile {
    @Nullable
    SCDamageType getDamageType();
}