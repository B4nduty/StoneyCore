package banduty.stoneycore.entity.siege;

import net.minecraft.server.level.ServerLevel;

public interface AbstractSiegeHelper {
    void updateSiegeNetworkData(ServerLevel serverLevel, AbstractSiegeEntity abstractSiegeEntity);
}
