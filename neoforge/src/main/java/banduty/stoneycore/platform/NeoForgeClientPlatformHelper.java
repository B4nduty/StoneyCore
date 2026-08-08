package banduty.stoneycore.platform;

import banduty.stoneycore.platform.services.IClientPlatformHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

public class NeoForgeClientPlatformHelper implements IClientPlatformHelper {

    @Override
    public BakedModel getModel(ResourceLocation resourceLocation) {
        return Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.inventory(resourceLocation));
    }
}