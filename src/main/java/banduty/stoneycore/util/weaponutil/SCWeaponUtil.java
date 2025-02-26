package banduty.stoneycore.util.weaponutil;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.item.SCWeapon;
import banduty.stoneycore.util.SCDamageCalculator;
import net.minecraft.block.Block;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public final class SCWeaponUtil {
    private static final double BACKSTAB_ANGLE_THRESHOLD = -0.5;
    private static final double RADIUS_TOLERANCE = 0.25;
    private static final int MAX_PIERCING_ANIMATIONS = 2;

    private SCWeaponUtil() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    public static SCDamageCalculator.DamageType calculateDamageType(ItemStack stack, SCWeapon scWeapon, int comboCount) {
        boolean bludgeoningToPiercing = scWeapon.getAttackDamageValues()[0] == 0
                && scWeapon.getAttackDamageValues()[1] > 0 && scWeapon.getAttackDamageValues()[2] > 0;
        boolean isBludgeoning = stack.getOrCreateNbt().getBoolean("sc_bludgeoning");
        boolean isPiercing = isPiercingWeapon(scWeapon, comboCount);

        if (isBludgeoning || scWeapon.getOnlyDamageType() == SCDamageCalculator.DamageType.BLUDGEONING) {
            return SCDamageCalculator.DamageType.BLUDGEONING;
        }
        if (isPiercing || bludgeoningToPiercing) {
            return SCDamageCalculator.DamageType.PIERCING;
        }
        return SCDamageCalculator.DamageType.SLASHING;
    }

    private static boolean isPiercingWeapon(SCWeapon scWeapon, int comboCount) {
        return (scWeapon.getAnimation() > 0 && isComboCountPiercing(scWeapon, comboCount)) ||
                scWeapon.getOnlyDamageType() == SCDamageCalculator.DamageType.PIERCING;
    }

    private static boolean isComboCountPiercing(SCWeapon scWeapon, int comboCount) {
        int[] piercingAnimations = scWeapon.getPiercingAnimation();
        validatePiercingAnimations(piercingAnimations);

        int animationLength = scWeapon.getAnimation();
        for (int piercingAnimation : piercingAnimations) {
            if (comboCount % animationLength == piercingAnimation - 1) {
                return true;
            }
        }
        return piercingAnimations.length == animationLength;
    }

    public static float adjustDamageForBackstab(LivingEntity target, Vec3d playerPos, float damage) {
        Vec3d targetFacing = target.getRotationVec(1.0F).normalize();
        Vec3d attackDirection = playerPos.subtract(target.getPos()).normalize();
        boolean isBehind = targetFacing.dotProduct(attackDirection) < BACKSTAB_ANGLE_THRESHOLD;
        return isBehind ? damage * 2 : damage;
    }

    public static float getAttackDamage(SCWeapon scWeapon, int index) {
        float[] damageValues = scWeapon.getAttackDamageValues();
        return isValidIndex(index, damageValues.length) ? damageValues[index] : 0.0F;
    }

    public static double getMaxDistance(SCWeapon scWeapon) {
        return getRadius(scWeapon, 4);
    }

    public static double getRadius(SCWeapon scWeapon, int index) {
        double[] radiusValues = scWeapon.getRadiusValues();
        validateRadiusValues(radiusValues);

        return isValidIndex(index, radiusValues.length) ? radiusValues[index] : 0.0;
    }

    private static boolean isValidIndex(int index, int arrayLength) {
        return index >= 0 && index < arrayLength;
    }

    private static void validateRadiusValues(double[] radiusValues) {
        for (int i = 1; i < radiusValues.length; i++) {
            if (radiusValues[i - 1] > radiusValues[i]) {
                String errorMessage = String.format("Critical error: Radius values are not sorted. Index %d > Index %d. Values: %s",
                        i - 1, i, java.util.Arrays.toString(radiusValues));
                StoneyCore.LOGGER.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
        }
    }

    private static void validatePiercingAnimations(int[] piercingAnimations) {
        if (piercingAnimations.length > MAX_PIERCING_ANIMATIONS) {
            String errorMessage = "Critical error: Piercing Animations Index exceeds maximum allowed value of " + MAX_PIERCING_ANIMATIONS;
            StoneyCore.LOGGER.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public static float calculateDamage(SCWeapon scWeapon, double distance, SCDamageCalculator.DamageType damageType) {
        for (int i = 0; i <= 4; i++) {
            double radius = getRadius(scWeapon, i);
            if (distance < radius + RADIUS_TOLERANCE) {
                float attackDamage = getAttackDamage(scWeapon, damageType.getIndex());
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

    public static void replantCrop(World world, BlockPos pos, CropBlock cropBlock, PlayerEntity player, ItemStack stack, Hand hand) {
        ItemStack seedStack = new ItemStack(cropBlock.asItem());
        if (!seedStack.isEmpty()) {
            world.setBlockState(pos, cropBlock.getDefaultState(), Block.NOTIFY_ALL);
            world.emitGameEvent(player, GameEvent.BLOCK_PLACE, pos);

            if (!player.getAbilities().creativeMode) {
                seedStack.decrement(1);
            }
        }

        stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
    }
}