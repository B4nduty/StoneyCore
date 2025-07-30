package banduty.stoneycore.entity.custom.siegeentity;

import banduty.stoneycore.entity.custom.AbstractSiegeEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class RotationAndMovementUtil {
    public static void updatePassengerPosition(AbstractSiegeEntity abstractSiegeEntity, Entity passenger, Entity.PositionUpdater positionUpdater) {
        if (abstractSiegeEntity.hasPassenger(passenger)) {
            if (!(passenger instanceof PlayerEntity) && passenger.getFirstPassenger() instanceof PlayerEntity player) abstractSiegeEntity.setTrackedYaw(player.getBodyYaw());
            else if (passenger instanceof PlayerEntity) abstractSiegeEntity.setTrackedYaw(passenger.getBodyYaw());
            abstractSiegeEntity.setYaw(abstractSiegeEntity.getTrackedYaw());
            abstractSiegeEntity.setHeadYaw(abstractSiegeEntity.getTrackedYaw());
            abstractSiegeEntity.setBodyYaw(abstractSiegeEntity.getTrackedYaw());
            abstractSiegeEntity.lastRiderYaw = abstractSiegeEntity.getTrackedYaw();

            if (!(passenger instanceof PlayerEntity)) abstractSiegeEntity.setTrackedPitch(0);
            else abstractSiegeEntity.setTrackedPitch(passenger.getPitch());
            abstractSiegeEntity.lastRiderPitch = abstractSiegeEntity.getTrackedPitch();

            float yawRadians = (float) Math.toRadians(abstractSiegeEntity.getBodyYaw());
            Vec3d offset = abstractSiegeEntity.getPassengerOffset(passenger);
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

            positionUpdater.accept(passenger, newX, newY, newZ);

            if (!(passenger instanceof PlayerEntity) && passenger.getFirstPassenger() == null) {
                passenger.setYaw(abstractSiegeEntity.getTrackedYaw());
                passenger.setHeadYaw(abstractSiegeEntity.getTrackedYaw());
                passenger.setBodyYaw(abstractSiegeEntity.getTrackedYaw());
            }
        }
    }

    public static void updateSiegeVelocity(AbstractSiegeEntity abstractSiegeEntity) {
        Vec3d horizontalVelocity = Vec3d.ZERO;
        Entity passenger = abstractSiegeEntity.getFirstPassenger();

        if (passenger != null) {
            if (passenger instanceof PlayerEntity playerEntity && abstractSiegeEntity.getCooldown() == 0) {
                double forward = playerEntity.forwardSpeed * abstractSiegeEntity.getVelocity(playerEntity);
                horizontalVelocity = calculateMovementVector(forward, abstractSiegeEntity.getBodyYaw());
            } else if (passenger.getFirstPassenger() != null && passenger.getFirstPassenger() instanceof PlayerEntity passengerRider && abstractSiegeEntity.getCooldown() == 0) {
                double forward = passengerRider.forwardSpeed * abstractSiegeEntity.getVelocity(passenger);
                horizontalVelocity = calculateMovementVector(forward, abstractSiegeEntity.getBodyYaw());
            }
        }

        Vec3d velocity = abstractSiegeEntity.getVelocity();

        double verticalVelocity = velocity.y;

        verticalVelocity -= 0.08;

        if (abstractSiegeEntity.isOnGround() && verticalVelocity < 0) {
            verticalVelocity = 0;
        }

        Vec3d newVelocity = new Vec3d(horizontalVelocity.x, verticalVelocity, horizontalVelocity.z);

        abstractSiegeEntity.setVelocity(newVelocity);

        abstractSiegeEntity.move(MovementType.SELF, newVelocity);
    }

    private static Vec3d calculateMovementVector(double forward, double yaw) {
        double yawRad = Math.toRadians(yaw);
        double x = -Math.sin(yawRad) * forward;
        double z = Math.cos(yawRad) * forward;
        return new Vec3d(x, 0, z);
    }

    public static void updateWheelRotation(AbstractSiegeEntity abstractSiegeEntity) {
        Vec3d velocity = abstractSiegeEntity.getVelocity();

        float yawRadians = (float) Math.toRadians(abstractSiegeEntity.getBodyYaw());
        Vec3d forward = new Vec3d(-Math.sin(yawRadians), 0, Math.cos(yawRadians)).normalize();

        double directionalSpeed = -velocity.dotProduct(forward);

        abstractSiegeEntity.wheelRotation += (float) (directionalSpeed * 72);
        abstractSiegeEntity.wheelRotation %= 360f;

        if (abstractSiegeEntity.wheelRotation < 0) {
            abstractSiegeEntity.wheelRotation += 360f;
        }
    }
}
