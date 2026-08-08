package banduty.stoneycore.platform.services;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

public interface IClientPlatformHelper {
    BakedModel getModel(ResourceLocation resourceLocation);
}