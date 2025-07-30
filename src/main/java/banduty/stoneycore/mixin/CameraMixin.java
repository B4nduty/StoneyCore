package banduty.stoneycore.mixin;

import banduty.stoneycore.entity.custom.AbstractSiegeEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {
    @Inject(method = "update", at = @At("TAIL"))
    private void onUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (focusedEntity.getVehicle() instanceof AbstractSiegeEntity abstractSiegeEntity && !thirdPerson) {
            Camera camera = ((Camera) (Object) this);
            Vec3d newPos = camera.getPos().add(abstractSiegeEntity.getPlayerPOV());
            ((CameraAccessor) camera).callSetPos(newPos.getX(), newPos.getY(), newPos.getZ());
        }
    }
}
