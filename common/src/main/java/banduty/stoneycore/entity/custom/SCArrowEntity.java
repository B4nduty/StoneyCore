package banduty.stoneycore.entity.custom;

import banduty.stoneycore.mixin.AbstractArrowAccessor;
import banduty.stoneycore.util.DeflectChanceHelper;
import banduty.stoneycore.util.SCDamageCalculator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public abstract class SCArrowEntity extends AbstractArrow {
    private SCDamageCalculator.DamageType damageType;

    public SCArrowEntity(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
    }

    public SCArrowEntity(EntityType<? extends SCArrowEntity> scArrow, LivingEntity shooter, Level level) {
        super(scArrow, shooter, level);
    }

    public void setDamageType(SCDamageCalculator.DamageType damageType) {
        this.damageType = damageType;
    }

    public SCDamageCalculator.DamageType getDamageType() {
        return this.damageType;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    protected abstract ItemStack getPickupItem();

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        if (entityHitResult.getEntity() instanceof LivingEntity livingEntity) {
            if (this.getPickupItem().isEmpty()) {
                return;
            }
            if (DeflectChanceHelper.shouldDeflect(livingEntity, this.getPickupItem())) {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5).scale(-1));
                this.setBaseDamage(this.getBbHeight() * 0.5);
                this.setYRot(this.getYRot() + 180);
                if (this.getBaseDamage() <= 1) this.discard();
                for (ServerPlayer player : serverLevel.players()) {
                    if (player != null) {
                        Vec3 playerPos = player.position();
                        Vec3 impactPos = entityHitResult.getLocation();
                        double distance = playerPos.distanceTo(impactPos);

                        float volume = (float) Math.max(0, 1 - (distance * 0.1));

                        this.playSound(SoundEvents.ANVIL_PLACE, volume, 2.5F / (this.random.nextFloat() * 0.2F + 0.9F));
                    }
                }
                return;
            }
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

        damage = SCDamageCalculator.getSCDamage(target, damage, getDamageType());
        SCDamageCalculator.applyDamage(target, this, stack, damage);
        if (this.isOnFire()) target.setRemainingFireTicks(5);
        return true;
    }
}