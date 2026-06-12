package banduty.stoneycore.mixin;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.combat.melee.CombatSelect;
import banduty.stoneycore.util.EntityDamageUtil;
import banduty.stoneycore.util.WeightUtil;
import banduty.stoneycore.util.data.entitydata.StaminaData;
import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import net.minecraft.core.registries.Registries;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Unique
    private final Player playerEntity = (Player)(Object)this;

    @Unique private static final float SHIELD_DISABLE_CHANCE_BASE = 0.25F;
    @Unique private static final float SHIELD_DISABLE_CHANCE_SPRINT_BONUS = 0.75F;
    @Unique private static final int SHIELD_COOLDOWN_TICKS = 100;
    @Unique private static final int VANILLA_SHIELD_COOLDOWN_TICKS = 60;

    @Inject(method = "hurtCurrentlyUsedShield", at = @At("HEAD"), cancellable = true)
    private void stoneycore$hurtCurrentlyUsedShield(float amount, CallbackInfo ci) {
        if (playerEntity.level().isClientSide()) return;

        if (playerEntity.getUseItem().is(SCTags.WEAPONS_SHIELD.getTag())) {
            int i = 1 + Mth.floor(amount);
            playerEntity.getUseItem().hurtAndBreak(i, playerEntity, LivingEntity.getSlotForHand(playerEntity.getUsedItemHand()));
            playerEntity.awardStat(Stats.ITEM_USED.get(playerEntity.getUseItem().getItem()));
            ci.cancel();

            StaminaData.removeStamina(playerEntity, StoneyCore.getConfig().combatOptions().onBlockStaminaConstant() * WeightUtil.getWeight(playerEntity));
        }
    }

    @Inject(method = "disableShield", at = @At("HEAD"), cancellable = true)
    public void stoneycore$disableShield(CallbackInfo ci) {
        ItemStack activeItem = playerEntity.getUseItem();
        Level level = playerEntity.level();

        float efficiencyLevel = EnchantmentHelper.getEnchantmentLevel(
                level.holderLookup(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY),
                playerEntity
        );
        float disableChance = SHIELD_DISABLE_CHANCE_BASE + efficiencyLevel * 0.05F;
        if (playerEntity.isSprinting()) {
            disableChance += SHIELD_DISABLE_CHANCE_SPRINT_BONUS;
        }

        if (playerEntity.getRandom().nextFloat() < disableChance) {
            if (!playerEntity.isCreative()) {
                int cooldownTicks = activeItem.is(SCTags.WEAPONS_SHIELD.getTag()) ? SHIELD_COOLDOWN_TICKS : VANILLA_SHIELD_COOLDOWN_TICKS;
                playerEntity.getCooldowns().addCooldown(activeItem.getItem(), cooldownTicks);
            }
            playerEntity.stopUsingItem();
            level.broadcastEntityEvent(playerEntity, (byte) 30);
            ci.cancel();
        }
    }

    @ModifyVariable(
            method = "attack",
            at = @At(
                    value = "STORE",
                    ordinal = 0
            ),
            index = 2
    )
    private float stoneycore$modifyDamage(float originalBaseDamage, Entity target) {
        if (!(target instanceof LivingEntity livingTarget) || livingTarget.level().isClientSide()) return originalBaseDamage;

        ItemStack weaponStack = CombatSelect.getWeaponStack(playerEntity, playerEntity.getMainHandItem());

        if (weaponStack == null || weaponStack.isEmpty() || weaponStack.is(Items.AIR)) {
            return originalBaseDamage;
        }
        if (!WeaponDefinitionsStorage.isMelee(weaponStack)) {
            return originalBaseDamage;
        }

        double damage = EntityDamageUtil.onDamage(livingTarget, playerEntity, weaponStack);

        if (weaponStack.is(SCTags.BROKEN_WEAPONS.getTag()) && weaponStack.getDamageValue() >= weaponStack.getMaxDamage() * 0.9f) {
            damage *= 0.2f;
        }

        return (float) Math.max(0.0, damage);
    }
}
