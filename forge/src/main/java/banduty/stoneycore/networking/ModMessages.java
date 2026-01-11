package banduty.stoneycore.networking;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.networking.packet.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class ModMessages {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(StoneyCore.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        // C2S packets
        CHANNEL.registerMessage(packetId++, AttackC2SPacket.class,
                AttackC2SPacket::encode, AttackC2SPacket::decode, AttackC2SPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(packetId++, ReloadC2SPacket.class,
                ReloadC2SPacket::encode, ReloadC2SPacket::decode, ReloadC2SPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(packetId++, SiegeYawC2SPacket.class,
                SiegeYawC2SPacket::encode, SiegeYawC2SPacket::decode, SiegeYawC2SPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(packetId++, ToggleVisorC2SPacket.class,
                ToggleVisorC2SPacket::encode, ToggleVisorC2SPacket::decode, ToggleVisorC2SPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        // S2C packets
        CHANNEL.registerMessage(packetId++, StaminaBlockedS2CPacket.class,
                StaminaBlockedS2CPacket::encode, StaminaBlockedS2CPacket::decode, StaminaBlockedS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        CHANNEL.registerMessage(packetId++, LandTitleS2CPacket.class,
                LandTitleS2CPacket::encode, LandTitleS2CPacket::decode, LandTitleS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        CHANNEL.registerMessage(packetId++, OutlineClaimS2CPacket.class,
                OutlineClaimS2CPacket::encode, OutlineClaimS2CPacket::decode, OutlineClaimS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        CHANNEL.registerMessage(packetId++, SiegeYawS2CPacket.class,
                SiegeYawS2CPacket::encode, SiegeYawS2CPacket::decode, SiegeYawS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        CHANNEL.registerMessage(packetId++, LandClientDataS2CPacket.class,
                LandClientDataS2CPacket::encode, LandClientDataS2CPacket::decode, LandClientDataS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}