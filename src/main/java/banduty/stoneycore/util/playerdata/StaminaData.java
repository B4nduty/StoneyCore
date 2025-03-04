package banduty.stoneycore.util.playerdata;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.networking.ModMessages;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class StaminaData {
    private static final String STAMINA_KEY = "stamina";
    private static final String STAMINA_BLOCKED_KEY = "stamina_blocked";

    public static void setStamina(IEntityDataSaver player, float stamina) {
        NbtCompound nbt = player.stoneycore$getPersistentData();
        nbt.putFloat(STAMINA_KEY, stamina);
        syncStamina(stamina, (ServerPlayerEntity) player);
    }

    public static void addStamina(IEntityDataSaver player, float amount) {
        NbtCompound nbt = player.stoneycore$getPersistentData();
        float currentStamina = nbt.getFloat(STAMINA_KEY);
        float newStamina = clampStamina(currentStamina + amount);
        nbt.putFloat(STAMINA_KEY, newStamina);
        syncStamina(newStamina, (ServerPlayerEntity) player);
    }

    public static void removeStamina(IEntityDataSaver player, float amount) {
        NbtCompound nbt = player.stoneycore$getPersistentData();
        float currentStamina = nbt.getFloat(STAMINA_KEY);
        float newStamina = clampStamina(currentStamina - amount);
        nbt.putFloat(STAMINA_KEY, newStamina);
        syncStamina(newStamina, (ServerPlayerEntity) player);
    }

    public static float getStamina(IEntityDataSaver player) {
        return player.stoneycore$getPersistentData().getFloat(STAMINA_KEY);
    }

    private static float clampStamina(float value) {
        return Math.max(0, Math.min(value, StoneyCore.getConfig().maxStamina()));
    }

    public static void setStaminaBlocked(IEntityDataSaver player, boolean blocked) {
        NbtCompound nbt = player.stoneycore$getPersistentData();
        nbt.putBoolean(STAMINA_BLOCKED_KEY, blocked);
        syncStaminaBlocked(blocked, (ServerPlayerEntity) player);
    }

    public static boolean isStaminaBlocked(IEntityDataSaver player) {
        return player.stoneycore$getPersistentData().getBoolean(STAMINA_BLOCKED_KEY);
    }

    public static void syncStamina(float stamina, ServerPlayerEntity player) {
        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeFloat(stamina);
        ServerPlayNetworking.send(player, ModMessages.STAMINA_FLOAT_ID, buffer);
    }

    public static void syncStaminaBlocked(boolean blocked, ServerPlayerEntity player) {
        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeBoolean(blocked);
        ServerPlayNetworking.send(player, ModMessages.STAMINA_BLOCKED_ID, buffer);
    }
}