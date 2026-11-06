package banduty.stoneycore.entity.siege;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Everything about driving, seating and orienting an engine, in one place.
 * <p>
 * Convention for local offsets: x = right, y = up, z = BACKWARD (so a seat at z=+1.25 is behind the engine).
 * This is the same convention the old passenger / dismount code used.
 */
public final class SiegeMovement {

    private static final double GRAVITY = 0.08;
    private static final double WHEEL_DEGREES_PER_BLOCK = 72.0;

    private SiegeMovement() {}

    /** The player controlling the engine: a direct passenger, or the rider of a mounted horse. */
    @Nullable
    public static Player getRider(AbstractSiegeEntity siege) {
        Entity passenger = siege.getFirstPassenger();
        if (passenger instanceof Player player) return player;
        if (passenger != null && passenger.getFirstPassenger() instanceof Player player) return player;
        return null;
    }

    public static Vec3 forward(float yawDegrees) {
        double yaw = Math.toRadians(yawDegrees);
        return new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
    }

    public static Vec3 direction(float yawDegrees, float pitchDegrees) {
        double yaw = Math.toRadians(yawDegrees);
        double pitch = Math.toRadians(pitchDegrees);
        return new Vec3(
                -Math.sin(yaw) * Math.cos(pitch),
                -Math.sin(pitch),
                Math.cos(yaw) * Math.cos(pitch)
        ).normalize();
    }

    /** Rotates a local offset (see class doc) into a world-space offset. */
    public static Vec3 localToWorld(float yawDegrees, Vec3 local) {
        double yaw = Math.toRadians(yawDegrees);
        double sin = Math.sin(yaw);
        double cos = Math.cos(yaw);
        return new Vec3(cos * local.x + sin * local.z, local.y, sin * local.x - cos * local.z);
    }

    public static void positionPassenger(AbstractSiegeEntity siege, Entity passenger, Entity.MoveFunction moveFunction) {
        if (!siege.hasPassenger(passenger)) return;

        Player rider = passenger instanceof Player player ? player
                : passenger.getFirstPassenger() instanceof Player player ? player : null;

        if (rider != null) {
            siege.setFacing(rider.getVisualRotationYInDegrees());
            siege.setAimPitch(rider.getXRot());
        }

        Vec3 pos = siege.position().add(localToWorld(siege.getYRot(), siege.getPassengerOffset(passenger)));
        moveFunction.accept(passenger, pos.x, pos.y, pos.z);

        if (!(passenger instanceof Player) && passenger.getFirstPassenger() == null) {
            float yaw = siege.getYRot();
            passenger.setYRot(yaw);
            passenger.setYHeadRot(yaw);
            passenger.setYBodyRot(yaw);
        }
    }

    /** Applies rider input + gravity and moves the engine. Called exactly once per server tick. */
    public static void tickMovement(AbstractSiegeEntity siege) {
        double x = 0;
        double z = 0;

        Player rider = getRider(siege);
        if (rider != null && siege.getCooldown() == 0) {
            double speed = siege.getVelocity(siege.getFirstPassenger());
            Vec3 move = forward(siege.getYRot()).scale(rider.zza * speed);
            x = move.x;
            z = move.z;
        }

        double y = siege.getDeltaMovement().y - GRAVITY;
        if (siege.onGround() && y < 0) y = 0;

        Vec3 velocity = new Vec3(x, y, z);
        siege.setDeltaMovement(velocity);
        siege.move(MoverType.SELF, velocity);
    }

    public static void updateWheelRotation(AbstractSiegeEntity siege) {
        Vec3 velocity = siege.getDeltaMovement();
        Vec3 forward = forward(siege.getYRot());
        double directionalSpeed = -(velocity.x * forward.x + velocity.z * forward.z);

        float rotation = (siege.wheelRotation + (float) (directionalSpeed * WHEEL_DEGREES_PER_BLOCK)) % 360f;
        if (rotation < 0) rotation += 360f;
        siege.wheelRotation = rotation;
    }

    /**
     * Handles the client's aim packet (identical for Fabric and NeoForge). Ignores non-finite values and
     * players that are not operating an engine. A rider on a horse only steers; pitch is ignored.
     */
    public static void applyClientAim(ServerPlayer player, float yaw, float pitch) {
        if (!Float.isFinite(yaw) || !Float.isFinite(pitch)) return;

        Entity vehicle = player.getVehicle();
        if (vehicle instanceof AbstractSiegeEntity siege) {
            siege.setFacing(yaw);
            if (pitch != 0) siege.setAimPitch(pitch);
        } else if (vehicle instanceof Horse horse && horse.getVehicle() instanceof AbstractSiegeEntity siege) {
            siege.setFacing(yaw);
        }
    }

    public static float wrap(float yaw) {
        return Mth.wrapDegrees(yaw);
    }
}
