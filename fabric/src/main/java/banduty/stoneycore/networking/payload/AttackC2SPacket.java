package banduty.stoneycore.networking.payload;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.combat.damagetype.SCDamageType;
import banduty.stoneycore.combat.damagetype.SCDamageTypeResolver;
import banduty.stoneycore.networking.SCPayloads;
import banduty.stoneycore.util.WeightUtil;
import banduty.stoneycore.util.data.entitydata.StaminaData;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record AttackC2SPacket() implements CustomPacketPayload {
    public static final Type<AttackC2SPacket> ID = new Type<>(SCPayloads.ATTACK_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, AttackC2SPacket> CODEC = StreamCodec.unit(new AttackC2SPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(AttackC2SPacket payload, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        ItemStack mainHandStack = player.getMainHandItem();
        SCDamageType damageType = SCDamageTypeResolver.determine(mainHandStack, player);
        double stamina = StoneyCore.getConfig().combatOptions().attackStaminaConstant();

        if (damageType == SCDamageType.SLASHING) stamina *= 0.5;
        StaminaData.removeStamina(player, stamina * WeightUtil.getWeight(player));
    }
}