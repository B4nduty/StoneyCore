package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.config.StoneyCoreConfig;
import banduty.stoneycore.items.armor.SCTrinketsItem;
import banduty.stoneycore.util.definitionsloader.SCMeleeWeaponDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.SCArmorDefinitionsLoader;
import banduty.stoneycore.util.itemdata.SCTags;
import banduty.stoneycore.util.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.playerdata.StaminaData;
import banduty.streq.StrEq;
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

import java.util.HashMap;
import java.util.Map;

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
                if (!SCArmorDefinitionsLoader.containsItem(armorPiece.getItem())) {
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

        if ((playerEntity.isCreative() || playerEntity.isSpectator() || StoneyCore.getConfig().maxStamina() <= 0)) {
            if (stamina < StoneyCore.getConfig().maxStamina()) StaminaData.setStamina((IEntityDataSaver) playerEntity, StoneyCore.getConfig().maxStamina());
            removeStaminaEffects(playerEntity);
            StaminaData.setStaminaBlocked((IEntityDataSaver) playerEntity, false);
        }

        if (stamina > StoneyCore.getConfig().maxStamina())
            StaminaData.setStamina((IEntityDataSaver) playerEntity, StoneyCore.getConfig().maxStamina());

        if (!playerEntity.isCreative() || !playerEntity.isSpectator()) {
            if (!StoneyCore.getConfig().getRealisticCombat() || !isUsingStamina(playerEntity) || playerEntity.isOnGround() || playerEntity.isClimbing()) handleStaminaRecovery(playerEntity, stamina);
            handleStaminaEffects(playerEntity, stamina);
        }
    }

    private void handleStaminaRecovery(ServerPlayerEntity playerEntity, float stamina) {
        double foodLevel = playerEntity.getHungerManager().getFoodLevel();
        double health = playerEntity.getHealth();
        String formula = StoneyCore.getConfig().staminaRecoveryFormula();
        Map<String, Double> variables = new HashMap<>();
        variables.put("foodLevel", foodLevel);
        variables.put("health", health);
        int ticksPerRecovery = Math.max(1, (int) StrEq.evaluate(formula, variables));

        StaminaData.addStamina((IEntityDataSaver) playerEntity, 0);
        if (playerEntity.age % ticksPerRecovery == 0 && stamina < StoneyCore.getConfig().maxStamina() && !(foodLevel == 0 && StoneyCore.getConfig().getRealisticCombat())) {
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

    private boolean isUsingStamina(ServerPlayerEntity player) {
        IEntityDataSaver dataSaver = (IEntityDataSaver) player;
        boolean staminaBlocked = StaminaData.isStaminaBlocked(dataSaver);
        boolean usingStamina = false;

        boolean wearingSCArmor = isWearingSCArmor(player);
        boolean hasSCWeapon = isSCWeapon(player.getMainHandStack());
        StoneyCoreConfig config = StoneyCore.getConfig();

        if (!staminaBlocked) {
            if (hasSCWeapon && player.isBlocking()) {
                StaminaData.removeStamina(dataSaver, config.blockingStaminaPerSecond() / 20f);
                usingStamina = true;
            }

            if (wearingSCArmor) {
                if (player.isSprinting()) {
                    StaminaData.removeStamina(dataSaver, config.sprintingStaminaPerSecond() / 20f);
                    usingStamina = true;
                }

                if (player.isSwimming()) {
                    StaminaData.removeStamina(dataSaver, config.swimmingStaminaPerSecond() / 20f);
                    usingStamina = true;
                }
            }
        }

        return usingStamina;
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

    private boolean isSCWeapon(ItemStack stack) {
        return SCMeleeWeaponDefinitionsLoader.containsItem(stack.getItem());
    }

    private boolean isWearingSCArmor(ServerPlayerEntity playerEntity) {
        for (ItemStack armorStack : playerEntity.getArmorItems()) {
            if (SCArmorDefinitionsLoader.containsItem(armorStack.getItem())) {
                return true;
            }
        }
        return false;
    }
}