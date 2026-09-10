package banduty.stoneycore.combat.range;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IRangedWeaponHandler {
    String getTypeId();
    void shoot(Level level, LivingEntity livingEntity, ItemStack weapon);
    void reload(Level level, LivingEntity livingEntity, ItemStack weapon);
    void handleRelease(ItemStack stack, Level level, LivingEntity livingEntity, int useTime, ItemStack arrowStack);
    boolean canShoot(ItemStack weapon);
    void handleUsageTick(Level level, ItemStack stack, LivingEntity livingEntity, int useTime);
}
