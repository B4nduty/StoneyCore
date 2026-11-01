package banduty.stoneycore.mixin;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractArrow.class)
public interface AbstractArrowAccessor {
    @Accessor("piercingIgnoreEntityIds")
    IntOpenHashSet getPiercingIgnoreEntityIds();

    @Accessor("piercingIgnoreEntityIds")
    void setPiercingIgnoreEntityIds(IntOpenHashSet piercingIgnoreEntityIds);

    @Invoker("getPickupItem")
    ItemStack stoneycore$getPickupItem();
}
