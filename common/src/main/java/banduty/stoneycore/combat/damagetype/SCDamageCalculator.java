package banduty.stoneycore.combat.damagetype;

import banduty.stoneycore.definitions.ArmorDefinitionsStorage;
import banduty.stoneycore.combat.weapon.SCArmorUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public class SCDamageCalculator {
    /**
     * Applies the vanilla (and datapack) damage enchantments of {@code weapon} — Sharpness, Smite, Bane of Arthropods,
     * Impaling, Power, etc. — to a StoneyCore damage value. Should be called on the raw weapon damage, before armor
     * resistances are applied.
     */
    public static double applyEnchantments(ServerLevel level, @Nullable ItemStack weapon, Entity target, DamageSource source, double damage) {
        if (weapon == null || weapon.isEmpty()) return damage;
        return EnchantmentHelper.modifyDamage(level, weapon, target, source, (float) damage);
    }

    /**
     * Client-safe preview of {@link #applyEnchantments} for tooltips. Only applies damage effects that have no
     * requirements (e.g. Sharpness); target-dependent ones like Smite or Bane of Arthropods are skipped, since
     * evaluating their conditions needs a server level and a target.
     */
    public static double previewEnchantments(ItemStack weapon, double damage) {
        ItemEnchantments enchantments = weapon.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchantments.isEmpty()) return damage;

        float value = (float) damage;
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            int level = entry.getIntValue();
            for (ConditionalEffect<EnchantmentValueEffect> effect : entry.getKey().value().getEffects(EnchantmentEffectComponents.DAMAGE)) {
                if (effect.requirements().isEmpty()) {
                    value = effect.effect().process(level, PREVIEW_RANDOM, value);
                }
            }
        }
        return value;
    }

    private static final RandomSource PREVIEW_RANDOM = RandomSource.create(0L);

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    /**
     * Entities whose StoneyCore armor already had its protection enchantments folded into its resistances this tick,
     * mapped to the game time of that hit. Consumed by {@link #consumeEnchantmentsApplied} from the vanilla enchantment
     * protection pass so those enchantments are not counted a second time.
     */
    private static final Map<LivingEntity, Long> ENCHANTMENTS_APPLIED = new WeakHashMap<>();

    public static double applyArmor(LivingEntity target, double damage, SCDamageType type) {
        return applyArmor(target, damage, type, null);
    }

    /**
     * Reduces {@code damage} by the resistances of each StoneyCore armor piece worn by {@code target}. When a
     * {@code source} is given, each piece's own protection enchantments (Protection, Projectile Protection, ... - any
     * enchantment with a {@code damage_protection} effect matching the source) strengthen that piece's resistance,
     * using the vanilla scale of 4% per protection point, capped at 80%.
     */
    public static double applyArmor(LivingEntity target, double damage, SCDamageType type, @Nullable DamageSource source) {
        ServerLevel level = source != null && target.level() instanceof ServerLevel serverLevel ? serverLevel : null;
        boolean wearsSCArmor = false;

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack armorStack = target.getItemBySlot(slot);
            if (!ArmorDefinitionsStorage.containsItem(armorStack.getItem())) continue;
            wearsSCArmor = true;

            double resistance = getResistance(armorStack.getItem(), type);
            if (level != null) {
                resistance = applyEnchantmentProtection(resistance, getEnchantmentProtection(level, target, armorStack, slot, source));
            }
            damage *= Math.max(1 - resistance, 0);
        }

        if (level != null && wearsSCArmor) {
            ENCHANTMENTS_APPLIED.put(target, level.getGameTime());
        }
        return damage;
    }

    /**
     * Combines a StoneyCore resistance with vanilla protection points: the enchantments reduce the damage that gets
     * through the resistance by {@code min(points, 20) / 25}, the same as vanilla magic absorption.
     */
    public static double applyEnchantmentProtection(double resistance, float protection) {
        if (protection <= 0) return resistance;
        double enchantReduction = Mth.clamp(protection, 0.0F, 20.0F) / 25.0;
        return 1 - Math.max(1 - resistance, 0) * (1 - enchantReduction);
    }

    /** Protection points granted by the enchantments of a single armor piece against {@code source}. */
    public static float getEnchantmentProtection(ServerLevel level, LivingEntity wearer, ItemStack armorStack, EquipmentSlot slot, DamageSource source) {
        MutableFloat protection = new MutableFloat(0.0F);
        ItemEnchantments enchantments = armorStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey().value();
            if (enchantment.matchingSlot(slot)) {
                enchantment.modifyDamageProtection(level, entry.getIntValue(), armorStack, wearer, source, protection);
            }
        }
        return protection.floatValue();
    }

    /** Total protection points granted by the enchantments on all StoneyCore armor pieces worn by {@code wearer}. */
    public static float getSCArmorEnchantmentProtection(ServerLevel level, LivingEntity wearer, DamageSource source) {
        float protection = 0.0F;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack armorStack = wearer.getItemBySlot(slot);
            if (ArmorDefinitionsStorage.containsItem(armorStack.getItem())) {
                protection += getEnchantmentProtection(level, wearer, armorStack, slot, source);
            }
        }
        return protection;
    }

    /**
     * Returns whether {@link #applyArmor} already applied the StoneyCore armor's protection enchantments to the hit
     * {@code entity} is currently taking (same tick), clearing the mark.
     */
    public static boolean consumeEnchantmentsApplied(LivingEntity entity) {
        Long gameTime = ENCHANTMENTS_APPLIED.remove(entity);
        return gameTime != null && gameTime == entity.level().getGameTime();
    }

    public static double getResistance(Item item, SCDamageType type) {
        if (type == null) {
            return SCArmorUtil.getResistance(SCDamageType.BLUDGEONING, item);
        }

        return switch (type) {
            case SLASHING -> SCArmorUtil.getResistance(SCDamageType.SLASHING, item);
            case PIERCING -> SCArmorUtil.getResistance(SCDamageType.PIERCING, item);
            case BLUDGEONING -> SCArmorUtil.getResistance(SCDamageType.BLUDGEONING, item);
        };
    }
}
