package banduty.stoneycore.util.playerdata;

import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.util.SharedParameters;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class StaminaData {
    public static void setStaminaBlocked(IEntityDataSaver player, boolean blocked) {
        NbtCompound nbt = player.stoneycore$getPersistentData();
        nbt.putBoolean("stamina_blocked", blocked);
        syncStaminaBlocked(blocked, (ServerPlayerEntity) player);
    }

    public static void addStamina(IEntityDataSaver player, int amount) {
        NbtCompound nbt = player.stoneycore$getPersistentData();
        int currentStamina = nbt.getInt("stamina_int");

        currentStamina = Math.min(currentStamina + amount, SharedParameters.TOTAL_STAMINA);

        nbt.putInt("stamina_int", currentStamina);
        if (player instanceof ServerPlayerEntity) {
            syncStamina(currentStamina, (ServerPlayerEntity) player);
        }
    }

    public static void removeStamina(IEntityDataSaver player, int amount) {
        NbtCompound nbt = player.stoneycore$getPersistentData();
        int currentStamina = nbt.getInt("stamina_int");

        currentStamina = Math.max(currentStamina - amount, 0);

        nbt.putInt("stamina_int", currentStamina);
        syncStamina(currentStamina, (ServerPlayerEntity) player);
    }

    public static void syncStamina(int stamina, ServerPlayerEntity player) {
        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeInt(stamina);
        ServerPlayNetworking.send(player, ModMessages.STAMINA_INT_ID, buffer);
    }

    public static void syncStaminaBlocked(boolean blocked, ServerPlayerEntity player) {
        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeBoolean(blocked);
        ServerPlayNetworking.send(player, ModMessages.STAMINA_BLOCKED_ID, buffer);
    }
}