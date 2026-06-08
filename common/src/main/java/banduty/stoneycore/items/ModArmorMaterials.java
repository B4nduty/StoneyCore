package banduty.stoneycore.items;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.platform.Services;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public final class ModArmorMaterials {
    private ModArmorMaterials() {}
    public static final Holder<ArmorMaterial> CROWN = register("crown", Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
        map.put(ArmorItem.Type.BOOTS, 2);
        map.put(ArmorItem.Type.LEGGINGS, 5);
        map.put(ArmorItem.Type.CHESTPLATE, 6);
        map.put(ArmorItem.Type.HELMET, 2);
    }), 12, SoundEvents.ARMOR_EQUIP_GOLD, 0f, 0f, () -> Ingredient.of(Items.GOLD_INGOT));

    private static Holder<ArmorMaterial> register(String name, EnumMap<ArmorItem.Type, Integer> defense, int enchantmentValue, Holder<SoundEvent> equipSound, float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient) {
        List<ArmorMaterial.Layer> layers = List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, name), "", false));
        ArmorMaterial material = new ArmorMaterial(defense, enchantmentValue, equipSound, repairIngredient, layers, toughness, knockbackResistance);
        return Services.PLATFORM.registerHolder(Registries.ARMOR_MATERIAL, name, () -> material);
    }

    public static void init() {
        StoneyCore.LOG.info("Registering Mod Armor Materials for " + StoneyCore.MOD_ID);
    }
}