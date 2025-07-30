package banduty.stoneycore.mixin;

import banduty.stoneycore.util.definitionsloader.SCAccessoriesDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.SCArmorDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.SCWeaponDefinitionsLoader;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(PersistentProjectileEntity.class)
public class PersistentProjectileEntityMixin {
    @Unique
    private final Random random = new Random();

    @Inject(method = "onEntityHit", at = @At("HEAD"), cancellable = true)
    private void onEntityHit(EntityHitResult entityHitResult, CallbackInfo ci) {
        if (entityHitResult.getEntity().getWorld().isClient()) return;
        PersistentProjectileEntity projectileEntity = (PersistentProjectileEntity) (Object) this;
        if (projectileEntity.getWorld().isClient()) return;

        if (projectileEntity.getPickBlockStack() == null || projectileEntity.getPickBlockStack().isEmpty()) {
            return;
        }

        if (projectileEntity != null) {
            if (entityHitResult.getEntity() instanceof LivingEntity livingEntity && shouldDeflect(livingEntity)) {
                projectileEntity.setVelocity(
                        -projectileEntity.getVelocity().x / 2,
                        -projectileEntity.getVelocity().y / 2,
                        -projectileEntity.getVelocity().z / 2
                );
                projectileEntity.updatePosition(
                        projectileEntity.getX() + projectileEntity.getVelocity().x * 0.1,
                        projectileEntity.getY() + projectileEntity.getVelocity().y * 0.1,
                        projectileEntity.getZ() + projectileEntity.getVelocity().z * 0.1
                );
                if (livingEntity == projectileEntity.getOwner()) projectileEntity.discard();
                ci.cancel();
            }
        }
    }

    @Unique
    private boolean shouldDeflect(LivingEntity livingEntity) {
        double deflectProbability = calculateDeflectProbability(livingEntity);
        double random2 = random.nextDouble();
        return random2 < deflectProbability;
    }

    @Unique
    private double calculateDeflectProbability(LivingEntity livingEntity) {
        PersistentProjectileEntity projectileEntity = (PersistentProjectileEntity) (Object) this;
        Identifier projectileId = Registries.ENTITY_TYPE.getId(projectileEntity.getType());
        if (projectileId == null) return 0;
        String projectileKey = projectileId.toString();
        double deflectChance = 0f;

        if (AccessoriesCapability.getOptionally(livingEntity).isPresent()) {
            for (SlotEntryReference equipped : AccessoriesCapability.get(livingEntity).getAllEquipped()) {
                ItemStack itemStack = equipped.stack();
                if (SCAccessoriesDefinitionsLoader.containsItem(itemStack)) {
                    deflectChance += SCAccessoriesDefinitionsLoader.getData(itemStack).deflectChance().getOrDefault(projectileKey, 0.0);
                }
            }
        }

        for (ItemStack itemStack : livingEntity.getArmorItems()) {
            if (SCArmorDefinitionsLoader.containsItem(itemStack)) {
                deflectChance += SCArmorDefinitionsLoader.getData(itemStack).deflectChance().getOrDefault(projectileKey, 0.0);
            }
        }

        if (projectileEntity.getPickBlockStack() != null && SCWeaponDefinitionsLoader.isAmmo(projectileEntity.getPickBlockStack())) {
            deflectChance += SCWeaponDefinitionsLoader.getData(projectileEntity.getPickBlockStack().getItem()).melee().deflectChance();
        }

        return deflectChance;
    }
}
