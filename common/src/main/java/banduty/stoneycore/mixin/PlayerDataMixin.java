package banduty.stoneycore.mixin;

import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class PlayerDataMixin implements IEntityDataSaver {

    @Unique
    private final CompoundTag stoneycore$persistentData = new CompoundTag();

    @Override
    public CompoundTag stoneycore$getPersistentData() {
        return stoneycore$persistentData;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void stoneycore$writeNbt(CompoundTag nbt, CallbackInfo ci) {
        nbt.put("StoneyCoreData", stoneycore$persistentData);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void stoneycore$readNbt(CompoundTag nbt, CallbackInfo ci) {
        if (nbt.contains("StoneyCoreData")) {
            stoneycore$persistentData.merge(nbt.getCompound("StoneyCoreData"));
        }
    }
}
