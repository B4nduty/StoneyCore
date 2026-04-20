package banduty.stoneycore.util.weaponutil;

import banduty.stoneycore.combat.melee.SCDamageType;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionData;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
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

public final class SCWeaponUtil {
    private static final double BACKSTAB_ANGLE_THRESHOLD = -0.5;
    private static final double RADIUS_TOLERANCE = 0.25;

    private SCWeaponUtil() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    public static double getDamageValues(SCDamageType type, Item item, int levelIndex) {
        WeaponDefinitionData attributeData = WeaponDefinitionsStorage.getData(item);
        if (attributeData.melee() == null) return 0f;

        Map<String, Float> levels = attributeData.melee().damage().get(type.name());
        if (levels == null) return 0f;

        String levelKey = "level_" + levelIndex;
        return levels.getOrDefault(levelKey, 0f);
    }

    public static boolean hasDamageType(SCDamageType type, Item item) {
        WeaponDefinitionData data = WeaponDefinitionsStorage.getData(item);
        if (data.melee() == null) return false;
        Map<String, Float> levels = data.melee().damage().get(type.name());
        return levels != null && levels.values().stream().anyMatch(v -> v > 0);
    }

    public static boolean isOnlyDamageType(SCDamageType type, Item item) {
        boolean hasSlashing = hasDamageType(SCDamageType.SLASHING, item);
        boolean hasPiercing = hasDamageType(SCDamageType.PIERCING, item);
        boolean hasBludgeoning = hasDamageType(SCDamageType.BLUDGEONING, item);

        return switch (type) {
            case SLASHING -> hasSlashing && !hasPiercing && !hasBludgeoning;
            case PIERCING -> hasPiercing && !hasSlashing && !hasBludgeoning;
            case BLUDGEONING -> hasBludgeoning && !hasSlashing && !hasPiercing;
        };
    }

    public static SCDamageType calculateDamageType(ItemStack stack, int comboCount) {
        Item item = stack.getItem();
        boolean hasSlashing = hasDamageType(SCDamageType.SLASHING, item);
        boolean hasPiercing = hasDamageType(SCDamageType.PIERCING, item);
        boolean hasBludgeoning = hasDamageType(SCDamageType.BLUDGEONING, item);

        boolean bludgeoningToPiercing = !hasSlashing && hasPiercing && hasBludgeoning;
        boolean isBludgeoningNBT = NBTDataHelper.get(stack, INBTKeys.BLUDGEONING, false);
        boolean isPiercingLogic = isPiercingWeapon(item, comboCount);

        if (isBludgeoningNBT ^ bludgeoningToPiercing || isOnlyDamageType(SCDamageType.BLUDGEONING, item)) {
            return SCDamageType.BLUDGEONING;
        }
        if (isPiercingLogic || bludgeoningToPiercing) {
            return SCDamageType.PIERCING;
        }
        return SCDamageType.SLASHING;
    }

    private static boolean isPiercingWeapon(Item item, int comboCount) {
        return (WeaponDefinitionsStorage.getData(item).melee().animation() > 0 && isComboCountPiercing(item, comboCount)) ||
                isOnlyDamageType(SCDamageType.PIERCING, item);
    }

    private static boolean isComboCountPiercing(Item item, int comboCount) {
        WeaponDefinitionData attributeData = WeaponDefinitionsStorage.getData(item);
        int[] piercingAnimations = attributeData.melee().piercingAnimation();
        int animation = attributeData.melee().animation();
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

    public static List<Double> getSortedDistances(Item item) {
        WeaponDefinitionData attributeData = WeaponDefinitionsStorage.getData(item);
        if (attributeData == null || attributeData.melee() == null) return Collections.emptyList();

        Map<String, Double> levels = attributeData.melee().radius();
        if (levels == null || levels.isEmpty()) return Collections.emptyList();

        return levels.values().stream()
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public static double getMaxDistance(Item item) {
        WeaponDefinitionData attributeData = WeaponDefinitionsStorage.getData(item);
        if (attributeData == null || attributeData.melee() == null) return 0.0;

        Map<String, Double> levels = attributeData.melee().radius();
        if (levels == null || levels.isEmpty()) return 0.0;

        return levels.values().stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0);
    }

    public static double getRadius(Item item, int index) {
        WeaponDefinitionData attributeData = WeaponDefinitionsStorage.getData(item);
        Map<String, Double> radiusValues = attributeData.melee().radius();

        return radiusValues.getOrDefault("level_" + index, 0.0);
    }

    public static List<Double> getSortedDamageValues(SCDamageType type, Item item) {
        WeaponDefinitionData attributeData = WeaponDefinitionsStorage.getData(item);
        if (attributeData == null || attributeData.melee() == null) return Collections.emptyList();

        Map<String, Float> levels = attributeData.melee().damage().get(type.name());
        if (levels == null || levels.isEmpty()) return Collections.emptyList();

        return levels.values().stream()
                .map(Float::doubleValue)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public static double getMaxDamage(SCDamageType scDamageType, Item item) {
        List<Double> values = getSortedDamageValues(scDamageType, item);
        return values.isEmpty() ? 0.0 : values.get(values.size() - 1);
    }

    public static double calculateDamage(Item item, double distance, SCDamageType key) {
        for (int i = 0; i <= 4; i++) {
            double radius = getRadius(item, i);
            if (distance < radius + RADIUS_TOLERANCE) {
                return getDamageValues(key, item, i);
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

        stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
    }
}