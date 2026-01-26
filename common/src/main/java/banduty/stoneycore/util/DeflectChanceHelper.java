package banduty.stoneycore.util;

import banduty.stoneycore.platform.Services;
import net.minecraft.world.entity.LivingEntity;

import java.util.Random;

public class DeflectChanceHelper {
    private static final Random random = new Random();

    public static boolean shouldDeflect(LivingEntity livingEntity) {
        double totalDeflect = livingEntity.getAttributeValue(Services.ATTRIBUTES.getDeflectChance());

        return totalDeflect > random.nextDouble();
    }
}
