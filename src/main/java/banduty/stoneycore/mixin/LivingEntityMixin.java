package banduty.stoneycore.mixin;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.event.custom.LivingEntityDamageEvents;
import banduty.stoneycore.items.item.SCWeapon;
import banduty.stoneycore.util.itemdata.SCTags;
import banduty.stoneycore.util.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.playerdata.StaminaData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Unique
    private boolean blockShield = true;

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void stoneycore$onJump(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity player && isStaminaBlocked(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
    private void stoneycore$onSprinting(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity player && isStaminaBlocked(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "setCurrentHand", at = @At("HEAD"), cancellable = true)
    private void stoneycore$onSetCurrentHand(Hand hand, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity player && isStaminaBlocked(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "disablesShield", at = @At("HEAD"), cancellable = true)
    public void stoneycore$disablesShield(CallbackInfoReturnable<Boolean> cir) {
        ItemStack mainStack = ((LivingEntity) (Object) this).getMainHandStack();
        boolean isWeaponOrInTag = mainStack.getItem() instanceof AxeItem
                || mainStack.isIn(SCTags.WEAPONS_DISABLE_SHIELD.getTag());

        if (StoneyCore.getConfig().getVanillaWeaponsDamage0()) {
            cir.setReturnValue(mainStack.isIn(SCTags.WEAPONS_DISABLE_SHIELD.getTag()));
        } else {
            cir.setReturnValue(isWeaponOrInTag);
        }
    }

    @Inject(method = "damage", at = @At("HEAD"))
    private void stoneycore$injectDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.getAttacker() instanceof PlayerEntity attacker) {
            if (attacker.getMainHandStack().isIn(SCTags.WEAPONS_BYPASS_BLOCK.getTag())) {
                this.blockShield = false;
            }
        }
    }

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float stoneycore$modifyDamageAmount(float amount, DamageSource source) {
        return LivingEntityDamageEvents.EVENT.invoker().onDamage((LivingEntity) (Object) this, source, amount);
    }

    @Inject(method = "applyDamage", at = @At("TAIL"))
    private void stoneycore$sendDamage(DamageSource source, float amount, CallbackInfo ci) {
        if (StoneyCore.getConfig().getDamageIndicator() && source.getAttacker() instanceof PlayerEntity player) {
            ItemStack mainHandStack = player.getMainHandStack();
            if (mainHandStack.getItem() instanceof SCWeapon && !player.hasStatusEffect(StatusEffects.WEAKNESS)) {
                if (amount <= 0) amount = 0;
                player.sendMessage(Text.literal("Damage: " + (int) amount), true);
            }
        }
    }

    @Redirect(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;blockedByShield(Lnet/minecraft/entity/damage/DamageSource;)Z"))
    private boolean stoneycore$redirectBlockedByShield(LivingEntity instance, DamageSource source) {
        return blockShield && instance.blockedByShield(source);
    }

    @Unique
    private boolean isStaminaBlocked(PlayerEntity player) {
        return StaminaData.isStaminaBlocked((IEntityDataSaver) player);
    }
}