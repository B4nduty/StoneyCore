package banduty.stoneycore.entity.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;

public interface AbstractSiegeHelper {
    SoundEvent getDefaultHitGroundSoundEvent();
    void updateSiegeNetworkData(ServerLevel serverLevel, AbstractSiegeEntity abstractSiegeEntity);
}
