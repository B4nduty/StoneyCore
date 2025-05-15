package banduty.stoneycore.util.playerdata;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.util.ModifiersHelper;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class StaminaData {
    private static final UUID GENERIC_STAMINA_MODIFIER_ID = UUID.randomUUID();
    private static final String STAMINA_BLOCKED_KEY = "stamina_blocked";

    public static void setStamina(LivingEntity livingEntity, double stamina) {
        ModifiersHelper.updateModifier(livingEntity.getAttributeInstance(SCAttributes.STAMINA),
                new EntityAttributeModifier(GENERIC_STAMINA_MODIFIER_ID,
                        StoneyCore.MOD_ID + ":stamina", clampStamina(livingEntity, stamina), EntityAttributeModifier.Operation.ADDITION)
        );
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
        NbtCompound nbt = livingEntity.stoneycore$getPersistentData();
        nbt.putBoolean(STAMINA_BLOCKED_KEY, blocked);
        syncStaminaBlocked(blocked, (ServerPlayerEntity) livingEntity);
    }

    public static boolean isStaminaBlocked(IEntityDataSaver livingEntity) {
        return livingEntity.stoneycore$getPersistentData().getBoolean(STAMINA_BLOCKED_KEY);
    }

    public static void syncStaminaBlocked(boolean blocked, ServerPlayerEntity player) {
        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeBoolean(blocked);
        ServerPlayNetworking.send(player, ModMessages.STAMINA_BLOCKED_ID, buffer);
    }
}