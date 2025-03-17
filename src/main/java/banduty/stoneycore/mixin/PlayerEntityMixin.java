package banduty.stoneycore.mixin;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.util.definitionsloader.SCMeleeWeaponDefinitionsLoader;
import banduty.stoneycore.util.itemdata.SCTags;
import banduty.stoneycore.util.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.playerdata.StaminaData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements IEntityDataSaver {
    @Unique
    private final PlayerEntity playerEntity = (PlayerEntity) (Object) this;

    @Unique
    private NbtCompound persistentData;

    @Unique
    private static final int STAMINA_COST_ON_SHIELD_DAMAGE = 2;
    @Unique
    private static final float SHIELD_DISABLE_CHANCE_BASE = 0.25F;
    @Unique
    private static final float SHIELD_DISABLE_CHANCE_SPRINT_BONUS = 0.75F;
    @Unique
    private static final int SHIELD_COOLDOWN_TICKS = 100;
    @Unique
    private static final int VANILLA_SHIELD_COOLDOWN_TICKS = 60;

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

    @Inject(method = "damageShield", at = @At("HEAD"), cancellable = true)
    private void stoneycore$onDamageShield(float amount, CallbackInfo ci) {
        ItemStack mainHandStack = playerEntity.getMainHandStack();
        if (SCMeleeWeaponDefinitionsLoader.containsItem(mainHandStack.getItem()) && playerEntity.getActiveItem().isIn(SCTags.WEAPONS_SHIELD.getTag())) {
            if (!playerEntity.getWorld().isClient) {
                playerEntity.incrementStat(Stats.USED.getOrCreateStat(playerEntity.getActiveItem().getItem()));
            }
            ci.cancel();

            if (StoneyCore.getConfig().getBlocking()) {
                float stamina = StaminaData.getStamina((IEntityDataSaver) playerEntity);
                StaminaData.removeStamina(this, Math.min(stamina, STAMINA_COST_ON_SHIELD_DAMAGE));
            }
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
}