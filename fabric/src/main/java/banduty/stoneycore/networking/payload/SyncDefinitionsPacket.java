package banduty.stoneycore.networking.payload;

import banduty.stoneycore.networking.SCPayloadsClient;
import banduty.stoneycore.util.definitionsloader.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public record SyncDefinitionsPacket(
        Map<ResourceLocation, ArmorDefinitionData> armor,
        Map<ResourceLocation, AccessoriesDefinitionData> accessories,
        Map<ResourceLocation, LandValues> land,
        Map<ResourceLocation, SiegeEngineDefinitionData> siege_engine,
        Map<ResourceLocation, WeaponDefinitionData> weapon
) implements CustomPacketPayload {
    public static final Type<SyncDefinitionsPacket> ID = new Type<>(SCPayloadsClient.SYNC_DEFINITIONS);

    private static <T> StreamCodec<RegistryFriendlyByteBuf, Map<ResourceLocation, T>> mapCodec(com.mojang.serialization.Codec<T> codec) {
        return ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.fromCodec(codec));
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncDefinitionsPacket> CODEC = StreamCodec.composite(
            mapCodec(ArmorDefinitionData.CODEC), SyncDefinitionsPacket::armor,
            mapCodec(AccessoriesDefinitionData.CODEC), SyncDefinitionsPacket::accessories,
            mapCodec(LandValues.CODEC), SyncDefinitionsPacket::land,
            mapCodec(SiegeEngineDefinitionData.CODEC), SyncDefinitionsPacket::siege_engine,
            mapCodec(WeaponDefinitionData.CODEC), SyncDefinitionsPacket::weapon,
            SyncDefinitionsPacket::new
    );

    @Override public Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(SyncDefinitionsPacket payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            ArmorDefinitionsStorage.clearDefinitions();
            payload.armor.forEach(ArmorDefinitionsStorage::addDefinition);

            AccessoriesDefinitionsStorage.clearDefinitions();
            payload.accessories.forEach(AccessoriesDefinitionsStorage::addDefinition);

            LandDefinitionsStorage.clearDefinitions();
            payload.land.forEach(LandDefinitionsStorage::addDefinition);

            SiegeEngineDefinitionsStorage.clearDefinitions();
            payload.siege_engine.forEach(SiegeEngineDefinitionsStorage::addDefinition);

            WeaponDefinitionsStorage.clearDefinitions();
            payload.weapon.forEach(WeaponDefinitionsStorage::addDefinition);
        });
    }
}