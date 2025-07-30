package banduty.stoneycore.mixin;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.event.custom.LivingEntityDamageEvents;
import banduty.stoneycore.util.definitionsloader.SCArmorDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.SCWeaponDefinitionsLoader;
import banduty.stoneycore.util.itemdata.SCTags;
import banduty.stoneycore.util.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.playerdata.SCAttributes;
import banduty.stoneycore.util.playerdata.StaminaData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements IEntityDataSaver {
    @Unique
    private NbtCompound persistentData;

    LivingEntityMixin(final EntityType<?> type, final World world) {
        super(type, world);
    }

    @Override
    public NbtCompound stoneycore$getPersistentData() {
        if (persistentData == null) {
            persistentData = new NbtCompound();
        }
        return persistentData;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    protected void stoneycore$injectWriteMethod(NbtCompound nbt, CallbackInfo ci) {
        if (persistentData != null) {
            nbt.put("stoneycore.data", persistentData);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    protected void stoneycore$injectReadMethod(NbtCompound nbt, CallbackInfo info) {
        if (nbt.contains("stoneycore.data", 10)) {
            persistentData = nbt.getCompound("stoneycore.data");
        }
    }

    @Inject(
            method = "createLivingAttributes()Lnet/minecraft/entity/attribute/DefaultAttributeContainer$Builder;",
            require = 1, allow = 1, at = @At("RETURN"))
    private static void addAttributes(final CallbackInfoReturnable<DefaultAttributeContainer.Builder> info) {
        info.getReturnValue().add(SCAttributes.HUNGER_DRAIN_MULTIPLIER).add(SCAttributes.STAMINA).add(SCAttributes.MAX_STAMINA);
    }

    @Unique
    private boolean blockShield = true;

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void stoneycore$onJump(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (isStaminaBlocked(livingEntity)) {
            ci.cancel();
        }
    }

    @Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
    private void stoneycore$onSprinting(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (isStaminaBlocked(livingEntity)) {
            ci.cancel();
        }
    }

    @Inject(method = "setCurrentHand", at = @At("HEAD"), cancellable = true)
    private void stoneycore$onSetCurrentHand(Hand hand, CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (isStaminaBlocked(livingEntity)) {
            ci.cancel();
        }
    }

    @Inject(method = "disablesShield", at = @At("HEAD"), cancellable = true)
    public void stoneycore$disablesShield(CallbackInfoReturnable<Boolean> cir) {
        ItemStack mainStack = ((LivingEntity) (Object) this).getMainHandStack();
        boolean isWeaponOrInTag = mainStack.getItem() instanceof AxeItem
                || mainStack.isIn(SCTags.WEAPONS_DISABLE_SHIELD.getTag());

        cir.setReturnValue(isWeaponOrInTag);
    }

    @Inject(method = "damage", at = @At("HEAD"))
    private void stoneycore$injectDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.getAttacker() instanceof LivingEntity attacker) {
            if (attacker.getMainHandStack().isIn(SCTags.WEAPONS_BYPASS_BLOCK.getTag())) {
                this.blockShield = false;
            }
        }
    }

    @ModifyVariable(
            method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
            at = @At("HEAD"),
            argsOnly = true,
            index = 2
    )
    private float modifyDamageAmount(float amount, DamageSource source) {
        amount = LivingEntityDamageEvents.EVENT.invoker().onDamage((LivingEntity) (Object) this, source, amount);
        return amount;
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void stoneycore$cancelZeroDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (amount <= 0) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "applyDamage", at = @At("TAIL"))
    private void stoneycore$sendDamage(DamageSource source, float amount, CallbackInfo ci) {
        if (StoneyCore.getConfig().visualOptions.getDamageIndicator() && source.getAttacker() instanceof PlayerEntity player) {
            ItemStack mainHandStack = player.getMainHandStack();
            if (SCWeaponDefinitionsLoader.isMelee(mainHandStack.getItem()) && !player.hasStatusEffect(StatusEffects.WEAKNESS)) {
                if (amount <= 0) amount = 0;
                player.sendMessage(Text.literal("Damage: " + (int) amount), true);
            }
        }
    }

    @Inject(method = "jump", at = @At("HEAD"))
    private void stoneycore$jump(CallbackInfo ci) {
        if ((Object) this instanceof LivingEntity livingEntity) {
            IEntityDataSaver dataSaver = (IEntityDataSaver) livingEntity;
            boolean staminaBlocked = StaminaData.isStaminaBlocked(dataSaver);
            boolean wearingSCArmor = isWearingSCArmor(livingEntity);
            if (!staminaBlocked && wearingSCArmor) {
                StaminaData.removeStamina(livingEntity, StoneyCore.getConfig().combatOptions.jumpingStamina());
            }
        }
    }

    @Redirect(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;blockedByShield(Lnet/minecraft/entity/damage/DamageSource;)Z"))
    private boolean stoneycore$redirectBlockedByShield(LivingEntity instance, DamageSource source) {
        return blockShield && instance.blockedByShield(source);
    }

    @Unique
    private boolean isStaminaBlocked(LivingEntity livingEntity) {
        return StaminaData.isStaminaBlocked((IEntityDataSaver) livingEntity);
    }

    @Unique
    private boolean isWearingSCArmor(LivingEntity livingEntity) {
        for (ItemStack armorStack : livingEntity.getArmorItems()) {
            if (SCArmorDefinitionsLoader.containsItem(armorStack.getItem())) {
                return true;
            }
        }
        return false;
    }
}