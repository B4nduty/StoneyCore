package banduty.stoneycore.networking;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.networking.packet.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    public static SimpleChannel CHANNEL;
    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(ResourceLocation.tryBuild(StoneyCore.MOD_ID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        CHANNEL = net;

        // Server-bound packets (PLAY_TO_SERVER)
        net.messageBuilder(AttackC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(AttackC2SPacket::encode)
                .decoder(AttackC2SPacket::decode)
                .consumerMainThread(AttackC2SPacket::handle)
                .add();

        net.messageBuilder(ReloadC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(ReloadC2SPacket::encode)
                .decoder(ReloadC2SPacket::decode)
                .consumerMainThread(ReloadC2SPacket::handle)
                .add();

        net.messageBuilder(SiegeYawC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(SiegeYawC2SPacket::encode)
                .decoder(SiegeYawC2SPacket::decode)
                .consumerMainThread(SiegeYawC2SPacket::handle)
                .add();

        net.messageBuilder(ToggleVisorC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(ToggleVisorC2SPacket::encode)
                .decoder(ToggleVisorC2SPacket::decode)
                .consumerMainThread(ToggleVisorC2SPacket::handle)
                .add();

        // Client-bound packets (PLAY_TO_CLIENT)
        net.messageBuilder(StaminaBlockedS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(StaminaBlockedS2CPacket::encode)
                .decoder(StaminaBlockedS2CPacket::decode)
                .consumerMainThread(StaminaBlockedS2CPacket::handle)
                .add();

        net.messageBuilder(SyncDefinitionsPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncDefinitionsPacket::encode)
                .decoder(SyncDefinitionsPacket::decode)
                .consumerMainThread(SyncDefinitionsPacket::handle)
                .add();

        net.messageBuilder(LandTitleS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(LandTitleS2CPacket::encode)
                .decoder(LandTitleS2CPacket::decode)
                .consumerMainThread(LandTitleS2CPacket::handle)
                .add();

        net.messageBuilder(OutlineClaimS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OutlineClaimS2CPacket::encode)
                .decoder(OutlineClaimS2CPacket::decode)
                .consumerMainThread(OutlineClaimS2CPacket::handle)
                .add();

        net.messageBuilder(SiegeYawS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SiegeYawS2CPacket::encode)
                .decoder(SiegeYawS2CPacket::decode)
                .consumerMainThread(SiegeYawS2CPacket::handle)
                .add();

        net.messageBuilder(LandClientDataS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(LandClientDataS2CPacket::encode)
                .decoder(LandClientDataS2CPacket::decode)
                .consumerMainThread(LandClientDataS2CPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        CHANNEL.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToAll(MSG message) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), message);
    }
}