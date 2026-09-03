package banduty.stoneycore.mobgear.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.List;

public record MobGearArmorData(EquipmentSlot slot, List<ResourceLocation> mobs, boolean replace) {
    private static final Codec<EquipmentSlot> SLOT_CODEC = StringRepresentable.fromEnum(EquipmentSlot::values);

    public static final Codec<MobGearArmorData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SLOT_CODEC.fieldOf("slot").forGetter(MobGearArmorData::slot),
            ResourceLocation.CODEC.listOf().optionalFieldOf("mobs", List.of()).forGetter(MobGearArmorData::mobs),
            Codec.BOOL.optionalFieldOf("replace", false).forGetter(MobGearArmorData::replace)
    ).apply(instance, MobGearArmorData::new));
}