package banduty.stoneycore.util.definitionsloader;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

public record ArmorAttachmentDefinitionData(double armor, double toughness, String armorSlot, double hungerDrainMultiplier,
                                            float deflectChance, double weight, ResourceLocation visoredHelmet) {
    public static final Codec<ArmorAttachmentDefinitionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("armor", 0.0).forGetter(ArmorAttachmentDefinitionData::armor),
            Codec.DOUBLE.optionalFieldOf("toughness", 0.0).forGetter(ArmorAttachmentDefinitionData::toughness),
            Codec.STRING.optionalFieldOf("armorSlot", "").forGetter(ArmorAttachmentDefinitionData::armorSlot),
            Codec.DOUBLE.optionalFieldOf("hungerDrainMultiplier", 0.0).forGetter(ArmorAttachmentDefinitionData::hungerDrainMultiplier),
            Codec.FLOAT.optionalFieldOf("deflectChance", 0.0f).forGetter(ArmorAttachmentDefinitionData::deflectChance),
            Codec.DOUBLE.optionalFieldOf("weight", 0.0).forGetter(ArmorAttachmentDefinitionData::weight),
            ResourceLocation.CODEC.optionalFieldOf("visoredHelmet", ResourceLocation.fromNamespaceAndPath("","")).forGetter(ArmorAttachmentDefinitionData::visoredHelmet)
    ).apply(instance, ArmorAttachmentDefinitionData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ArmorAttachmentDefinitionData> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public EquipmentSlot getArmorSlot() {
        if (armorSlot.isEmpty()) return null;
        try {
            return EquipmentSlot.valueOf(armorSlot.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}