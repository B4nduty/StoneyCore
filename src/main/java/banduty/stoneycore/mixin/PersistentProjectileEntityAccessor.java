package banduty.stoneycore.mixin;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PersistentProjectileEntity.class)
public interface PersistentProjectileEntityAccessor {
    @Accessor("piercedEntities")
    IntOpenHashSet getPiercedEntities();

    @Accessor("piercedEntities")
    void setPiercedEntities(IntOpenHashSet piercedEntities);
}
