package banduty.stoneycore.mixin;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.event.custom.PlayerNameTagEvents;
import banduty.stoneycore.util.EntityDamageUtil;
import banduty.stoneycore.util.WeightUtil;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsLoader;
import banduty.stoneycore.util.itemdata.SCTags;
import banduty.stoneycore.util.playerdata.StaminaData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

import static banduty.stoneycore.util.EntityDamageUtil.getWeaponStack;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Unique
    private final PlayerEntity playerEntity = (PlayerEntity) (Object) this;
    @Unique
    private static final float SHIELD_DISABLE_CHANCE_BASE = 0.25F;
    @Unique
    private static final float SHIELD_DISABLE_CHANCE_SPRINT_BONUS = 0.75F;
    @Unique
    private static final int SHIELD_COOLDOWN_TICKS = 100;
    @Unique
    private static final int VANILLA_SHIELD_COOLDOWN_TICKS = 60;

    @ModifyArg(
            method = "getDisplayName",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/scoreboard/Team;decorateName(Lnet/minecraft/scoreboard/AbstractTeam;Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;"
            )
    )
    private Text stoneycore$addTags(Text baseName) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (!(self instanceof ServerPlayerEntity serverPlayer)) {
            return baseName;
        }

        var tags = PlayerNameTagEvents.EVENT.invoker().collectTags(serverPlayer);

        MutableText result = Text.empty();
        for (var entry : tags) {
            if (!entry.text().getString().isEmpty()) {
                result = result.append(entry.text()).append(Text.literal(" "));
            }
        }

        return result.append(baseName);
    }

    @Inject(method = "damageShield", at = @At("HEAD"), cancellable = true)
    private void stoneycore$onDamageShield(float amount, CallbackInfo ci) {
        ItemStack mainHandStack = playerEntity.getMainHandStack();
        if (WeaponDefinitionsLoader.isMelee(mainHandStack.getItem()) && playerEntity.getActiveItem().isIn(SCTags.WEAPONS_SHIELD.getTag())) {
            if (!playerEntity.getWorld().isClient) {
                playerEntity.incrementStat(Stats.USED.getOrCreateStat(playerEntity.getActiveItem().getItem()));
            }
            ci.cancel();

            StaminaData.removeStamina((LivingEntity) (Object) this, StoneyCore.getConfig().combatOptions.onBlockStaminaConstant() * WeightUtil.getCachedWeight(playerEntity));
        }
    }

    @Inject(method = "disableShield", at = @At("HEAD"), cancellable = true)
    public void stoneycore$disableShield(boolean sprinting, CallbackInfo ci) {
        ItemStack activeItem = playerEntity.getActiveItem();
        World world = playerEntity.getWorld();

        float disableChance = SHIELD_DISABLE_CHANCE_BASE + (float) EnchantmentHelper.getEfficiency(playerEntity) * 0.05F;
        if (sprinting) {
            disableChance += SHIELD_DISABLE_CHANCE_SPRINT_BONUS;
        }

        if (playerEntity.getRandom().nextFloat() < disableChance) {
            if (!playerEntity.isCreative()) {
                int cooldownTicks = activeItem.isIn(SCTags.WEAPONS_SHIELD.getTag()) ? SHIELD_COOLDOWN_TICKS : VANILLA_SHIELD_COOLDOWN_TICKS;
                playerEntity.getItemCooldownManager().set(activeItem.getItem(), cooldownTicks);
            }
            playerEntity.clearActiveItem();
            world.sendEntityStatus(playerEntity, (byte) 30);
            ci.cancel();
        }
    }

    @Unique
    private UUID currentModifierId = null;

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void stoneycore$attackLivingEntity(Entity target, CallbackInfo ci) {
        if (!(target instanceof LivingEntity livingEntity) || livingEntity.getWorld().isClient()) return;
        PlayerEntity playerEntity = (PlayerEntity) (Object) this;
        ItemStack weaponStack = getWeaponStack(playerEntity);
        if (!WeaponDefinitionsLoader.isMelee(weaponStack)) return;

        EntityAttributeInstance attackDamage = playerEntity.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);

        if (attackDamage != null) {
            double additionalDamage = EntityDamageUtil.onDamage(livingEntity, playerEntity, weaponStack, ci);

            if (currentModifierId != null) {
                attackDamage.removeModifier(currentModifierId);
            }

            currentModifierId = UUID.randomUUID();

            EntityAttributeModifier modifier = new EntityAttributeModifier(
                    currentModifierId,
                    "stoneycore_damage_bonus",
                    additionalDamage - attackDamage.getBaseValue(),
                    EntityAttributeModifier.Operation.ADDITION
            );

            attackDamage.addTemporaryModifier(modifier);
        }
    }

    @Inject(method = "attack", at = @At("TAIL"))
    private void stoneycore$removeDamageModifier(Entity target, CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        EntityAttributeInstance attackDamage = livingEntity.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);

        if (attackDamage != null && currentModifierId != null) {
            attackDamage.removeModifier(currentModifierId);
            currentModifierId = null;
        }
    }
}