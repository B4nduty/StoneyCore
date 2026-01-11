package banduty.stoneycore.util.data.playerdata;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.platform.Services;
import banduty.stoneycore.util.ModifiersHelper;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

public class StaminaData {
    private static final UUID GENERIC_STAMINA_MODIFIER_ID = UUID.randomUUID();

    public static void setStamina(LivingEntity livingEntity, double stamina) {
        ModifiersHelper.updateModifier(livingEntity.getAttribute(Services.ATTRIBUTES.getStamina()),
                new AttributeModifier(GENERIC_STAMINA_MODIFIER_ID,
                        StoneyCore.MOD_ID + ":stamina", clampStamina(livingEntity, stamina), AttributeModifier.Operation.ADDITION)
        );
    }

    public static void addStamina(LivingEntity livingEntity, double amount) {
        setStamina(livingEntity, clampStamina(livingEntity, livingEntity.getAttributeValue(Services.ATTRIBUTES.getStamina()) + amount));
    }

    public static void removeStamina(LivingEntity livingEntity, double amount) {
        setStamina(livingEntity, clampStamina(livingEntity, livingEntity.getAttributeValue(Services.ATTRIBUTES.getStamina()) - amount));
    }

    public static double getStamina(LivingEntity livingEntity) {
        return livingEntity.getAttributeValue(Services.ATTRIBUTES.getStamina());
    }

    private static double clampStamina(LivingEntity livingEntity, double value) {
        return Math.max(0, Math.min(value, livingEntity.getAttributeValue(Services.ATTRIBUTES.getMaxStamina())));
    }

    public static void setStaminaBlocked(IEntityDataSaver livingEntity, boolean blocked) {
        NBTDataHelper.set(livingEntity, PDKeys.STAMINA_BLOCKED, blocked);
        syncStaminaBlocked(blocked, (ServerPlayer) livingEntity);
    }

    public static boolean isStaminaBlocked(IEntityDataSaver livingEntity) {
        return NBTDataHelper.get(livingEntity, PDKeys.STAMINA_BLOCKED, false);
    }

    public static void setStaminaUseTime(IEntityDataSaver livingEntity, int time) {
        NBTDataHelper.set(livingEntity, PDKeys.STAMINA_USE_TIME, time);
    }

    public static int getStaminaUseTime(IEntityDataSaver player) {
        return NBTDataHelper.get(player, PDKeys.STAMINA_USE_TIME, 0);
    }

    public static void syncStaminaBlocked(boolean blocked, ServerPlayer player) {
        Services.STAMINA.syncStaminaBlocked(blocked, player);
    }

    public static void saveStamina(IEntityDataSaver livingEntity, double stamina) {
        NBTDataHelper.set(livingEntity, PDKeys.STAMINA_VALUE_SAVED, stamina);
    }

    public static void loadStamina(LivingEntity livingEntity) {
        if (!(livingEntity instanceof IEntityDataSaver iEntityDataSaver)) return;
        NBTDataHelper.set(iEntityDataSaver, PDKeys.STAMINA_VALUE_SAVED, NBTDataHelper.get(iEntityDataSaver, PDKeys.STAMINA_VALUE_SAVED, livingEntity.getAttributeValue(Services.ATTRIBUTES.getMaxStamina())));
    }
}