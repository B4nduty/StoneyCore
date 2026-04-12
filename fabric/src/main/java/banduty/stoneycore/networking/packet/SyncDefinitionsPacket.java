package banduty.stoneycore.networking.packet;

import banduty.stoneycore.util.definitionsloader.*;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class SyncDefinitionsPacket {
    public static void receive(Minecraft client, ClientPacketListener handler,
                               FriendlyByteBuf buf, PacketSender responseSender) {
        Map<ResourceLocation, ArmorDefinitionData> armor = buf.readMap(
                FriendlyByteBuf::readResourceLocation,
                b -> b.readJsonWithCodec(ArmorDefinitionData.CODEC)
        );
        Map<ResourceLocation, AccessoriesDefinitionData> accessories = buf.readMap(
                FriendlyByteBuf::readResourceLocation,
                b -> b.readJsonWithCodec(AccessoriesDefinitionData.CODEC)
        );
        Map<ResourceLocation, LandValues> land = buf.readMap(
                FriendlyByteBuf::readResourceLocation,
                b -> b.readJsonWithCodec(LandValues.CODEC)
        );
        Map<ResourceLocation, SiegeEngineDefinitionData> siege_engine = buf.readMap(
                FriendlyByteBuf::readResourceLocation,
                b -> b.readJsonWithCodec(SiegeEngineDefinitionData.CODEC)
        );
        Map<ResourceLocation, WeaponDefinitionData> weapon = buf.readMap(
                FriendlyByteBuf::readResourceLocation,
                b -> b.readJsonWithCodec(WeaponDefinitionData.CODEC)
        );
        if (client.player != null) {
            ArmorDefinitionsStorage.clearDefinitions();
            armor.forEach(ArmorDefinitionsStorage::addDefinition);

            AccessoriesDefinitionsStorage.clearDefinitions();
            accessories.forEach(AccessoriesDefinitionsStorage::addDefinition);

            LandDefinitionsStorage.clearDefinitions();
            land.forEach(LandDefinitionsStorage::addDefinition);

            SiegeEngineDefinitionsStorage.clearDefinitions();
            siege_engine.forEach(SiegeEngineDefinitionsStorage::addDefinition);

            WeaponDefinitionsStorage.clearDefinitions();
            weapon.forEach(WeaponDefinitionsStorage::addDefinition);
        }
    }

    private static <T> Map<ResourceLocation, T> readMap(FriendlyByteBuf buf, com.mojang.serialization.Codec<T> codec) {
        int size = buf.readInt();
        Map<ResourceLocation, T> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            map.put(buf.readResourceLocation(), buf.readJsonWithCodec(codec));
        }
        return map;
    }
}
