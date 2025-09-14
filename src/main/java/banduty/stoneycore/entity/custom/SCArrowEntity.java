package banduty.stoneycore.entity.custom;

import banduty.stoneycore.mixin.PersistentProjectileEntityAccessor;
import banduty.stoneycore.util.DeflectChanceHelper;
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

public abstract class SCArrowEntity extends PersistentProjectileEntity {
    private SCDamageCalculator.DamageType damageType;

    public SCArrowEntity(EntityType<? extends PersistentProjectileEntity> type, World world) {
        super(type, world);
    }

    public SCArrowEntity(EntityType<? extends SCArrowEntity> scArrow, LivingEntity shooter, World world) {
        super(scArrow, shooter, world);
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

    protected abstract ItemStack asItemStack();

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (this.getWorld().isClient()) return;

        if (entityHitResult.getEntity() instanceof LivingEntity livingEntity) {
            if (this.asItemStack() == null || this.asItemStack().isEmpty()) {
                return;
            }
            if (DeflectChanceHelper.shouldDeflect(livingEntity, this.asItemStack())) {
                this.setVelocity(this.getVelocity().multiply(0.5).multiply(-1));
                this.setDamage(this.getDamage() * 0.5);
                this.setYaw(this.getYaw() + 180);
                if (this.getDamage() <= 1) this.discard();
                return;
            }
        }

        onSCEntityHit(entityHitResult);

        this.discard();

    }

    protected void onSCEntityHit(EntityHitResult entityHitResult) {
        if (this.getWorld().isClient()) return;

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
    }

    public boolean scHitEntity(LivingEntity target, ItemStack stack, double damage) {
        if (this.getWorld().isClient()) return false;

        damage = SCDamageCalculator.getSCDamage(target, damage, getDamageType());
        SCDamageCalculator.applyDamage(target, this, stack, damage);
        if (this.isOnFire()) target.setOnFireFor(5);
        return true;
    }
}