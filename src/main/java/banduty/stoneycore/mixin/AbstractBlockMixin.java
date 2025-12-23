package banduty.stoneycore.mixin;

import banduty.stoneycore.util.patterns.StructureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.class)
public class AbstractBlockMixin {
    @Inject(method = "onPlace", at = @At("HEAD"))
    private void stoneycore$onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean notify, CallbackInfo ci) {
        if (!oldState.is(state.getBlock())) {
            StructureHelper.trySpawnEntity(level, pos, state);
        }
    }
}