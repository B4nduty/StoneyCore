package banduty.stoneycore.combat.damagetype;

import banduty.stoneycore.definitions.ArmorDefinitionsStorage;
import banduty.stoneycore.combat.weapon.SCArmorUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;

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

    public static double applyArmor(LivingEntity target, double damage, SCDamageType type) {
        for (ItemStack armorStack : target.getArmorSlots()) {
            if (ArmorDefinitionsStorage.containsItem(armorStack.getItem())) {
                double resistance = getResistance(armorStack.getItem(), type);
                damage *= Math.max(1 - resistance, 0);
            }
        }
        return damage;
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
