package banduty.stoneycore.mixin;

import banduty.stoneycore.util.definitionsloader.SCMeleeWeaponDefinitionsLoader;
import banduty.stoneycore.util.weaponutil.SCWeaponUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final
    MinecraftClient client;

    @ModifyConstant(method = "updateTargetedEntity", constant = @Constant(doubleValue = (double) 3.0F))
    private double injected(double constant) {
        if (client.player != null && SCMeleeWeaponDefinitionsLoader.containsItem(client.player.getMainHandStack().getItem())) {
            return (float) SCWeaponUtil.getMaxDistance(client.player.getMainHandStack().getItem()) + 1;
        }
        return constant;
    }
}