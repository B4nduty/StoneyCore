package banduty.stoneycore.entity.custom;

import banduty.stoneycore.mixin.PersistentProjectileEntityAccessor;
import banduty.stoneycore.util.SCDamageCalculator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class SCArrowEntity extends PersistentProjectileEntity {
    private SCDamageCalculator.DamageType damageType;
    private float damage;

    public SCArrowEntity(EntityType<? extends PersistentProjectileEntity> type, World world) {
        super(type, world);
    }

    public SCArrowEntity(EntityType<? extends SCArrowEntity> scArrow, LivingEntity shooter, World world) {
        super(scArrow, shooter, world);
    }

    public void setDamageAmount(float damage) {
        this.damage = damage;
    }

    public float getDamageAmount() {
        return this.damage;
    }

    public void setDamageType(SCDamageCalculator.DamageType damageType) {
        this.damageType = damageType;
    }

    public SCDamageCalculator.DamageType getDamageType() {
        return this.damageType;
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    @Override
    protected ItemStack asItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        if (this.getPierceLevel() > 0) {
            PersistentProjectileEntityAccessor accessor = (PersistentProjectileEntityAccessor) this;

            IntOpenHashSet piercedEntities = accessor.getPiercedEntities();
            if (piercedEntities == null) {
                piercedEntities = new IntOpenHashSet(5);
                accessor.setPiercedEntities(piercedEntities);
            }

            if (piercedEntities.size() >= this.getPierceLevel() + 1) {
                this.discard();
                return;
            }

            piercedEntities.add(entity.getId());
        }

        boolean bl = entity.getType() == EntityType.ENDERMAN;
        int j = entity.getFireTicks();
        if (this.isOnFire() && !bl) {
            entity.setOnFireFor(5);
        }

        entity.setFireTicks(j);
        this.setVelocity(this.getVelocity().multiply(-0.1));
        this.setYaw(this.getYaw() + 180.0F);
        this.prevYaw += 180.0F;
        if (!this.getWorld().isClient && this.getVelocity().lengthSquared() < 1.0E-7) {
            if (this.pickupType == PersistentProjectileEntity.PickupPermission.ALLOWED) {
                this.dropStack(this.asItemStack(), 0.1F);
            }

            this.discard();
        }

    }

    public void scHitEntity(LivingEntity target, ItemStack stack, float damage) {
        damage = SCDamageCalculator.getSCDamage(target, damage, getDamageType());
        SCDamageCalculator.applyDamage(target, (LivingEntity) getOwner(), stack, damage);
        if (this.isOnFire()) target.setOnFireFor(5);
    }
}