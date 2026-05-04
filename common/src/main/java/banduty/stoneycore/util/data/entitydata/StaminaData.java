package banduty.stoneycore.util.data.entitydata;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.platform.Services;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class StaminaData {
    private static final ResourceLocation STAMINA_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "stamina");

    public static void setStamina(LivingEntity livingEntity, double stamina) {
        double clamped = clampStamina(livingEntity, stamina);
        AttributeInstance attribute = livingEntity.getAttribute(SCAttributes.STAMINA);

        if (attribute != null) {
            attribute.removeModifier(STAMINA_MODIFIER_ID);

            attribute.addOrUpdateTransientModifier(new AttributeModifier(
                    STAMINA_MODIFIER_ID,
                    clamped,
                    AttributeModifier.Operation.ADD_VALUE
            ));
        }

        if (livingEntity instanceof ServerPlayer serverPlayer) {
            Services.STAMINA.syncStaminaValue(clamped, serverPlayer);
        }
    }

    public static void addStamina(LivingEntity livingEntity, double amount) {
        setStamina(livingEntity, clampStamina(livingEntity, livingEntity.getAttributeValue(SCAttributes.STAMINA) + amount));
    }

    public static void removeStamina(LivingEntity livingEntity, double amount) {
        setStamina(livingEntity, clampStamina(livingEntity, livingEntity.getAttributeValue(SCAttributes.STAMINA) - amount));
    }

    public static double getStamina(LivingEntity livingEntity) {
        return livingEntity.getAttributeValue(SCAttributes.STAMINA);
    }

    private static double clampStamina(LivingEntity livingEntity, double value) {
        return Math.max(0, Math.min(value, livingEntity.getAttributeValue(SCAttributes.MAX_STAMINA)));
    }

    public static void setStaminaBlocked(IEntityDataSaver livingEntity, boolean blocked) {
        livingEntity.stoneycore$getPersistentData().putBoolean("stamina_blocked", blocked);
        if (livingEntity instanceof ServerPlayer player) {
            syncStaminaBlocked(blocked, player);
        }
    }

    public static boolean isStaminaBlocked(IEntityDataSaver livingEntity) {
        return livingEntity.stoneycore$getPersistentData().getBoolean("stamina_blocked");
    }

    public static void setStaminaUseTime(IEntityDataSaver livingEntity, int time) {
        livingEntity.stoneycore$getPersistentData().putInt("stamina_use_time", time);
    }

    public static int getStaminaUseTime(IEntityDataSaver livingEntity) {
        return livingEntity.stoneycore$getPersistentData().getInt("stamina_use_time");
    }

    public static void syncStaminaBlocked(boolean blocked, ServerPlayer player) {
        Services.STAMINA.syncStaminaBlocked(blocked, player);
    }

    public static void saveStamina(IEntityDataSaver livingEntity, double stamina) {
        livingEntity.stoneycore$getPersistentData().putDouble("stamina_value_saved", stamina);
    }

    public static void loadStamina(LivingEntity livingEntity) {
        if (!(livingEntity instanceof IEntityDataSaver saver)) return;

        double saved = saver.stoneycore$getPersistentData().contains("stamina_value_saved")
                ? saver.stoneycore$getPersistentData().getDouble("stamina_value_saved")
                : livingEntity.getAttributeValue(SCAttributes.MAX_STAMINA);

        setStamina(livingEntity, saved);
    }
}