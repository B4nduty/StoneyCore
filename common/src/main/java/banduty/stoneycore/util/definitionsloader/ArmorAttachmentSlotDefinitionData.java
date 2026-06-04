package banduty.stoneycore.util.definitionsloader;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Locale;

public record ArmorAttachmentSlotDefinitionData(String slot, String armor, List<ResourceLocation> items, String icon, boolean replace, String requiredSlot) {
    public static final Codec<ArmorAttachmentSlotDefinitionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("slot").forGetter(ArmorAttachmentSlotDefinitionData::slot),
            Codec.STRING.xmap(s -> s.toUpperCase(Locale.ROOT), s -> s.toLowerCase(Locale.ROOT))
                    .fieldOf("armor").forGetter(ArmorAttachmentSlotDefinitionData::armor),
            ResourceLocation.CODEC.listOf().fieldOf("items").forGetter(ArmorAttachmentSlotDefinitionData::items),
            Codec.STRING.fieldOf("icon").forGetter(ArmorAttachmentSlotDefinitionData::icon),
            Codec.BOOL.optionalFieldOf("replace", false).forGetter(ArmorAttachmentSlotDefinitionData::replace),
            Codec.STRING.optionalFieldOf("required_slot", "").forGetter(ArmorAttachmentSlotDefinitionData::requiredSlot)
    ).apply(instance, ArmorAttachmentSlotDefinitionData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ArmorAttachmentSlotDefinitionData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ArmorAttachmentSlotDefinitionData::slot,
            ByteBufCodecs.STRING_UTF8, ArmorAttachmentSlotDefinitionData::armor,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), ArmorAttachmentSlotDefinitionData::items,
            ByteBufCodecs.STRING_UTF8, ArmorAttachmentSlotDefinitionData::icon,
            ByteBufCodecs.BOOL, ArmorAttachmentSlotDefinitionData::replace,
            ByteBufCodecs.STRING_UTF8, ArmorAttachmentSlotDefinitionData::requiredSlot,
            ArmorAttachmentSlotDefinitionData::new
    );
}