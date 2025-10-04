package banduty.stoneycore.mixin;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.event.custom.PlayerNameTagEvents;
import banduty.stoneycore.util.EntityDamageUtil;
import banduty.stoneycore.util.WeightUtil;
import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsLoader;
import banduty.stoneycore.util.data.playerdata.StaminaData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.SwordItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Unique
    private final PlayerEntity playerEntity = (PlayerEntity)(Object)this;

    @Unique private static final float SHIELD_DISABLE_CHANCE_BASE = 0.25F;
    @Unique private static final float SHIELD_DISABLE_CHANCE_SPRINT_BONUS = 0.75F;
    @Unique private static final int SHIELD_COOLDOWN_TICKS = 100;
    @Unique private static final int VANILLA_SHIELD_COOLDOWN_TICKS = 60;

    @ModifyArg(
            method = "getDisplayName",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/scoreboard/Team;decorateName(Lnet/minecraft/scoreboard/AbstractTeam;Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;"
            )
    )
    private Text stoneycore$addTags(Text baseName) {
        if (!(playerEntity instanceof ServerPlayerEntity serverPlayer)) {
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
        if (playerEntity.getWorld().isClient()) return;

        if (playerEntity.getActiveItem().isIn(SCTags.WEAPONS_SHIELD.getTag())) {
            int i = 1 + MathHelper.floor(amount);
            playerEntity.getActiveItem().damage(i, playerEntity, (player) -> player.sendToolBreakStatus(playerEntity.getActiveHand()));
            playerEntity.incrementStat(Stats.USED.getOrCreateStat(playerEntity.getActiveItem().getItem()));
            ci.cancel();

            StaminaData.removeStamina(playerEntity, StoneyCore.getConfig().combatOptions.onBlockStaminaConstant() * WeightUtil.getCachedWeight(playerEntity));
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

    @ModifyVariable(
            method = "attack",
            at = @At(
                    value = "STORE",
                    ordinal = 0
            ),
            ordinal = 0
    )
    private float stoneycore$modifyDamage(float f, Entity target) {
        if (!(target instanceof LivingEntity living) || living.getWorld().isClient()) return f;
        ItemStack weaponStack = EntityDamageUtil.getWeaponStack(playerEntity);

        if (!WeaponDefinitionsLoader.isMelee(weaponStack)) return f;

        float weaponDamage = 0;
        if (weaponStack.getItem() instanceof SwordItem swordItem) weaponDamage = swordItem.getAttackDamage();
        else if (weaponStack.getItem() instanceof MiningToolItem miningToolItem) weaponDamage = miningToolItem.getAttackDamage();

        double extra = EntityDamageUtil.onDamage(living, playerEntity, weaponStack);

        return f + (float)extra - weaponDamage - 1;
    }
}
