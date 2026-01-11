package banduty.stoneycore.util.weaponutil;

import banduty.stoneycore.util.SCDamageCalculator;
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

import java.util.Map;

public final class SCWeaponUtil {
    private static final double BACKSTAB_ANGLE_THRESHOLD = -0.5;
    private static final double RADIUS_TOLERANCE = 0.25;

    private SCWeaponUtil() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    public static double getDamageValues(SCDamageCalculator.DamageType key, Item item) {
        WeaponDefinitionData attributeData = WeaponDefinitionsStorage.getData(item);
        Map<String, Float> damageValues = attributeData.melee().damage();

        return damageValues.getOrDefault(key.name(), 0f);
    }

    public static SCDamageCalculator.DamageType calculateDamageType(ItemStack stack, int comboCount) {
        boolean bludgeoningToPiercing = getDamageValues(SCDamageCalculator.DamageType.SLASHING, stack.getItem()) == 0
                && getDamageValues(SCDamageCalculator.DamageType.PIERCING, stack.getItem()) > 0
                && getDamageValues(SCDamageCalculator.DamageType.BLUDGEONING, stack.getItem()) > 0;
        boolean isBludgeoning = NBTDataHelper.get(stack, INBTKeys.BLUDGEONING, false);
        boolean isPiercing = isPiercingWeapon(stack.getItem(), comboCount);

        if (isBludgeoning ^ bludgeoningToPiercing || WeaponDefinitionsStorage.getData(stack).melee().onlyDamageType() == SCDamageCalculator.DamageType.BLUDGEONING) {
            return SCDamageCalculator.DamageType.BLUDGEONING;
        }
        if (isPiercing || bludgeoningToPiercing) {
            return SCDamageCalculator.DamageType.PIERCING;
        }
        return SCDamageCalculator.DamageType.SLASHING;
    }

    private static boolean isPiercingWeapon(Item item, int comboCount) {
        return (WeaponDefinitionsStorage.getData(item).melee().animation() > 0 && isComboCountPiercing(item, comboCount)) ||
                WeaponDefinitionsStorage.getData(item).melee().onlyDamageType() == SCDamageCalculator.DamageType.PIERCING;
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

    public static double getMaxDistance(Item item) {
        return getRadius(item, 4);
    }

    public static double getRadius(Item item, int index) {
        WeaponDefinitionData attributeData = WeaponDefinitionsStorage.getData(item);
        Map<String, Double> radiusValues = attributeData.melee().radius();

        String key;
        switch (index) {
            case 0 -> key = "level_0";
            case 1 -> key = "level_1";
            case 2 -> key = "level_2";
            case 3 -> key = "level_3";
            case 4 -> key = "level_4";
            default -> throw new IllegalArgumentException("Invalid index: " + index);
        }

        return radiusValues.getOrDefault(key, 0.0);
    }

    public static double calculateDamage(Item item, double distance, SCDamageCalculator.DamageType key) {
        for (int i = 0; i <= 4; i++) {
            double radius = getRadius(item, i);
            if (distance < radius + RADIUS_TOLERANCE) {
                double attackDamage = getDamageValues(key, item);
                float percentage = calculatePercentageForIndex(i);
                return attackDamage * percentage;
            }
        }
        return 0.0F;
    }

    private static float calculatePercentageForIndex(int index) {
        return switch (index) {
            case 4 -> (1f/3f);
            case 3, 1 -> (2f/3f);
            case 2 -> 1.0f;
            default -> 0f;
        };
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