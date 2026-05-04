package banduty.stoneycore.networking.payload;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.combat.damagetype.SCDamageType;
import banduty.stoneycore.combat.damagetype.SCDamageTypeResolver;
import banduty.stoneycore.util.WeightUtil;
import banduty.stoneycore.util.data.entitydata.StaminaData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AttackC2SPacket() implements CustomPacketPayload {

    public static final Type<AttackC2SPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "attack")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, AttackC2SPacket> STREAM_CODEC =
            StreamCodec.unit(new AttackC2SPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            ItemStack mainHandStack = player.getMainHandItem();
            SCDamageType damageType = SCDamageTypeResolver.determine(mainHandStack, player);
            double stamina = StoneyCore.getConfig().combatOptions().attackStaminaConstant();

            if (damageType == SCDamageType.SLASHING) stamina *= 0.5;
            StaminaData.removeStamina(player, stamina * WeightUtil.getWeight(player));
        }
    }
}