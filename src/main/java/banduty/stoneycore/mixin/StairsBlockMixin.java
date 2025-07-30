package banduty.stoneycore.mixin;

import banduty.stoneycore.util.patterns.StructureHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StairsBlock.class)
public class StairsBlockMixin {
    @Inject(method = "onBlockAdded", at = @At("HEAD"))
    private void stoneycore$onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify, CallbackInfo ci) {
        if (!oldState.isOf(state.getBlock())) {
            StructureHelper.trySpawnEntity(world, pos, state);
        }
    }
}