package banduty.stoneycore.combat;

import banduty.stoneycore.data.SCAttributes;
import net.minecraft.world.entity.LivingEntity;

import java.util.Random;

public class DeflectChanceHelper {
    private static final Random random = new Random();

    public static boolean shouldDeflect(LivingEntity livingEntity) {
        double totalDeflect = livingEntity.getAttributeValue(SCAttributes.DEFLECT_CHANCE);

        return totalDeflect > random.nextDouble();
    }
}
