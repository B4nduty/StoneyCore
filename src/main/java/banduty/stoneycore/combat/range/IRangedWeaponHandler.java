package banduty.stoneycore.combat.range;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IRangedWeaponHandler {
    String getTypeId();
    void shoot(World world, PlayerEntity player, ItemStack weapon);
    void reload(World world, PlayerEntity player, ItemStack weapon);
    boolean canShoot(ItemStack weapon);
    void handleUsageTick(World world, ItemStack stack, PlayerEntity player, int useTime);
}
