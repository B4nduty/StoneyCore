package banduty.stoneycore.util.weaponutil;

import banduty.stoneycore.util.SCDamageCalculator;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsLoader;
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

    public static double getDamageValues(SCDamageCalculator.DamageType key, Item item) {
        WeaponDefinitionsLoader.DefinitionData attributeData = WeaponDefinitionsLoader.getData(item);
        Map<String, Float> damageValues = attributeData.melee().damage();

        return damageValues.getOrDefault(key.name(), 0f);
    }

    public static SCDamageCalculator.DamageType calculateDamageType(ItemStack stack, int comboCount) {
        boolean bludgeoningToPiercing = getDamageValues(SCDamageCalculator.DamageType.SLASHING, stack.getItem()) == 0
                && getDamageValues(SCDamageCalculator.DamageType.PIERCING, stack.getItem()) > 0
                && getDamageValues(SCDamageCalculator.DamageType.BLUDGEONING, stack.getItem()) > 0;
        boolean isBludgeoning = stack.getNbt() != null && stack.getNbt().getBoolean("bludgeoning");
        boolean isPiercing = isPiercingWeapon(stack.getItem(), comboCount);

        if (isBludgeoning ^ bludgeoningToPiercing || WeaponDefinitionsLoader.getData(stack).melee().onlyDamageType() == SCDamageCalculator.DamageType.BLUDGEONING) {
            return SCDamageCalculator.DamageType.BLUDGEONING;
        }
        if (isPiercing || bludgeoningToPiercing) {
            return SCDamageCalculator.DamageType.PIERCING;
        }
        return SCDamageCalculator.DamageType.SLASHING;
    }

    private static boolean isPiercingWeapon(Item item, int comboCount) {
        return (WeaponDefinitionsLoader.getData(item).melee().animation() > 0 && isComboCountPiercing(item, comboCount)) ||
                WeaponDefinitionsLoader.getData(item).melee().onlyDamageType() == SCDamageCalculator.DamageType.PIERCING;
    }

    private static boolean isComboCountPiercing(Item item, int comboCount) {
        WeaponDefinitionsLoader.DefinitionData attributeData = WeaponDefinitionsLoader.getData(item);
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

    public static double adjustDamageForBackstab(LivingEntity target, Vec3d playerPos, double damage) {
        Vec3d targetFacing = target.getRotationVec(1.0F).normalize();
        Vec3d attackDirection = playerPos.subtract(target.getPos()).normalize();
        boolean isBehind = targetFacing.dotProduct(attackDirection) < BACKSTAB_ANGLE_THRESHOLD;
        return isBehind ? damage * 2 : damage;
    }

    public static double getMaxDistance(Item item) {
        return getRadius(item, 4);
    }

    public static double getRadius(Item item, int index) {
        WeaponDefinitionsLoader.DefinitionData attributeData = WeaponDefinitionsLoader.getData(item);
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