package banduty.stoneycore.entity.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;

public interface AbstractSiegeHelper {
    void updateSiegeNetworkData(ServerLevel serverLevel, AbstractSiegeEntity abstractSiegeEntity);
}
