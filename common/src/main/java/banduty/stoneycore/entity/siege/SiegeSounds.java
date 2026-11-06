package banduty.stoneycore.entity.siege;

import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * All sound handling for one engine. Remembers exactly which players were sent a sound,
 * so "stop" packets go only to them instead of every player in the level.
 */
public final class SiegeSounds {

    private final AbstractSiegeEntity siege;
    private final Set<UUID> moveListeners = new HashSet<>();
    private final Set<UUID> reloadListeners = new HashSet<>();
    private int moveTick;
    private boolean reloadActive;

    public SiegeSounds(AbstractSiegeEntity siege) {
        this.siege = siege;
    }

    public void tick(ServerLevel level, boolean moving, boolean reloading) {
        SiegeProperties props = siege.getProperties();

        if (moving && siege.isAlive()) {
            if (moveTick == 0 || moveTick >= props.moveSoundDelay()) {
                playNearby(level, props.moveSound(), props.moveSoundRange(), moveListeners);
                moveTick = 0;
            }
            moveTick++;
        } else if (moveTick != 0 || !moveListeners.isEmpty()) {
            moveTick = 0;
            stop(level, props.moveSound(), moveListeners);
        }

        if (reloading && !reloadActive) {
            reloadActive = true;
            playNearby(level, props.reloadSound(), props.reloadSoundRange(), reloadListeners);
        } else if (!reloading && reloadActive) {
            reloadActive = false;
            stop(level, props.reloadSound(), reloadListeners);
        }
    }

    public void playShoot(ServerLevel level) {
        SiegeProperties props = siege.getProperties();
        playNearby(level, props.shootSound(), props.shootSoundRange(), null);
    }

    public void playAttack(ServerLevel level) {
        SiegeProperties props = siege.getProperties();
        playNearby(level, props.attackSound(), props.attackSoundRange(), null);
    }

    public void stopAll(ServerLevel level) {
        SiegeProperties props = siege.getProperties();
        stop(level, props.moveSound(), moveListeners);
        stop(level, props.reloadSound(), reloadListeners);
        moveTick = 0;
        reloadActive = false;
    }

    private void playNearby(ServerLevel level, SoundEvent sound, double range, Set<UUID> track) {
        Vec3 origin = siege.position();
        for (ServerPlayer player : level.players()) {
            double distance = player.position().distanceTo(origin);
            if (distance > range) continue;

            float volume = (float) (1.0 - distance / range);
            if (volume <= 0f) continue;

            player.playNotifySound(sound, SoundSource.AMBIENT, volume,
                    Mth.randomBetween(siege.getRandom(), 0.75f, 1.25f));
            if (track != null) track.add(player.getUUID());
        }
    }

    private void stop(ServerLevel level, SoundEvent sound, Set<UUID> listeners) {
        if (listeners.isEmpty()) return;
        ClientboundStopSoundPacket packet = new ClientboundStopSoundPacket(sound.getLocation(), SoundSource.AMBIENT);
        for (UUID id : listeners) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(id);
            if (player != null) player.connection.send(packet);
        }
        listeners.clear();
    }
}
