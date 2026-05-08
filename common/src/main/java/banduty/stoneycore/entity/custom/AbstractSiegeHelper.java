package banduty.stoneycore.entity.custom;

import net.minecraft.server.level.ServerLevel;

public interface AbstractSiegeHelper {
    void updateSiegeNetworkData(ServerLevel serverLevel, AbstractSiegeEntity abstractSiegeEntity);
}
