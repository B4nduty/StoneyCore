package banduty.stoneycore.platform;

import banduty.stoneycore.platform.services.IClientPlatformHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class FabricClientPlatformHelper implements IClientPlatformHelper {
    @Override
    public void startBlurService(float blur) {

    }
}
