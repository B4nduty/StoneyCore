package banduty.stoneycore.entity.projectile;

import banduty.stoneycore.combat.damagetype.SCDamageApplier;
import banduty.stoneycore.combat.damagetype.SCDamageCalculator;
import banduty.stoneycore.combat.damagetype.SCDamageType;
import banduty.stoneycore.mixin.AbstractArrowAccessor;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

public abstract class SCArrowEntity extends AbstractArrow {

    private SCDamageType damageType;

    public SCArrowEntity(EntityType<? extends SCArrowEntity> type, Level level) {
        super(type, level);
    }

    public SCArrowEntity(EntityType<? extends SCArrowEntity> type, LivingEntity shooter, Level level) {
        super(type, shooter, level, ItemStack.EMPTY, null);
    }

    public SCArrowEntity(EntityType<? extends SCArrowEntity> type, LivingEntity shooter, Level level, ItemStack stack, @Nullable ItemStack firedFromWeapon) {
        super(type, shooter, level, stack, firedFromWeapon);
    }

    public void setDamageType(SCDamageType damageType) {
        this.damageType = damageType;
    }

    public SCDamageType getDamageType() {
        return this.damageType;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        if (!(this.level() instanceof ServerLevel)) return;

        if (entityHitResult.getEntity() instanceof LivingEntity && this.getPickupItem().isEmpty()) {
            return;
        }

        onSCEntityHit(entityHitResult);
        this.discard();
    }

    protected void onSCEntityHit(EntityHitResult entityHitResult) {
        if (this.level().isClientSide()) return;

        Entity entity = entityHitResult.getEntity();
        if (this.getPierceLevel() > 0) {
            AbstractArrowAccessor accessor = (AbstractArrowAccessor) this;

            IntOpenHashSet piercedEntities = accessor.getPiercingIgnoreEntityIds();
            if (piercedEntities == null) {
                piercedEntities = new IntOpenHashSet(5);
                accessor.setPiercingIgnoreEntityIds(piercedEntities);
            }

            if (piercedEntities.size() >= this.getPierceLevel() + 1) {
                this.discard();
                return;
            }

            piercedEntities.add(entity.getId());
        }

        boolean bl = entity.getType() == EntityType.ENDERMAN;
        int j = entity.getRemainingFireTicks();
        if (this.isOnFire() && !bl) {
            entity.setRemainingFireTicks(5);
        }

        entity.setRemainingFireTicks(j);
    }

    public boolean scHitEntity(LivingEntity target, ItemStack stack, double damage) {
        if (this.level().isClientSide()) return false;

        damage *= getDeltaMovement().length();

        DamageSource source = this.damageSources().arrow(this, this.getOwner());
        if (this.level() instanceof ServerLevel serverLevel) {
            ItemStack weapon = stack != null && !stack.isEmpty() ? stack : this.getWeaponItem();
            damage = SCDamageCalculator.applyEnchantments(serverLevel, weapon, target, source, damage);
        }

        damage = SCDamageCalculator.applyArmor(target, damage, getDamageType(), source);
        SCDamageApplier.apply(target, this, stack, damage);
        if (this.isOnFire()) target.setRemainingFireTicks(5);
        return true;
    }
}