package banduty.stoneycore.util.weaponutil;

import banduty.stoneycore.util.SCDamageCalculator;
import banduty.stoneycore.util.definitionsloader.SCMeleeWeaponDefinitionsLoader;
import net.minecraft.block.Block;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.Map;

public final class SCWeaponUtil {
    private static final double BACKSTAB_ANGLE_THRESHOLD = -0.5;
    private static final double RADIUS_TOLERANCE = 0.25;

    private SCWeaponUtil() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    public static float getDamageValues(String key, Item item) {
        SCMeleeWeaponDefinitionsLoader.DefinitionData attributeData = SCMeleeWeaponDefinitionsLoader.getData(item);
        Map<String, Float> damageValues = attributeData.damage();

        return damageValues.getOrDefault(key, 0f);
    }

    public static SCDamageCalculator.DamageType calculateDamageType(ItemStack stack, Item item, int comboCount) {
        boolean bludgeoningToPiercing = getDamageValues(SCDamageCalculator.DamageType.SLASHING.name(), stack.getItem()) == 0
                && getDamageValues(SCDamageCalculator.DamageType.PIERCING.name(), stack.getItem()) > 0
                && getDamageValues(SCDamageCalculator.DamageType.BLUDGEONING.name(), stack.getItem()) > 0;
        boolean isBludgeoning = stack.getOrCreateNbt().getBoolean("sc_bludgeoning");
        boolean isPiercing = isPiercingWeapon(item, comboCount);

        if (isBludgeoning || SCMeleeWeaponDefinitionsLoader.getData(item).onlyDamageType() == SCDamageCalculator.DamageType.BLUDGEONING) {
            return SCDamageCalculator.DamageType.BLUDGEONING;
        }
        if (isPiercing || bludgeoningToPiercing) {
            return SCDamageCalculator.DamageType.PIERCING;
        }
        return SCDamageCalculator.DamageType.SLASHING;
    }

    private static boolean isPiercingWeapon(Item item, int comboCount) {
        return (SCMeleeWeaponDefinitionsLoader.getData(item).animation() > 0 && isComboCountPiercing(item, comboCount)) ||
                SCMeleeWeaponDefinitionsLoader.getData(item).onlyDamageType() == SCDamageCalculator.DamageType.PIERCING;
    }

    private static boolean isComboCountPiercing(Item item, int comboCount) {
        SCMeleeWeaponDefinitionsLoader.DefinitionData attributeData = SCMeleeWeaponDefinitionsLoader.getData(item);
        int[] piercingAnimations = attributeData.piercingAnimation();
        int animation = attributeData.animation();
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

    public static float adjustDamageForBackstab(LivingEntity target, Vec3d playerPos, float damage) {
        Vec3d targetFacing = target.getRotationVec(1.0F).normalize();
        Vec3d attackDirection = playerPos.subtract(target.getPos()).normalize();
        boolean isBehind = targetFacing.dotProduct(attackDirection) < BACKSTAB_ANGLE_THRESHOLD;
        return isBehind ? damage * 2 : damage;
    }

    public static double getMaxDistance(Item item) {
        return getRadius(item, 4);
    }

    public static double getRadius(Item item, int index) {
        SCMeleeWeaponDefinitionsLoader.DefinitionData attributeData = SCMeleeWeaponDefinitionsLoader.getData(item);
        Map<String, Double> radiusValues = attributeData.radius();

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

    public static float calculateDamage(Item item, double distance, String key) {
        for (int i = 0; i <= 4; i++) {
            double radius = getRadius(item, i);
            if (distance < radius + RADIUS_TOLERANCE) {
                float attackDamage = getDamageValues(key, item);
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