package banduty.stoneycore.util.definitionsloader;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Locale;

public record ArmorAttachmentSlotDefinitionData(String slot, String armor, List<ResourceLocation> items, String icon,
                                                boolean replace, String requiredSlot, List<String> protectedSlots) {
    public static final Codec<ArmorAttachmentSlotDefinitionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("slot", "").forGetter(ArmorAttachmentSlotDefinitionData::slot),
            Codec.STRING.xmap(s -> s.toUpperCase(Locale.ROOT), s -> s.toLowerCase(Locale.ROOT))
                    .optionalFieldOf("armor", "").forGetter(ArmorAttachmentSlotDefinitionData::armor),
            ResourceLocation.CODEC.listOf().fieldOf("items").forGetter(ArmorAttachmentSlotDefinitionData::items),
            Codec.STRING.optionalFieldOf("icon", "").forGetter(ArmorAttachmentSlotDefinitionData::icon),
            Codec.BOOL.optionalFieldOf("replace", false).forGetter(ArmorAttachmentSlotDefinitionData::replace),
            Codec.STRING.optionalFieldOf("required_slot", "").forGetter(ArmorAttachmentSlotDefinitionData::requiredSlot),
            Codec.STRING.listOf().optionalFieldOf("protectedSlots", List.of()).forGetter(ArmorAttachmentSlotDefinitionData::protectedSlots)
    ).apply(instance, ArmorAttachmentSlotDefinitionData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ArmorAttachmentSlotDefinitionData> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, value.slot());
                ByteBufCodecs.STRING_UTF8.encode(buf, value.armor());
                ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, value.items());
                ByteBufCodecs.STRING_UTF8.encode(buf, value.icon());
                ByteBufCodecs.BOOL.encode(buf, value.replace());
                ByteBufCodecs.STRING_UTF8.encode(buf, value.requiredSlot());
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).encode(buf, value.protectedSlots());
            },
            buf -> new ArmorAttachmentSlotDefinitionData(
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).decode(buf)
            )
    );
}