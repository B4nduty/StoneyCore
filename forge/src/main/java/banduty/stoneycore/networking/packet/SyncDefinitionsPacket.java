package banduty.stoneycore.networking.packet;

import banduty.stoneycore.util.definitionsloader.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public record SyncDefinitionsPacket(Map<ResourceLocation, ArmorDefinitionData> armor,
                                    Map<ResourceLocation, AccessoriesDefinitionData> accessories,
                                    Map<ResourceLocation, LandValues> land,
                                    Map<ResourceLocation, SiegeEngineDefinitionData> siege_engine,
                                    Map<ResourceLocation, WeaponDefinitionData> weapon) {

    public static void handle(SyncDefinitionsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                ArmorDefinitionsStorage.clearDefinitions();
                msg.armor().forEach(ArmorDefinitionsStorage::addDefinition);

                AccessoriesDefinitionsStorage.clearDefinitions();
                msg.accessories().forEach(AccessoriesDefinitionsStorage::addDefinition);

                LandDefinitionsStorage.clearDefinitions();
                msg.land().forEach(LandDefinitionsStorage::addDefinition);

                SiegeEngineDefinitionsStorage.clearDefinitions();
                msg.siege_engine().forEach(SiegeEngineDefinitionsStorage::addDefinition);

                WeaponDefinitionsStorage.clearDefinitions();
                msg.weapon().forEach(WeaponDefinitionsStorage::addDefinition);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeMap(armor, FriendlyByteBuf::writeResourceLocation, (buf, data) -> buf.writeJsonWithCodec(ArmorDefinitionData.CODEC, data));
        buffer.writeMap(accessories, FriendlyByteBuf::writeResourceLocation, (buf, data) -> buf.writeJsonWithCodec(AccessoriesDefinitionData.CODEC, data));
        buffer.writeMap(land, FriendlyByteBuf::writeResourceLocation, (buf, data) -> buf.writeJsonWithCodec(LandValues.CODEC, data));
        buffer.writeMap(siege_engine, FriendlyByteBuf::writeResourceLocation, (buf, data) -> buf.writeJsonWithCodec(SiegeEngineDefinitionData.CODEC, data));
        buffer.writeMap(weapon, FriendlyByteBuf::writeResourceLocation, (buf, data) -> buf.writeJsonWithCodec(WeaponDefinitionData.CODEC, data));
    }

    public static SyncDefinitionsPacket decode(FriendlyByteBuf buffer) {
        return new SyncDefinitionsPacket(
                buffer.readMap(FriendlyByteBuf::readResourceLocation, buf -> buf.readJsonWithCodec(ArmorDefinitionData.CODEC)),
                buffer.readMap(FriendlyByteBuf::readResourceLocation, buf -> buf.readJsonWithCodec(AccessoriesDefinitionData.CODEC)),
                buffer.readMap(FriendlyByteBuf::readResourceLocation, buf -> buf.readJsonWithCodec(LandValues.CODEC)),
                buffer.readMap(FriendlyByteBuf::readResourceLocation, buf -> buf.readJsonWithCodec(SiegeEngineDefinitionData.CODEC)),
                buffer.readMap(FriendlyByteBuf::readResourceLocation, buf -> buf.readJsonWithCodec(WeaponDefinitionData.CODEC))
        );
    }
}