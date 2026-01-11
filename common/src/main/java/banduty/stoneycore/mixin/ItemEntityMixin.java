package banduty.stoneycore.mixin;

import banduty.stoneycore.block.CraftmanAnvilBlock;
import banduty.stoneycore.block.CraftmanAnvilBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ItemEntity entity = (ItemEntity)(Object)this;
        Level world = entity.level();

        if (!world.isClientSide() && entity.onGround()) {
            BlockPos pos = entity.getOnPos();
            BlockState state = world.getBlockState(pos);

            if (state.getBlock() instanceof CraftmanAnvilBlock) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof CraftmanAnvilBlockEntity anvilEntity) {
                    if (anvilEntity.addItem(entity.getItem())) {
                        entity.discard();
                        world.playSound(null, pos, SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 0.5f, 1.0f);
                    }
                }
            }
        }
    }
}