package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.armor.SCTrinketsItem;
import banduty.stoneycore.items.armor.SCUnderArmorItem;
import banduty.stoneycore.util.itemdata.SCTags;
import banduty.stoneycore.util.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.playerdata.StaminaData;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class StartTickHandler implements ServerTickEvents.StartTick {
    private int damageTick;

    @Override
    public void onStartTick(MinecraftServer server) {
        for (ServerPlayerEntity playerEntity : server.getPlayerManager().getPlayerList()) {
            if (!playerEntity.isSpectator()) {
                handlePlayerTick(playerEntity);
                if (StoneyCore.getConfig().getParry()) handleParry(playerEntity);
            }
            if (!isWearingFullSCArmorSet(playerEntity)) {
                TrinketsApi.getTrinketComponent(playerEntity).ifPresent(trinketComponent ->
                        trinketComponent.getAllEquipped().forEach(pair -> {
                            ItemStack trinketStack = pair.getRight();
                            if (trinketStack.getItem() instanceof SCTrinketsItem && !trinketStack.isIn(SCTags.ALWAYS_WEARABLE.getTag())) {
                                playerEntity.giveItemStack(trinketStack);
                                trinketStack.setCount(0);
                            }
                }));
            }

            int swallowTailArrowCount = ((IEntityDataSaver) playerEntity).stoneycore$getPersistentData().getInt("swallowtail_arrow_count");
            if (swallowTailArrowCount >= 1 && !playerEntity.isCreative()) {
                damageTick++;
                if (damageTick % 20 == 0 && (playerEntity.isSprinting() || playerEntity.getVelocity().horizontalLengthSquared() > 0)) {
                    playerEntity.damage(playerEntity.getDamageSources().genericKill(), 0.2f);
                }
            }
        }
    }

    private void handleParry(ServerPlayerEntity player) {
        boolean isBlocking = player.isBlocking();
        ItemStack activeItem = player.getActiveItem();
        boolean usingCustomShield = activeItem.isIn(SCTags.WEAPONS_SHIELD.getTag());

        NbtCompound persistentData = ((IEntityDataSaver) player).stoneycore$getPersistentData();
        boolean wasBlocking = persistentData.contains("BlockStartTick");

        if (isBlocking && usingCustomShield) {
            if (!wasBlocking) {
                persistentData.putInt("BlockStartTick", (int) player.getWorld().getTime());
            }
        } else {
            persistentData.remove("BlockStartTick");
        }
    }

    private boolean isWearingFullSCArmorSet(LivingEntity entity) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (isArmorSlot(slot)) {
                ItemStack armorPiece = entity.getEquippedStack(slot);
                if (!(armorPiece.getItem() instanceof SCUnderArmorItem)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isArmorSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET;
    }

    private void handlePlayerTick(ServerPlayerEntity playerEntity) {
        IEntityDataSaver dataSaver = (IEntityDataSaver) playerEntity;
        float stamina = StaminaData.getStamina(dataSaver);

        if ((playerEntity.isCreative() || playerEntity.isSpectator())) {
            if (stamina < StoneyCore.getConfig().maxStamina()) StaminaData.setStamina((IEntityDataSaver) playerEntity, StoneyCore.getConfig().maxStamina());
            removeStaminaEffects(playerEntity);
            StaminaData.setStaminaBlocked((IEntityDataSaver) playerEntity, false);
        }

        if (stamina > StoneyCore.getConfig().maxStamina())
            StaminaData.setStamina((IEntityDataSaver) playerEntity, StoneyCore.getConfig().maxStamina());

        if (!playerEntity.isCreative() || !playerEntity.isSpectator()) {
            handleStaminaRecovery(playerEntity, stamina);
            handleStaminaEffects(playerEntity, stamina);
            handleStaminaUsage(playerEntity, stamina);
        }
    }

    private void handleStaminaRecovery(ServerPlayerEntity playerEntity, float stamina) {
        double foodLevel = playerEntity.getHungerManager().getFoodLevel();
        double health = playerEntity.getHealth();
        double ticksPerRecovery = (foodLevel + health) / 5.0d;
        int roundOff = Math.min(1, (int) (10 - Math.round(ticksPerRecovery)));

        StaminaData.addStamina((IEntityDataSaver) playerEntity, 0);
        if (ticksPerRecovery != 0 && playerEntity.age % roundOff == 0 && stamina < StoneyCore.getConfig().maxStamina()
                && !playerEntity.isTouchingWater()) {
            StaminaData.addStamina((IEntityDataSaver) playerEntity, 0.1f);
        }
    }

    private void handleStaminaEffects(ServerPlayerEntity playerEntity, float stamina) {
        IEntityDataSaver dataSaver = (IEntityDataSaver) playerEntity;
        long firstLevel = Math.absExact((int) (StoneyCore.getConfig().maxStamina() * 0.3f));
        long secondLevel = Math.absExact((int) (StoneyCore.getConfig().maxStamina() * 0.15f));

        if (stamina < firstLevel && stamina > secondLevel) {
            applyStaminaEffects(playerEntity, 0, 0);
        }

        if (stamina == 0) {
            StaminaData.setStaminaBlocked(dataSaver, true);
            applyStaminaEffects(playerEntity, 3, 3);
        }

        if (StaminaData.isStaminaBlocked((IEntityDataSaver) playerEntity) && stamina >= secondLevel) {
            StaminaData.setStaminaBlocked(dataSaver, false);
            removeStaminaEffects(playerEntity);
        }

        if (stamina >= firstLevel) {
            removeStaminaEffects(playerEntity);
        }
    }

    private void handleStaminaUsage(ServerPlayerEntity playerEntity, float stamina) {
        IEntityDataSaver dataSaver = (IEntityDataSaver) playerEntity;
        boolean staminaBlocked = StaminaData.isStaminaBlocked((IEntityDataSaver) playerEntity);

        if (isHoldingSCWeapon(playerEntity) && !staminaBlocked && !StoneyCore.getConfig().getBlocking()
                && playerEntity.isBlocking() && stamina >= 0.1f && playerEntity.age % 2 == 0) {
            StaminaData.removeStamina(dataSaver, 0.1f);
        }

        if (isWearingSCArmor(playerEntity) && !staminaBlocked && playerEntity.isSprinting() && stamina >= 1) {
            StaminaData.removeStamina(dataSaver, 0.1f);
        }

        if (isWearingSCArmor(playerEntity) && !staminaBlocked && !playerEntity.isOnGround()
                && playerEntity.getVelocity().y > 0 && !playerEntity.isBlocking()
                && !playerEntity.hasVehicle() && !playerEntity.isTouchingWater()) {
            StaminaData.removeStamina(dataSaver, 0.6f);
        }

        if (isWearingSCArmor(playerEntity) && playerEntity.isTouchingWater() && stamina >= 0.1f
                && playerEntity.age % 2 == 0) {
            StaminaData.removeStamina(dataSaver, 0.1f);
        }
    }

    private void applyStaminaEffects(ServerPlayerEntity playerEntity, int miningFatigueLevel, int slownessLevel) {
        playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, -1,
                miningFatigueLevel, false, false, false));
        playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, -1,
                slownessLevel, false, false, false));
    }

    private void removeStaminaEffects(ServerPlayerEntity playerEntity) {
        playerEntity.removeStatusEffect(StatusEffects.SLOWNESS);
        playerEntity.removeStatusEffect(StatusEffects.MINING_FATIGUE);
    }

    private boolean isHoldingSCWeapon(ServerPlayerEntity playerEntity) {
        return isSCWeapon(playerEntity.getMainHandStack()) || isSCWeapon(playerEntity.getOffHandStack());
    }

    private boolean isSCWeapon(ItemStack stack) {
        return stack.isIn(SCTags.MELEE_COMBAT_MECHANICS.getTag());
    }

    private boolean isWearingSCArmor(ServerPlayerEntity playerEntity) {
        for (ItemStack armorStack : playerEntity.getArmorItems()) {
            if (armorStack.getItem() instanceof SCUnderArmorItem) {
                return true;
            }
        }
        return false;
    }
}