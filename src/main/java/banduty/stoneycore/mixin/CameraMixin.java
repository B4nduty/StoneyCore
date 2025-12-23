package banduty.stoneycore.mixin;

import banduty.stoneycore.entity.custom.AbstractSiegeEntity;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {
    @Inject(method = "setup", at = @At("TAIL"))
    private void stoneycore$onUpdate(BlockGetter area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (focusedEntity.getVehicle() instanceof AbstractSiegeEntity abstractSiegeEntity && !thirdPerson) {
            Camera camera = ((Camera) (Object) this);
            Vec3 newPos = camera.getPosition().add(abstractSiegeEntity.getPlayerPOV());
            ((CameraAccessor) camera).callSetPos(newPos.x(), newPos.y(), newPos.z());
        }
    }
}
