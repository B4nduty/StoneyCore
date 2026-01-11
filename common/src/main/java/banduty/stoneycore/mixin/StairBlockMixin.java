package banduty.stoneycore.mixin;

import banduty.stoneycore.util.patterns.StructureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StairBlock.class)
public class StairBlockMixin {
    @Inject(method = "onPlace", at = @At("HEAD"))
    private void stoneycore$onBlockAdded(BlockState state, Level level, BlockPos blockPos, BlockState oldState, boolean bl, CallbackInfo ci) {
        if (!oldState.is(state.getBlock())) {
            StructureHelper.trySpawnEntity(level, blockPos, state);
        }
    }
}