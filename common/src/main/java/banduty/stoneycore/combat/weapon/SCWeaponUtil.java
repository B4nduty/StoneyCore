package banduty.stoneycore.combat.weapon;

import banduty.stoneycore.combat.damagetype.SCDamageType;
import banduty.stoneycore.data.SCDataComponents;
import banduty.stoneycore.definitions.WeaponDefinitionData;
import banduty.stoneycore.definitions.WeaponDefinitionsStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Melee weapon stat helpers. Every query has an {@link ItemStack} overload, which honours the stack's
 * {@link SCDataComponents#WEAPON_DEFINITION} override, and an {@link Item} overload, which only sees the item's
 * own definition. Prefer the stack overloads whenever the stack is available.
 */
public final class SCWeaponUtil {
    private static final double BACKSTAB_ANGLE_THRESHOLD = -0.5;
    private static final double RADIUS_TOLERANCE = 0.25;

    private SCWeaponUtil() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    // ------------------------------------------------------------------ damage values

    public static double getDamageValues(SCDamageType type, ItemStack stack, int levelIndex) {
        return getDamageValues(type, WeaponDefinitionsStorage.getData(stack), levelIndex);
    }

    public static double getDamageValues(SCDamageType type, Item item, int levelIndex) {
        return getDamageValues(type, WeaponDefinitionsStorage.getData(item), levelIndex);
    }

    private static double getDamageValues(SCDamageType type, WeaponDefinitionData data, int levelIndex) {
        if (data.melee() == null) return 0f;

        Map<String, Float> levels = data.melee().damage().get(type.name());
        if (levels == null) return 0f;

        String levelKey = "level_" + levelIndex;
        return levels.getOrDefault(levelKey, 0f);
    }

    // ------------------------------------------------------------------ damage types

    public static boolean hasDamageType(SCDamageType type, ItemStack stack) {
        return hasDamageType(type, WeaponDefinitionsStorage.getData(stack));
    }

    public static boolean hasDamageType(SCDamageType type, Item item) {
        return hasDamageType(type, WeaponDefinitionsStorage.getData(item));
    }

    private static boolean hasDamageType(SCDamageType type, WeaponDefinitionData data) {
        if (data.melee() == null) return false;
        Map<String, Float> levels = data.melee().damage().get(type.name());
        return levels != null && levels.values().stream().anyMatch(v -> v > 0);
    }

    public static boolean isOnlyDamageType(SCDamageType type, ItemStack stack) {
        return isOnlyDamageType(type, WeaponDefinitionsStorage.getData(stack));
    }

    public static boolean isOnlyDamageType(SCDamageType type, Item item) {
        return isOnlyDamageType(type, WeaponDefinitionsStorage.getData(item));
    }

    private static boolean isOnlyDamageType(SCDamageType type, WeaponDefinitionData data) {
        boolean hasSlashing = hasDamageType(SCDamageType.SLASHING, data);
        boolean hasPiercing = hasDamageType(SCDamageType.PIERCING, data);
        boolean hasBludgeoning = hasDamageType(SCDamageType.BLUDGEONING, data);

        return switch (type) {
            case SLASHING -> hasSlashing && !hasPiercing && !hasBludgeoning;
            case PIERCING -> hasPiercing && !hasSlashing && !hasBludgeoning;
            case BLUDGEONING -> hasBludgeoning && !hasSlashing && !hasPiercing;
        };
    }

    public static SCDamageType calculateDamageType(ItemStack stack, int comboCount) {
        WeaponDefinitionData data = WeaponDefinitionsStorage.getData(stack);
        boolean hasSlashing = hasDamageType(SCDamageType.SLASHING, data);
        boolean hasPiercing = hasDamageType(SCDamageType.PIERCING, data);
        boolean hasBludgeoning = hasDamageType(SCDamageType.BLUDGEONING, data);

        boolean bludgeoningToPiercing = !hasSlashing && hasPiercing && hasBludgeoning;
        boolean isBludgeoningNBT = Boolean.TRUE.equals(stack.get(SCDataComponents.BLUDGEONING.get()));
        boolean isPiercingLogic = isPiercingWeapon(data, comboCount);

        if (isBludgeoningNBT ^ bludgeoningToPiercing || isOnlyDamageType(SCDamageType.BLUDGEONING, data)) {
            return SCDamageType.BLUDGEONING;
        }
        if (isPiercingLogic || bludgeoningToPiercing) {
            return SCDamageType.PIERCING;
        }
        return SCDamageType.SLASHING;
    }

    private static boolean isPiercingWeapon(WeaponDefinitionData data, int comboCount) {
        return (data.melee() != null && data.melee().animation() > 0 && isComboCountPiercing(data, comboCount)) ||
                isOnlyDamageType(SCDamageType.PIERCING, data);
    }

    /** Whether the attack at this combo count is one of the weapon's piercing animations. */
    public static boolean isComboCountPiercing(ItemStack stack, int comboCount) {
        return isComboCountPiercing(WeaponDefinitionsStorage.getData(stack), comboCount);
    }

    private static boolean isComboCountPiercing(WeaponDefinitionData data, int comboCount) {
        if (data.melee() == null) return false;
        int[] piercingAnimations = data.melee().piercingAnimation();
        int animation = data.melee().animation();
        boolean piercing = false;

        if (animation > 0) {
            for (int piercingAnimation : piercingAnimations) {
                if (comboCount % animation == piercingAnimation - 1) {
                    piercing = true;
                    break;
                }
            }

            if (piercingAnimations.length == animation) piercing = true;
        }
        return piercing;
    }

    public static double adjustDamageForBackstab(LivingEntity target, Vec3 playerPos, double damage) {
        Vec3 targetFacing = target.getViewVector(1.0F).normalize();
        Vec3 attackDirection = playerPos.subtract(target.position()).normalize();
        boolean isBehind = targetFacing.dot(attackDirection) < BACKSTAB_ANGLE_THRESHOLD;
        return isBehind ? damage * 2 : damage;
    }

    // ------------------------------------------------------------------ reach

    public static List<Double> getSortedDistances(ItemStack stack) {
        return getSortedDistances(WeaponDefinitionsStorage.getData(stack));
    }

    public static List<Double> getSortedDistances(Item item) {
        return getSortedDistances(WeaponDefinitionsStorage.getData(item));
    }

    private static List<Double> getSortedDistances(WeaponDefinitionData data) {
        if (data == null || data.melee() == null) return Collections.emptyList();

        Map<String, Double> levels = data.melee().radius();
        if (levels == null || levels.isEmpty()) return Collections.emptyList();

        return levels.values().stream()
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public static double getMaxDistance(ItemStack stack) {
        return getMaxDistance(WeaponDefinitionsStorage.getData(stack));
    }

    public static double getMaxDistance(Item item) {
        return getMaxDistance(WeaponDefinitionsStorage.getData(item));
    }

    private static double getMaxDistance(WeaponDefinitionData data) {
        if (data == null || data.melee() == null) return 0.0;

        Map<String, Double> levels = data.melee().radius();
        if (levels == null || levels.isEmpty()) return 0.0;

        return levels.values().stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0);
    }

    public static double getRadius(ItemStack stack, int index) {
        return getRadius(WeaponDefinitionsStorage.getData(stack), index);
    }

    public static double getRadius(Item item, int index) {
        return getRadius(WeaponDefinitionsStorage.getData(item), index);
    }

    private static double getRadius(WeaponDefinitionData data, int index) {
        if (data.melee() == null) return 0.0;
        Map<String, Double> radiusValues = data.melee().radius();

        return radiusValues.getOrDefault("level_" + index, 0.0);
    }

    // ------------------------------------------------------------------ damage lookups

    public static List<Double> getSortedDamageValues(SCDamageType type, ItemStack stack) {
        return getSortedDamageValues(type, WeaponDefinitionsStorage.getData(stack));
    }

    public static List<Double> getSortedDamageValues(SCDamageType type, Item item) {
        return getSortedDamageValues(type, WeaponDefinitionsStorage.getData(item));
    }

    private static List<Double> getSortedDamageValues(SCDamageType type, WeaponDefinitionData data) {
        if (data == null || data.melee() == null) return Collections.emptyList();

        Map<String, Float> levels = data.melee().damage().get(type.name());
        if (levels == null || levels.isEmpty()) return Collections.emptyList();

        return levels.values().stream()
                .map(Float::doubleValue)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public static double getMaxDamage(SCDamageType scDamageType, ItemStack stack) {
        return getMaxDamage(scDamageType, WeaponDefinitionsStorage.getData(stack));
    }

    public static double getMaxDamage(SCDamageType scDamageType, Item item) {
        return getMaxDamage(scDamageType, WeaponDefinitionsStorage.getData(item));
    }

    private static double getMaxDamage(SCDamageType scDamageType, WeaponDefinitionData data) {
        List<Double> values = getSortedDamageValues(scDamageType, data);
        return values.isEmpty() ? 0.0 : values.getLast();
    }

    public static double calculateDamage(ItemStack stack, double distance, SCDamageType key) {
        return calculateDamage(WeaponDefinitionsStorage.getData(stack), distance, key);
    }

    public static double calculateDamage(Item item, double distance, SCDamageType key) {
        return calculateDamage(WeaponDefinitionsStorage.getData(item), distance, key);
    }

    private static double calculateDamage(WeaponDefinitionData data, double distance, SCDamageType key) {
        if (data.melee() == null) return 0.0F;
        for (int i = 0; i <= data.melee().radius().size(); i++) {
            double radius = getRadius(data, i);
            if (distance < radius + RADIUS_TOLERANCE) {
                return getDamageValues(key, data, i);
            }
        }
        return 0.0F;
    }

    public static void replantCrop(Level level, BlockPos pos, CropBlock cropBlock, Player player, ItemStack stack, InteractionHand hand) {
        ItemStack seedStack = new ItemStack(cropBlock.asItem());
        if (!seedStack.isEmpty()) {
            level.setBlock(pos, cropBlock.defaultBlockState(), Block.UPDATE_ALL);
            level.gameEvent(player, GameEvent.BLOCK_PLACE, pos);

            if (!player.isCreative()) {
                seedStack.shrink(1);
            }
        }

        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
    }
}
