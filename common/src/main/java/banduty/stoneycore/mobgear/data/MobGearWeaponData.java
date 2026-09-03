package banduty.stoneycore.mobgear.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record MobGearWeaponData(List<ResourceLocation> mobs, boolean replace) {
    public static final Codec<MobGearWeaponData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().optionalFieldOf("mobs", List.of()).forGetter(MobGearWeaponData::mobs),
            Codec.BOOL.optionalFieldOf("replace", false).forGetter(MobGearWeaponData::replace)
    ).apply(instance, MobGearWeaponData::new));
}