package banduty.stoneycore.combat.range;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IRangedWeaponHandler {
    String getTypeId();
    void shoot(Level level, Player player, ItemStack weapon);
    void reload(Level level, Player player, ItemStack weapon);
    void handleRelease(ItemStack stack, Level level, Player player, int useTime, ItemStack arrowStack);
    boolean canShoot(ItemStack weapon);
    void handleUsageTick(Level level, ItemStack stack, Player player, int useTime);
}
