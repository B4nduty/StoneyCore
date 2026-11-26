package banduty.stoneycore.definitions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public record ArmorAttachmentDefinitionData(double armor, double toughness, double hungerDrainMultiplier,
                                            Map<String, Double> deflectChance, double weight, float attackSpeed, int rechargeTime, ResourceLocation visoredHelmet, List<String> incompatibleSlots) {
    public static final Codec<ArmorAttachmentDefinitionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("armor", 0.0).forGetter(ArmorAttachmentDefinitionData::armor),
            Codec.DOUBLE.optionalFieldOf("toughness", 0.0).forGetter(ArmorAttachmentDefinitionData::toughness),
            Codec.DOUBLE.optionalFieldOf("hungerDrainMultiplier", 0.0).forGetter(ArmorAttachmentDefinitionData::hungerDrainMultiplier),
            Codec.unboundedMap(Codec.STRING.xmap(String::toUpperCase, s -> s), Codec.DOUBLE).optionalFieldOf("deflectChance", Map.of()).forGetter(ArmorAttachmentDefinitionData::deflectChance),
            Codec.DOUBLE.optionalFieldOf("weight", 0.0).forGetter(ArmorAttachmentDefinitionData::weight),
            Codec.FLOAT.optionalFieldOf("attackSpeed", 0.0f).forGetter(ArmorAttachmentDefinitionData::attackSpeed),
            Codec.INT.optionalFieldOf("rechargeTime", 0).forGetter(ArmorAttachmentDefinitionData::rechargeTime),
            ResourceLocation.CODEC.optionalFieldOf("visoredHelmet", ResourceLocation.fromNamespaceAndPath("","")).forGetter(ArmorAttachmentDefinitionData::visoredHelmet),
            Codec.STRING.listOf().optionalFieldOf("incompatibleSlots", List.of()).forGetter(ArmorAttachmentDefinitionData::incompatibleSlots)
    ).apply(instance, ArmorAttachmentDefinitionData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ArmorAttachmentDefinitionData> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);
}