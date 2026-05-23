package banduty.stoneycore.networking.payload;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.util.definitionsloader.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record SyncDefinitionsPacket(
        Map<ResourceLocation, ArmorDefinitionData> armor,
        Map<ResourceLocation, ArmorAttachmentDefinitionData> armorAttachment,
        Map<ResourceLocation, LandValues> land,
        Map<ResourceLocation, SiegeEngineDefinitionData> siege_engine,
        Map<ResourceLocation, WeaponDefinitionData> weapon
) implements CustomPacketPayload {

    public static final Type<SyncDefinitionsPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "sync_definitions")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncDefinitionsPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ArmorDefinitionData.STREAM_CODEC), SyncDefinitionsPacket::armor,
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ArmorAttachmentDefinitionData.STREAM_CODEC), SyncDefinitionsPacket::armorAttachment,
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, LandValues.STREAM_CODEC), SyncDefinitionsPacket::land,
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, SiegeEngineDefinitionData.STREAM_CODEC), SyncDefinitionsPacket::siege_engine,
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, WeaponDefinitionData.STREAM_CODEC), SyncDefinitionsPacket::weapon,
            SyncDefinitionsPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext context) {
        ArmorDefinitionsStorage.clearDefinitions();
        this.armor.forEach(ArmorDefinitionsStorage::addDefinition);

        ArmorAttachmentDefinitionsStorage.clearDefinitions();
        this.armorAttachment.forEach(ArmorAttachmentDefinitionsStorage::addDefinition);

        LandDefinitionsStorage.clearDefinitions();
        this.land.forEach(LandDefinitionsStorage::addDefinition);

        SiegeEngineDefinitionsStorage.clearDefinitions();
        this.siege_engine.forEach(SiegeEngineDefinitionsStorage::addDefinition);

        WeaponDefinitionsStorage.clearDefinitions();
        this.weapon.forEach(WeaponDefinitionsStorage::addDefinition);
    }
}