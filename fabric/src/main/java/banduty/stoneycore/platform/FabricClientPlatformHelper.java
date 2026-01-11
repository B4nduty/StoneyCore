package banduty.stoneycore.platform;

import banduty.stoneycore.platform.services.IClientPlatformHelper;
import io.wispforest.owo.shader.BlurProgram;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class FabricClientPlatformHelper implements IClientPlatformHelper {
    private static final BlurProgram BLUR = new BlurProgram();
    @Override
    public void startBlurService(float blur) {
        BLUR.setParameters(16, 12, blur);
        BLUR.use();
    }
}
