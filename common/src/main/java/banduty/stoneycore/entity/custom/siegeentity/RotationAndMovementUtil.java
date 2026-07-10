package banduty.stoneycore.entity.custom.siegeentity;

import banduty.stoneycore.entity.custom.AbstractSiegeEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class RotationAndMovementUtil {

    public static void updatePassengerPosition(AbstractSiegeEntity siege, Entity passenger, Entity.MoveFunction moveFunction) {
        if (!siege.hasPassenger(passenger)) {
            return;
        }

        float yaw;

        if (!(passenger instanceof Player) && passenger.getFirstPassenger() instanceof Player player) {
            yaw = player.getVisualRotationYInDegrees();
        } else if (passenger instanceof Player player) {
            yaw = player.getVisualRotationYInDegrees();
        } else {
            yaw = siege.getTrackedYaw();
        }

        siege.setTrackedYaw(yaw);
        siege.setYRot(yaw);
        siege.setYHeadRot(yaw);
        siege.setYBodyRot(yaw);
        siege.lastRiderYaw = yaw;

        float pitch = passenger instanceof Player player ? player.getXRot() : 0;

        siege.setTrackedPitch(pitch);
        siege.lastRiderPitch = pitch;

        double yawRad = Math.toRadians(yaw);

        double sin = Math.sin(yawRad);
        double cos = Math.cos(yawRad);

        Vec3 offset = siege.getPassengerOffset(passenger);

        double offsetX = cos * offset.x - (-sin) * offset.z;
        double offsetZ = sin * offset.x - cos * offset.z;

        double x = siege.getX() + offsetX;
        double y = siege.getY() + offset.y;
        double z = siege.getZ() + offsetZ;

        moveFunction.accept(passenger, x, y, z);

        if (!(passenger instanceof Player) && passenger.getFirstPassenger() == null) {
            passenger.setYRot(yaw);
            passenger.setYHeadRot(yaw);
            passenger.setYBodyRot(yaw);
        }
    }


    public static void updateSiegeVelocity(AbstractSiegeEntity siege) {
        double horizontalX = 0;
        double horizontalZ = 0;

        if (siege.getCooldown() == 0) {
            Entity passenger = siege.getFirstPassenger();
            Player rider = null;

            if (passenger instanceof Player player) {
                rider = player;
            } else if (passenger != null && passenger.getFirstPassenger() instanceof Player player) {
                rider = player;
            }

            if (rider != null) {
                double forward = rider.zza * siege.getVelocity(rider);

                double yawRad = Math.toRadians(siege.getVisualRotationYInDegrees());

                horizontalX = -Math.sin(yawRad) * forward;
                horizontalZ = Math.cos(yawRad) * forward;
            }
        }

        Vec3 velocity = siege.getDeltaMovement();

        double verticalY = velocity.y - 0.08;

        if (siege.onGround() && verticalY < 0) {
            verticalY = 0;
        }

        Vec3 newVelocity = new Vec3(horizontalX, verticalY, horizontalZ);

        siege.setDeltaMovement(newVelocity);
        siege.move(MoverType.SELF, newVelocity);
    }


    public static void updateWheelRotation(AbstractSiegeEntity siege) {
        Vec3 velocity = siege.getDeltaMovement();

        double yawRad = Math.toRadians(siege.getVisualRotationYInDegrees());

        double forwardX = -Math.sin(yawRad);
        double forwardZ = Math.cos(yawRad);

        double directionalSpeed =
                -(velocity.x * forwardX + velocity.z * forwardZ);

        siege.wheelRotation += (float) (directionalSpeed * 72);

        siege.wheelRotation %= 360f;

        if (siege.wheelRotation < 0) {
            siege.wheelRotation += 360f;
        }
    }
}