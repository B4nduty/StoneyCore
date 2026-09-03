package banduty.stoneycore.mobgear.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.List;

public record MobGearAttachmentData(EquipmentSlot slot, List<ResourceLocation> mobs, boolean replace) {
    private static final Codec<EquipmentSlot> SLOT_CODEC = StringRepresentable.fromEnum(EquipmentSlot::values);

    public static final Codec<MobGearAttachmentData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SLOT_CODEC.fieldOf("slot").forGetter(MobGearAttachmentData::slot),
            ResourceLocation.CODEC.listOf().optionalFieldOf("mobs", List.of()).forGetter(MobGearAttachmentData::mobs),
            Codec.BOOL.optionalFieldOf("replace", false).forGetter(MobGearAttachmentData::replace)
    ).apply(instance, MobGearAttachmentData::new));
}