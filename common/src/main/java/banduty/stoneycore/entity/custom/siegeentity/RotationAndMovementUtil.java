package banduty.stoneycore.entity.custom.siegeentity;

import banduty.stoneycore.entity.custom.AbstractSiegeEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class RotationAndMovementUtil {
    public static void updatePassengerPosition(AbstractSiegeEntity abstractSiegeEntity, Entity passenger, Entity.MoveFunction moveFunction) {
        if (abstractSiegeEntity.hasPassenger(passenger)) {
            if (!(passenger instanceof Player) && passenger.getFirstPassenger() instanceof Player player) abstractSiegeEntity.setTrackedYaw(player.getVisualRotationYInDegrees());
            else if (passenger instanceof Player) abstractSiegeEntity.setTrackedYaw(passenger.getVisualRotationYInDegrees());
            abstractSiegeEntity.setYRot(abstractSiegeEntity.getTrackedYaw());
            abstractSiegeEntity.setYHeadRot(abstractSiegeEntity.getTrackedYaw());
            abstractSiegeEntity.setYBodyRot(abstractSiegeEntity.getTrackedYaw());
            abstractSiegeEntity.lastRiderYaw = abstractSiegeEntity.getTrackedYaw();

            if (!(passenger instanceof Player)) abstractSiegeEntity.setTrackedPitch(0);
            else abstractSiegeEntity.setTrackedPitch(passenger.getXRot());
            abstractSiegeEntity.lastRiderPitch = abstractSiegeEntity.getTrackedPitch();

            float yawRadians = (float) Math.toRadians(abstractSiegeEntity.getVisualRotationYInDegrees());
            Vec3 offset = abstractSiegeEntity.getPassengerOffset(passenger);
            double leftOffset = offset.x;
            double backOffset = offset.z;

            double forwardX = -Math.sin(yawRadians);
            double forwardZ =  Math.cos(yawRadians);

            double rightX =  Math.cos(yawRadians);
            double rightZ =  Math.sin(yawRadians);

            double offsetX = rightX * leftOffset - forwardX * backOffset;
            double offsetZ = rightZ * leftOffset - forwardZ * backOffset;

            double newX = abstractSiegeEntity.getX() + offsetX;
            double newY = abstractSiegeEntity.getY() + offset.y;
            double newZ = abstractSiegeEntity.getZ() + offsetZ;

            moveFunction.accept(passenger, newX, newY, newZ);

            if (!(passenger instanceof Player) && passenger.getFirstPassenger() == null) {
                passenger.setYRot(abstractSiegeEntity.getTrackedYaw());
                passenger.setYHeadRot(abstractSiegeEntity.getTrackedYaw());
                passenger.setYBodyRot(abstractSiegeEntity.getTrackedYaw());
            }
        }
    }

    public static void updateSiegeVelocity(AbstractSiegeEntity abstractSiegeEntity) {
        Vec3 horizontalVelocity = Vec3.ZERO;
        Entity passenger = abstractSiegeEntity.getFirstPassenger();

        if (passenger != null) {
            if (passenger instanceof Player player && abstractSiegeEntity.getCooldown() == 0) {
                double forward = player.zza * abstractSiegeEntity.getVelocity(player);
                horizontalVelocity = calculateMovementVector(forward, abstractSiegeEntity.getVisualRotationYInDegrees());
            } else if (passenger.getFirstPassenger() != null && passenger.getFirstPassenger() instanceof Player passengerRider && abstractSiegeEntity.getCooldown() == 0) {
                double forward = passengerRider.zza * abstractSiegeEntity.getVelocity(passenger);
                horizontalVelocity = calculateMovementVector(forward, abstractSiegeEntity.getVisualRotationYInDegrees());
            }
        }

        Vec3 velocity = abstractSiegeEntity.getDeltaMovement();

        double verticalVelocity = velocity.y;

        verticalVelocity -= 0.08;

        if (abstractSiegeEntity.onGround() && verticalVelocity < 0) {
            verticalVelocity = 0;
        }

        Vec3 newVelocity = new Vec3(horizontalVelocity.x, verticalVelocity, horizontalVelocity.z);

        abstractSiegeEntity.setDeltaMovement(newVelocity);

        abstractSiegeEntity.move(MoverType.SELF, newVelocity);
    }

    private static Vec3 calculateMovementVector(double forward, double yaw) {
        double yawRad = Math.toRadians(yaw);
        double x = -Math.sin(yawRad) * forward;
        double z = Math.cos(yawRad) * forward;
        return new Vec3(x, 0, z);
    }

    public static void updateWheelRotation(AbstractSiegeEntity abstractSiegeEntity) {
        Vec3 velocity = abstractSiegeEntity.getDeltaMovement();

        float yawRadians = (float) Math.toRadians(abstractSiegeEntity.getVisualRotationYInDegrees());
        Vec3 forward = new Vec3(-Math.sin(yawRadians), 0, Math.cos(yawRadians)).normalize();

        double directionalSpeed = -velocity.dot(forward);

        abstractSiegeEntity.wheelRotation += (float) (directionalSpeed * 72);
        abstractSiegeEntity.wheelRotation %= 360f;

        if (abstractSiegeEntity.wheelRotation < 0) {
            abstractSiegeEntity.wheelRotation += 360f;
        }
    }
}
